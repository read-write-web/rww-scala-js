package rww.auth

import org.scalajs.dom.crypto._
import org.scalajs.dom.experimental.{FetchEvent, _}
import org.scalajs.dom.raw.{Event, Promise, ServiceWorker}
import rww.log

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.typedarray.Uint8Array
import scala.util.control.NonFatal
import scala.scalajs.js.`|`

/**
 * Created by hjs on 29/10/2015.
  * https://developer.mozilla.org/en-US/docs/Web/API/Service_Worker_API/Using_Service_Workers
 */
@JSExport
object ServiceWorkerAuth {



  // example http://www.w3.org/TR/WebCryptoAPI/#examples-signing
  lazy val key: Promise[js.Any] ={
    val xxx =       RsaHashedKeyGenParams(
      2048,
      new Uint8Array(js.Array[Short](1,0,1)), //65537
      HashAlgorithm.`SHA-256`).asInstanceOf[AlgorithmIdentifier]
    log("~~~> rsaHashedKeyGenParams",xxx.asInstanceOf[js.Dynamic])
    GlobalCrypto.crypto.subtle.generateKey(
      xxx,
      false,
      js.Array(KeyUsages.sign,KeyUsages.verify)
    )
  }

  @JSExport
  def run(thiz: ServiceWorker): Unit = {
    //should also be able to access thiz as js.Dynamic.global
    thiz.addEventListener("install",installListener _)
    thiz.addEventListener("fetch",fetchListener _)
  }

  def installListener(e: Event) = {
    log("~~~!!> received event = ",e)
    key.andThen((key: js.Any) => log("~~~> created key", key),
      (e: Any)=>log("problem with key creation",e.asInstanceOf[js.Any]))

  }

  def isSignature(ah: String): Boolean = ah.toLowerCase.startsWith("signature")

  def sign(response: Response): Request = {
     for {
       ah<-response.headers.get("WWW-Authenticate")
       if (isSignature(ah))
     } yield {

     }
    ???
  }

  def fetchListener(e: FetchEvent) = {
    try {
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


}


class ServiceWorkerException(msg: String) extends Throwable(msg)
case class ServiceWorkerNotAvailable(msg: String) extends ServiceWorkerException(msg)
case class ServiceWorkerFailed(msg: String) extends ServiceWorkerException(msg)