package rww.store

import java.io.StringReader

import org.scalajs.dom
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.raw.{XMLHttpRequest, ProgressEvent}
import org.w3.banana.RDFOps
import org.w3.banana.io._
import rww.Rdf
import rx.Var

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

sealed trait RequestState
sealed trait Response {
  def code: Int
  def headers: String
  def body: String
}

object UnRequested extends RequestState
case class Downloading(uri: Rdf#URI, percentage: Option[Double]=None) extends RequestState
case class Redirected(to: Rdf#URI) extends RequestState
case class HttpError(code: Int, headers: String, body: String) extends RequestState with Response
case class Ok(code: Int,
              url: Rdf#URI,
              headers: String,
              body: String,
              parsed: Try[Rdf#Graph] ) extends RequestState with Response

/**
 *
 * @param proxy a function to map a URI to a proxy URI through which requests
 *              can be made as if for the original one.
 */
class WebAgent(proxy: Rdf#URI => Rdf#URI = (u: Rdf#URI)=>u)
                          (implicit
                           ec: ExecutionContext,
                           ops: RDFOps[Rdf],
                           rdrNT:  RDFReader[Rdf, Try, NTriples],
                           rdrTurtle:  RDFReader[Rdf, Future, Turtle],
                           rdrJSONLD: RDFReader[Rdf, Future, JsonLd]) {

  import ops._

  //todo: use WeakMap https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/WeakMap
  //use code from https://github.com/scala-js/scala-js/blob/master/javalanglib/src/main/scala/java/lang/System.scala
  private val cache = new collection.mutable.HashMap[Rdf#URI, Var[RequestState]]()

  //todo Q: should fetch return anything?
  def fetch(url: Rdf#URI, counter: Int = 1): Var[RequestState] = {
    // for AJAX calls http://lihaoyi.github.io/hands-on-scala-js/#dom.extensions
    //and for CORS see http://www.html5rocks.com/en/tutorials/cors/
    val base = url.fragmentLess
    import org.w3.banana.TryW
    cache.get(base) match {
      case Some(res) => res() match {
        case Redirected(to) => if( counter >= 0) fetch(to, counter-1) else res
        case other => res
      }
      case None => {
        import scalaz.Scalaz._

        val proxiedURL = proxy(base)
        val reqStateVar: Var[RequestState] = Var(Downloading(base))
        cache.put(base,reqStateVar)
        AjaxPlus.get(
          proxiedURL.toString,
          headers = Map("Accept" -> "application/n-triples,application/ld+json;q=0.8,text/turtle;q=0.8"),
          progress = (ev: ProgressEvent) => {
            if (ev.lengthComputable && ev.loaded != ev.total) reqStateVar.update(Downloading(base,(ev.lengthComputable).option(ev.loaded/ev.total)))
          }
        ) onComplete { xhrTry =>

          def cacheStateOf(xhr: XMLHttpRequest)(finalURLToState: Rdf#URI => RequestState): Unit = {
            val redirectURLOpt = xhr.asInstanceOf[js.Dynamic].responseURL.asInstanceOf[js.UndefOr[String]]
            val finalURL = redirectURLOpt.map { redirectURLstr =>
              val redirectURL = URI(redirectURLstr)
              if (redirectURL == proxiedURL) base else redirectURL
            } getOrElse {
              base
            }
            val finalURLVar: Var[RequestState] = if (finalURL != base) {
              reqStateVar.update(Redirected(finalURL))
              cache.get(finalURL).getOrElse{
                val v: Var[RequestState] = Var(Downloading(base))
                cache.put(finalURL, v)
                v
              }
            } else reqStateVar

            finalURLVar.update(finalURLToState(finalURL))
          }

          xhrTry match {
            case Success(xhr) => {
              // responseURL is quite new. See https://xhr.spec.whatwg.org/#the-responseurl-attribute
              // hence need for UndefOr
              // todo: to remove dynamic use need to fix https://github.com/scala-js/scala-js-dom/issues/111
              val rh = Option(xhr.getResponseHeader("Content-Type"))
              val reader = new StringReader(xhr.responseText)
              for {
                g <- rh.map(_.takeWhile(_ != ';').trim.toLowerCase) match {
                  case Some("application/n-triples") => rdrNT.read(reader, base.toString).asFuture
                  case Some("application/ld+json") => rdrJSONLD.read(reader, base.toString)
                  case Some("text/turtle") => rdrTurtle.read(reader, base.toString)
                  case Some(other) => Future.failed(new Exception("could not find parser for " + other))
                  case None => Future.failed(new Exception("problem fetching remote resource:" + xhr.statusText))
                }
              } yield {

                cacheStateOf(xhr) { graphUrl =>
                  Ok(xhr.status,
                    graphUrl,
                    xhr.getAllResponseHeaders(),
                    xhr.responseText,
                    Success(g)
                  )
                }
              }
            }
            case Failure(AjaxException(xhr)) => {
              println(s"Failure for <$base> with code ${xhr.status}")
              //todo: deal with redirects here too
              cacheStateOf(xhr)(url => HttpError(code = xhr.status, headers = xhr.getAllResponseHeaders(), body = xhr.responseText))
            }
            case Failure(other) => {
              //todo: deal correctly with redirects here too if it makes sense
              println(s"Other failure! " + other.toString)
              reqStateVar.update(HttpError(code = 477, headers = "", body = other.toString))
            }
          }

          //doit

        }
        reqStateVar
      }
    }
  }

  //just to get going
  //should it return the new graph?
  def vsimplePatch(url: Rdf#URI, add: Rdf#Triple, remove: Rdf#Triple): Unit = {
    val r = cache.get(url) map { reqVar =>
      reqVar() match {
        case Ok(code,url,headers,body,parsed) => {
           //todo: we should of course send a PATCH to the server here
          reqVar.update(Ok(code,url,headers,body,parsed.map{ graph=>
            graph.diff(Graph(remove)) union Graph(add)
          }))
        }
        case other => other
      }
    }
  }

}

object AjaxPlus {


  def apply(method: String,
            url: String,
            data: String,
            progress: ProgressEvent => Unit = e => (),
            timeout: Int,
            headers: Map[String, String],
            withCredentials: Boolean,
            responseType: String): Future[dom.XMLHttpRequest] = {
    val req = new dom.XMLHttpRequest()
    val promise = Promise[dom.XMLHttpRequest]()

    req.onreadystatechange = {(e: dom.Event) =>
      if (req.readyState.toInt == 4){
        req.onprogress = null
        if ((req.status >= 200 && req.status < 300) || req.status == 304) {
          promise.success(req)
        } else
          promise.failure(AjaxException(req))
      }
    }
    req.onprogress = progress

    req.open(method, url)
    req.responseType = responseType
    req.timeout = timeout
    req.withCredentials = withCredentials
    headers.foreach(x => req.setRequestHeader(x._1, x._2))
    req.send(data)
    promise.future
  }

  def get(url: String,
          data: String = "",
          progress: ProgressEvent => Unit = e => (),
          timeout: Int = 0,
          headers: Map[String, String] = Map.empty,
          withCredentials: Boolean = false,
          responseType: String = "") = {
    apply("GET", url, data, progress, timeout, headers, withCredentials, responseType)
  }

}