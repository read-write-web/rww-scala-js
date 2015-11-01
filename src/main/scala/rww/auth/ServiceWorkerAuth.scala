package rww.auth

import org.scalajs.dom.experimental.FetchEvent
import org.scalajs.dom.raw.{Event, ServiceWorker}

import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.annotation.JSExport
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
    thiz.addEventListener("install",install _)
    thiz.addEventListener("fetch",fetch _)
  }

  def install(e: Event) = {
    println("~~~!!> received event = "+e)
    g.console.log(g.Symbol.iterator)
  }

  def fetch(e: FetchEvent) = {
    import scala.scalajs.js.collection.Iterator._
    try {
      println("~~~!!> received fetch event")
      println{"~~~~!!> content-type=" +
        e.request.headers.get("Content-Type").getOrElse(" <-- empty") }

      g.console.log(e.request.headers)
      println("~~~~!!> all headers =" + e.request.headers.iterator())
//      println("~~~~> all headers =" + e.request.headers.iterator().toList)
    }catch {
      case scala.scalajs.js.JavaScriptException(e: scala.scalajs.js.Object) => {
        println("!!next the stack trace of the error itself")
        g.console.log(e)
      }
      case NonFatal(e) => {println("error "+e)}
    }

  }

}


class ServiceWorkerException(msg: String) extends Throwable(msg)
case class ServiceWorkerNotAvailable(msg: String) extends ServiceWorkerException(msg)
case class ServiceWorkerFailed(msg: String) extends ServiceWorkerException(msg)