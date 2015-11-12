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



  // example http://www.w3.org/TR/WebCryptoAPI/#examples-signing
  lazy val key: Promise[js.Any] ={
        val xxx = RsaHashedKeyGenParams("RSASSA-PKCS1-v1_5",
          2048,
          new Uint8Array(js.Array[Short](1, 0, 1)), //65537
          HashAlgorithm.`SHA-256`).asInstanceOf[KeyAlgorithmIdentifier]


    log("~~~> rsaHashedKeyGenParams", xxx.asInstanceOf[js.Dynamic])
    GlobalCrypto.crypto.subtle.generateKey(
      xxx,
      false,
      js.Array(KeyUsage.sign)
    )
  }

  /**
    *
    * @param base64Urlencoded string
    * @return base64 encoded string
    */
  def urlDecode(base64Urlencoded: String): String = {
    val newstr = (base64Urlencoded+"===")
      .substring(0,base64Urlencoded.length+(base64Urlencoded.length % 4))
    newstr.map{
      case '-' => '+'
      case '_' => '/'
      case other => other
    }
  }

  @JSExport
  def run(thiz: ServiceWorker): Unit = {
    //should also be able to access thiz as js.Dynamic.global
    thiz.addEventListener("install",installListener _)
    thiz.addEventListener("fetch",fetchListener _)
  }

  def installListener(e: Event) = {
    log("~install> received event = ",e)
    key.andThen((key: js.Any) => {
      log("~install> created key", key)
      val ckp = key.asInstanceOf[CryptoKeyPair]
      GlobalCrypto.crypto.subtle.exportKey(KeyFormat.jwk,ckp.publicKey) andThen { x: js.Any =>
        import com.github.marklister.base64.Base64._
        val pk = x.asInstanceOf[js.Dictionary[String]]
        log("exported created key to ",x)
        val dec =urlDecode(pk("n"))
        println("~install> decoded:"+dec)
        val ba =  dec.toByteArray
        log("~install> ba=",ba.asInstanceOf[js.Array[Byte]])
        val modHex = BigInt(ba).abs.toString(16) //could also publish as xsd:base64Binary
        val exp = BigInt(urlDecode(pk("e")).toByteArray)
        println(
          s"""
          | <#> cert:modulus "$modHex"^^xsd:hexBinary;
          |     cert:exponent $exp
        """.
            stripMargin)
        }
    },
      (e: Any)=>log("problem with key creation",e.asInstanceOf[js.Any]))

  }

  def isSignature(ah: String): Boolean = ah.toLowerCase.startsWith("signature")

  def sign(request: Request, response: Response): Promise[Any] = {
     val x = for {
       ah<-response.headers.get("WWW-Authenticate")
       if (isSignature(ah))
     } yield {
       val method = request.method.toString.toLowerCase
       val path = request.url
       val host = request.headers.get("host")
//       val user = request.headers.get("User")
       val now = new js.Date().toISOString()
       val nr = new Request(request)
       nr.headers.set("Signature-Time",now)
       val toSign =
         s"""(request-target) $method $path
            |host: $host
            |signature-time: $now
            |""".stripMargin
       //1. calculate signature
       key.andThen { x: js.Any =>
         val ckp: CryptoKeyPair = x.asInstanceOf[CryptoKeyPair]
         import org.scalajs.dom.crypto.arrayBuffer2BufferSource
         log("~sign>private key",ckp.privateKey)
         log("~sign>pubkey algorithm",ckp.privateKey.algorithm)
         log("~sign>buffer source",new Uint16Array(toSign.toCharArray.asInstanceOf[js.Array[Char]]).buffer)
         val s = GlobalCrypto.crypto.subtle.sign(
           ckp.publicKey.algorithm,
           ckp.privateKey,
           arrayBuffer2BufferSource(new Uint16Array(toSign.toCharArray.asInstanceOf[js.Array[Char]]).buffer)
         )
         s.andThen { sig: js.Any=>
           log("~sign> in Sig. received",sig)
           val sigHdr =
             s"""keyId="https://joe.example:8443/2013/key#",algorithm="rsa-sha256",
                 |headers="(request-target) host signature-time",
                 |signature="${sig}"
                 |"""
           log("~sign>returning sig",sigHdr)
           nr.headers.set("Signature",sigHdr)
           nr
           }
       }
       //2. add signature
     }
    x.getOrElse(new Promise((resolve: js.Function1[Unit, Any], reject: js.Function1[Any, Any])=>response))

  }

  def fetchListener(e: FetchEvent): Unit = {
    log(s"~~sw fetch> ${e.request.method} <${e.request.url}>:", e.request)
    e.respondWith {
      val promise = fetch(e.request).andThen{ response: Response =>
        log(s"~~sw fetch response> ${response.status} <${response.url}>. headers=",
                      rww.headerToString(response.headers))
        response
      }
        promise.asInstanceOf[Promise[Response]]
    }
//      try {
//        //see issue https://github.com/scala-js/scala-js-dom/issues/164
//        val p: Promise[Any] = fetch(e.request).andThen({ response: Response =>
//          log(s"~~sw fetch response> ${response.status} <${response.url}>. headers=",
//            rww.headerToString(response.headers))
//
//          response.status match {
//            case 401 if response.headers.has("WWW-Authenticate") => {
//              log("~~fetchListerner> intercepted 401", response.url)
//              //              response
//              sign(e.request, response)
//              //              fetch(sign(response))
//            }
//            case r => {
//              e.request
//            }
//            //            case _ => response
//          }
//        })
//        p.asInstanceOf[Promise[Response]]
//      } catch {
//        case scala.scalajs.js.JavaScriptException(e: scala.scalajs.js.Object) => {
//          log("~>next the stack trace of the error itself", e)
//          Promise.reject("~~sw fetch error> "+e)
//        }
//        case NonFatal(e) => Promise.reject("~~ sw fetch error>"+ e.toString)
//      }
//    }
  }

}


class ServiceWorkerException(msg: String) extends Throwable(msg)
case class ServiceWorkerNotAvailable(msg: String) extends ServiceWorkerException(msg)
case class ServiceWorkerFailed(msg: String) extends ServiceWorkerException(msg)