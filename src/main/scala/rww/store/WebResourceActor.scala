package rww.store

import java.io.StringReader

import akka.actor.{Props, Actor, ActorRef}
import org.scalajs.dom
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.raw.{ProgressEvent, XMLHttpRequest}
import org.w3.banana.RDFOps
import org.w3.banana.io.{JsonLd, NTriples, RDFReader, Turtle}
import rww.Rdf

import scala.concurrent.{ExecutionContext, Future}
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

  import ops._
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

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
  def forceFetch(base: Rdf#URI, proxiedURL: Rdf#URI, sender: ActorRef) = {
    import scalaz.Scalaz._
    import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

    AjaxPlus.get(
      proxiedURL.toString,
      headers = Map("Accept" -> "application/n-triples,application/ld+json;q=0.8,text/turtle;q=0.8"),
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
              case None => Future.failed(new scala.Exception("problem fetching remote resource:" + xhr.statusText))
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
