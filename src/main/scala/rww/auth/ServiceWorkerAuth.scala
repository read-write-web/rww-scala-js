package rww.auth

import org.scalajs.dom.experimental.{FetchEvent, _}
import org.scalajs.dom.raw.{Event, Promise, ServiceWorker}

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.collection.JSIterator
import scala.util.control.NonFatal

/**
 * Created by hjs on 29/10/2015.
  * https://developer.mozilla.org/en-US/docs/Web/API/Service_Worker_API/Using_Service_Workers
 */
@JSExport
object ServiceWorkerAuth {

  @JSExport
  def run(thiz: ServiceWorker): Unit = {
    //should also be able to access thiz as js.Dynamic.global
    thiz.addEventListener("install",installListener _)
    thiz.addEventListener("fetch",fetchListener _)
  }

  def installListener(e: Event) = {
    log("~~~!!> received event = ",e)
    log("Symbol.iterator",JSIterator.iteratorSymbol)
  }

  def isSignature(ah: String): Boolean = ah.toLowerCase.startsWith("signature")

  def sign(response: Response): Request = {
     for {
       ah<-response.headers.get("WWW-Authenticate")
       if (isSignature(ah))
     } yield {
//       response.clone()
     }
    ???
  }

  def fetchListener(e: FetchEvent) = {
    import scala.scalajs.js.collection.JSIterator._
    try {
      log("~> received fetch event. Request is:", e.request)
      log("~> headers:", e.request.headers)
      log("~> content-type=", e.request.headers.get("Accept"))

      println("~>all headers ="+ JSIterator.toIterator[js.Array[String]](e.request.headers.iterator()).toList)
      e.respondWith {
        //see issue https://github.com/scala-js/scala-js-dom/issues/164
        val p: Promise[Any] = fetch(e.request.url).andThen({ response: Response =>
          response.status match {
            case 401 if response.headers.has("WWW-Authenticate") => {
              println(s"~~>> intercepted 401 <${response.url}>")
              response
              //              fetch(sign(response))
            }
            case r => {
              println(s"~~>> response $r for <${response.url}>")
              response
            }
            //            case _ => response
          }
        })
        p.asInstanceOf[Promise[Response]]
      }
      //      log("~~>all headers =" ,e.request.headers.iterator().toList)
      //      println("~~~~> all headers =" + e.request.headers.iterator().toList)
    } catch {
      case scala.scalajs.js.JavaScriptException(e: scala.scalajs.js.Object) => {
        log("~>next the stack trace of the error itself", e)
      }
      case NonFatal(e) => log("~>error ", e.toString)
    }

  }

  def log(msg: String,err: js.Any): Unit = {
    g.console.log(msg,err)
  }
}


class ServiceWorkerException(msg: String) extends Throwable(msg)
case class ServiceWorkerNotAvailable(msg: String) extends ServiceWorkerException(msg)
case class ServiceWorkerFailed(msg: String) extends ServiceWorkerException(msg)