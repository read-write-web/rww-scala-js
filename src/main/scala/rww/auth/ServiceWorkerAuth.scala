package rww.auth

import org.scalajs.dom.crypto.{GlobalCrypto, _}
import org.scalajs.dom.experimental.{FetchEvent, _}
import org.scalajs.dom.raw.{Event, Promise, ServiceWorker}
import rww.log

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.typedarray.{Uint16Array, Uint8Array}
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
    log("~install> received event = ",e)
  }



  def fetchListener(e: FetchEvent): Unit = {
    log(s"~~sw fetch> ${e.request.method} <${e.request.url}>:", e.request)
    //    e.respondWith {
    //      val promise = fetch(e.request).andThen{ response: Response =>
    //        log(s"~~sw fetch response> ${response.status} <${response.url}>. headers=",
    //                      rww.headerToString(response.headers))
    //        response
    //      }
    //        promise.asInstanceOf[Promise[Response]]
  }

}


class ServiceWorkerException(msg: String) extends Throwable(msg)
case class ServiceWorkerNotAvailable(msg: String) extends ServiceWorkerException(msg)
case class ServiceWorkerFailed(msg: String) extends ServiceWorkerException(msg)