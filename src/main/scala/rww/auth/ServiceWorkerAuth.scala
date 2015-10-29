package rww.auth

import org.scalajs.dom.raw.{Event, ServiceWorker}

import scala.scalajs.js.annotation.JSExport

/**
 * Created by hjs on 29/10/2015.
 */
@JSExport
object ServiceWorkerAuth {

  @JSExport
  def run(thiz: ServiceWorker): Unit = {
    thiz.addEventListener("install",install _)
  }

  def install(e: Event) = {
    println("received event = "+e)
  }

}


class ServiceWorkerException(msg: String) extends Throwable(msg)
case class ServiceWorkerNotAvailable(msg: String) extends ServiceWorkerException(msg)
case class ServiceWorkerFailed(msg: String) extends ServiceWorkerException(msg)