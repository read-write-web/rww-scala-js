package rww.store

import java.net.{URI => jURI}

import com.github.marklister.base64.Base64
import com.viagraphs.idb._
import monifu.concurrent.Scheduler
import org.scalajs.dom.crypto._
import org.scalajs.dom.raw
import rww.log
import upickle.{Js, legacy}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.Dictionary
import scala.scalajs.js.Dynamic.literal


@js.native
trait KeyInfo extends js.Object {
  val keyPair: CryptoKeyPair      = js.native
  val date   : js.Date            = js.native
  val keyId  : js.UndefOr[String] = js.native
  val userId : js.UndefOr[String] = js.native
}

object KeyInfo {
  def apply(keyPair: CryptoKeyPair, created: js.Date): KeyInfo =
    literal(
      keyPair = keyPair,
      created = created
    ).asInstanceOf[KeyInfo]

  def apply(keyPair: CryptoKeyPair, created: js.Date, keyId: String): KeyInfo =
    literal(
      keyPair = keyPair,
      keyId = keyId,
      created = created
    ).asInstanceOf[KeyInfo]

  def apply(keyPair: CryptoKeyPair, created: js.Date, keyId: String, userId: String): KeyInfo =
    literal(
      keyPair = keyPair,
      keyId = keyId,
      userId = userId,
      created = created
    ).asInstanceOf[KeyInfo]

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

  // these two implicits don't actually ever get used, but they are needed to allow
  // the hacked scalajs-rx-idb to compile

  implicit val thing2Writer: legacy.Aliases.W[KeyInfo] = upickle.legacy.Writer[KeyInfo] {
    case t => Js.Str("") //<- ignore //todo: Js.Unserializable(t.asInstanceOf[js.Object])
  }
  implicit val thing2Reader: legacy.Aliases.R[KeyInfo] = legacy.Reader[KeyInfo] {
    case _ => "".asInstanceOf[KeyInfo] //<- ignore
  }
  implicit val scheduler                               = Scheduler.trampoline()

  val db = IndexedDb(
    new OpenDb(dbName, Some { (db, e) =>
      val store = db.createObjectStore(storeName, literal("autoIncrement" -> true))
      log("~~~~> created store", store)
      //      store.createIndex("testIndex", "a")
      ()
    }
    ) with Logging)

  val store = db.openStore[Int, KeyInfo](storeName)(
    legacy.IntRW,
    legacy.IntRW,
    ValidKey.IntOk,
    thing2Writer,
    thing2Reader
  )

  val keyFuture: Future[KeyInfo] = {
    store.get(List(1)).asFuture.flatMap{
      case None => createKey.flatMap((key: KeyInfo) => {
        log(s"~~~~> createdKey now adding it to $store", key)
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
    key.toFuture.map((key: CryptoKeyPair) => KeyInfo(key, new js.Date()))
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
