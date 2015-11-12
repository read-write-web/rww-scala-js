package rww.store

import java.io.StringReader

import akka.actor.{Actor, ActorRef, Props}
import org.scalajs.dom
import org.scalajs.dom.experimental.{Response => HttpResponse, _}
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.raw.{ProgressEvent, Promise, XMLHttpRequest}
import org.w3.banana.RDFOps
import org.w3.banana.io.{JsonLd, NTriples, RDFReader, Turtle}
import rww.{Rdf, log}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.util.{Failure, Success, Try}


object WebResourceActor {
  //see http://doc.akka.io/docs/akka/2.3.11/scala/actors.html#props
  def props(u: Rdf#URI)(implicit
    ops: RDFOps[Rdf],
    rdrNT: RDFReader[Rdf, Try, NTriples],
    rdrTurtle: RDFReader[Rdf, Future, Turtle],
    rdrJSONLD: RDFReader[Rdf, Future, JsonLd]
  ): Props = {
    Props(new WebResourceActor(u)(ops,rdrNT,rdrTurtle,rdrJSONLD))
  }
}

/**
 * Created by hjs on 19/06/2015.
 */
class WebResourceActor(
  resourceName: Rdf#URI
)(implicit
  ops: RDFOps[Rdf],
  rdrNT: RDFReader[Rdf, Try, NTriples],
  rdrTurtle: RDFReader[Rdf, Future, Turtle],
  rdrJSONLD: RDFReader[Rdf, Future, JsonLd]
) extends Actor {

  val rdfMimeTypes = "application/n-triples,application/ld+json;q=0.8,text/turtle;q=0.8"

  import ops._

  var state: RequestState = UnRequested(resourceName)

  def update(newState: RequestState,sender: ActorRef) = {
    state = newState
    sender ! newState  // one could extend this to a collection of listeners
  }


  override def receive = {
    case g@Get(base, proxy, mode) => forceFetch(base, proxy, context.sender())
    case Update(base, remove, add) => vsimplePatch(base,remove,add)
    case rs: RequestState =>  state = rs //for dealing with redirects
  }

  protected
  def forceFetch(base: Rdf#URI, proxiedURL: Rdf#URI, sender: ActorRef): Unit = {
    import org.scalajs.dom.experimental.fetch

    def consume(reader: ReadableStreamReader): Promise[Unit] = {
      var data: String = ""
      new Promise[Unit](
        (resolve: js.Function1[Unit, Any], reject: js.Function1[Any, Any]) => {
          def pump(): Unit = {
            reader.read().andThen { chunk: Chunk =>
              if (chunk.done) {
                resolve()
                ()
              }
              else {
                pump()
              }
            }.recover(reject)
          }
          pump()
        }
      )
    }

    def cacheStateOf(response: HttpResponse)(finalURLToState: Rdf#URI => RequestState): Unit = {
      // responseURL is quite new. See https://xhr.spec.whatwg.org/#the-responseurl-attribute
      // hence need for UndefOr ( but according to latest xhr spec it should return "" not undefined )
      // todo: to remove dynamic use need to fix https://github.com/scala-js/scala-js-dom/issues/111
      val finalURL = {
        val redirectURLstr = response.url
        val redirectURL = URI(redirectURLstr)
        if (redirectURL == proxiedURL || redirectURLstr == "")
          base
        else
          redirectURL
      }
      if (finalURL != base) {
        update(Redirected(base, finalURL), sender)
        //todo: need to send back a request to change the state of the final URL
      } else update(finalURLToState(finalURL), sender)

    }

    val ri = js.Dynamic.literal(
      headers = js.Dictionary(  "Accept" -> rdfMimeTypes ),
      requestCache = RequestCache.reload
//      window = null // should work in the future
    )

    log("request init",ri)
    fetch(proxiedURL.toString, ri) andThen { res: HttpResponse =>
      import scala.scalajs.js.collection.JSIterator._
      //      consume(res.body.getReader(),res.headers.get("Content-Length"))
      res.text() andThen { txt: String =>
        if (res.ok) {
          import org.w3.banana.TryW

          val rh = res.headers.get("Content-Type").toOption
          println(s"<$proxiedURL> content is ${txt.substring(0, 80)}")
          val reader = new StringReader(txt)
          for {
            g <- rh.map(_.takeWhile(_ != ';').trim.toLowerCase) match {
              case Some("application/n-triples") => rdrNT.read(reader, base.toString).asFuture
              case Some("application/ld+json") => rdrJSONLD.read(reader, base.toString)
              case Some("text/turtle") => rdrTurtle.read(reader, base.toString)
              case Some(other) => Future.failed(new scala.Exception("could not find parser for " + other))
              case None => Future.failed(
                new scala.Exception("missing content type on response - unable to parse response"
                ))
            }
          } yield {
            cacheStateOf(res) { graphUrl: Rdf#URI =>
              Ok(res.status,
                graphUrl,
                res.headers.iterator().map(a => a.mkString(": ")).toList.mkString("\n"),
                txt,
                Success(g)
              )
            }
          }
        } else {
          log(s"~fetch> Failure for <$base> with code ${res.status}. headers:",
            rww.headerToString(res.headers))
          //todo: deal with redirects here too
          cacheStateOf(res)(
            url => HttpError(url,
              code = res.status,
              headers = res.headers.iterator().map(a => a.mkString(": ")).toList.mkString("\n"),
              body = txt)
          )
        }
      } recover { e: Any =>
        log("~~> in recover from fetch",e.asInstanceOf[js.Any])
        cacheStateOf(res)(
          url => HttpError(url,
            code = res.status,
            headers = res.headers.iterator().map(a => a.mkString(": ")).toList.mkString("\n"),
            body = e.toString)
        )
      }
    }
  }



  protected
  def forceFetchAjax(base: Rdf#URI, proxiedURL: Rdf#URI, sender: ActorRef) = {
    import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
    import scalaz.Scalaz._

    AjaxPlus.get(
      proxiedURL.toString,
      headers = Map("Accept" -> rdfMimeTypes),
      progress = (ev: ProgressEvent) => {
        if (ev.lengthComputable && ev.loaded != ev.total)
          sender ! Downloading(base, (ev.lengthComputable).option(ev.loaded / ev.total))
      }
    ) onComplete { xhrTry: Try[dom.XMLHttpRequest] =>

      def cacheStateOf(xhr: XMLHttpRequest)(finalURLToState: (Rdf#URI) => RequestState): Unit = {
        // responseURL is quite new. See https://xhr.spec.whatwg.org/#the-responseurl-attribute
        // hence need for UndefOr ( but according to latest xhr spec it should return "" not undefined )
        // todo: to remove dynamic use need to fix https://github.com/scala-js/scala-js-dom/issues/111
        val redirectURLOpt = xhr.asInstanceOf[js.Dynamic].responseURL.asInstanceOf[UndefOr[String]]
        val finalURL = redirectURLOpt.map { redirectURLstr =>
          val redirectURL = URI(redirectURLstr)
          if (redirectURL == proxiedURL || redirectURLstr == "")
            base
          else
            redirectURL
        } getOrElse {
          base
        }
        if (finalURL != base) {
          update(Redirected(base, finalURL),sender)
          //todo: need to send back a request to change the state of the final URL
        } else update(finalURLToState(finalURL),sender)

      }

      xhrTry match {
        case Success(xhr) => {
          import org.w3.banana.TryW
          val rh = Option(xhr.getResponseHeader("Content-Type"))
          val reader = new StringReader(xhr.responseText)
          for {
            g <- rh.map(_.takeWhile(_ != ';').trim.toLowerCase) match {
              case Some("application/n-triples") => rdrNT.read(reader, base.toString).asFuture
              case Some("application/ld+json") => rdrJSONLD.read(reader, base.toString)
              case Some("text/turtle") => rdrTurtle.read(reader, base.toString)
              case Some(other) => Future.failed(new scala.Exception("could not find parser for " + other))
              case None => Future.failed(
                new scala.Exception(
                  "missing content type on response - unable to parse data!"
                ))
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
          cacheStateOf(xhr)(
            url => HttpError(url, code = xhr.status, headers = xhr.getAllResponseHeaders(), body = xhr.responseText))
        }
        case Failure(other) => {
          //todo: deal correctly with redirects here too if it makes sense
          println(s"Other failure! " + other.toString)
          update(HttpError(resourceName,code = 477, headers = "", body = other.toString),sender)
        }
      }
    }
  }

  protected
  def vsimplePatch(url: Rdf#URI, remove: Rdf#Triple, add: Rdf#Triple): Unit = {
    state match {
      case Ok(code, url, headers, body, parsed) => {
        //todo: we should of course send a PATCH to the server here
        update(
          Ok(code, url, headers, body, parsed.map { graph =>
            graph.diff(Graph(remove)) union Graph(add)
          }),
          context.sender
        )
      }
//      case other => other //send back error message. we can't update
    }
  }


}
