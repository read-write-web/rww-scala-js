package rww.store

import java.net.{URI => jURI}

import com.viagraphs.idb._
import monifu.concurrent.Scheduler
import org.scalajs.dom.crypto._
import org.scalajs.dom.raw
import rww.log
import upickle.{legacy, Js}

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.ScalaJSDefined

@ScalaJSDefined
abstract class KeyInfo extends js.Object {
  val keyPair: CryptoKeyPair
  val time: Double
  val keyId: UndefOr[String]
  val userId: UndefOr[String]

  def date() = new js.Date(time)
}

object KeyInfo {
  def apply(_keyPair: CryptoKeyPair,
            _time: Double = new js.Date().getTime(),
            _keyId: UndefOr[String] = js.undefined,
            _userId: UndefOr[String] = js.undefined
           ): KeyInfo = new KeyInfo {
    val keyPair = _keyPair
    val time = _time
    val keyId = _keyId
    val userId = _userId
  }
}

/**
  * Created by hjs on 13/11/2015.
  */
object KeyStore {
  val dbName    = "rww.store"
  val storeName = dbName + ".keyInfo"

  //  var keyPromise: Promise[KeyInfo] = _
  //  var db =
  //  override def receive = ???
  //
  //  @throws[Exception](classOf[Exception])
  //  override def preStart() = {
  //
  //    /**
  //      * todo: this code should be moved to an actor, so that it can be the central
  //      * point where all key logic is kept, and it can also save the info to indexDB.
  //      * ( also one would be able to send messages to create a key on a service, etc.. )
  //      */
  //    // example http://www.w3.org/TR/WebCryptoAPI/#examples-signing
  //    val dB = window.indexedDB
  //    if(dB!=null) {
  //      val request = dB.open("rww.store.key")
  //      request.onsuccess = ( e: dom.Event) => {
  //          e.target.result
  //      }
  //    } else {
  //      log("!!! no indexdb here. Won't be able to save keys!","")
  //    }
  //    keyPromise = createKey
  //
  //  }


  implicit val scheduler = Scheduler.trampoline()

  val db = IndexedDb(
    new OpenDb(dbName, Some { (db, e) =>
      val store = db.createObjectStore(storeName, literal("autoIncrement" -> true))
      log("~~~~> created store", store)
      //      store.createIndex("testIndex", "a")
      ()
    }
    ) with Logging)

  val store = db.openStore[Int, KeyInfo](storeName)

  val keyFuture: Future[KeyInfo] = {
    store.get(List(1)).asFuture.flatMap{
      case None => createKey.flatMap((key: KeyInfo) => {
        log(s"~~~~> createdKey now adding it to $store",key.asInstanceOf[js.Object])
        var add = store.add(List(key))
          .doOnStart(x => log("~~~~~> starting to add", x.asInstanceOf[js.Any]))
          .doOnCanceled(log("~~~~~> cancelled adding", "yes"))
          .doOnError(err => log("~~~~~> caught error adding key to store", err.asInstanceOf[js.Object]))
        add.asFuture.flatMap {
          case Some(keyinfo) => Future.successful(keyinfo._2)
          case None => Future.failed(new Throwable("could not create key"))
        }
      })
      case Some(keyinfo) => {
        asTurtle(keyinfo._2.keyPair.publicKey).andThen((s:String)=>
          log("keystore> retrieved public key",s)
        )
        Future.successful(keyinfo._2)
      }
    }
  }


  def createKey: Future[KeyInfo] = {
    import rww._

    val rsaParams = RsaHashedKeyGenParams("RSASSA-PKCS1-v1_5",
      2048,
      new BigInteger(js.Array[Short](1, 0, 1)), //65537
      HashAlgorithm.`SHA-256`).asInstanceOf[KeyAlgorithmIdentifier]


    log("~~~> rsaHashedKeyGenParams", rsaParams.asInstanceOf[js.Dynamic])

    val key = GlobalCrypto.crypto.subtle.generateKey(
      rsaParams,
      false,
      js.Array(KeyUsage.sign)
    ).asInstanceOf[raw.Promise[CryptoKeyPair]]

    key.andThen((ckp: CryptoKeyPair) => {
      log("~install> created key", ckp)
      asTurtle(ckp.publicKey) andThen ( (s: String) => log("~install> created key>",s))
    },
      (e: Any) => log(
        "problem with key creation", e.asInstanceOf[js.Any]
      )
    )
    key.toFuture.map((key: CryptoKeyPair) => KeyInfo(key))
  }

  def asTurtle(key: CryptoKey): raw.Promise[String] =
    GlobalCrypto.crypto.subtle.exportKey(KeyFormat.jwk, key).andThen ((x: js.Any) =>
    {
      import com.github.marklister.base64.Base64._
      val rsapk = x.asInstanceOf[org.scalajs.dom.crypto.RSAPublicKey]
      val ba = rsapk.n.toByteArray(base64Url)
      val modHex = BigInt(1,ba).toString(16)
      val exp = BigInt(rsapk.e.toByteArray(base64Url))
      s"""<#> cert:modulus "$modHex"^^xsd:hexBinary;
          |   cert:exponent $exp""".stripMargin
    }).asInstanceOf[raw.Promise[String]]

}
