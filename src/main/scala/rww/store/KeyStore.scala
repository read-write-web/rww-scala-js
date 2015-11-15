package rww.store

import java.net.{URI => jURI}

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
  val name   : js.UndefOr[String] = js.native
  val date   : js.Date            = js.native
}

object KeyInfo {
  def apply(keyPair: CryptoKeyPair, created: js.Date): KeyInfo =
    literal(
      keyPair = keyPair,
      created = created
    ).asInstanceOf[KeyInfo]

  def apply(keyPair: CryptoKeyPair, created: js.Date, name: String): KeyInfo =
    literal(
      keyPair = keyPair,
      name = name,
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
    case t => upickle.Js.Str("")
  }
  implicit val thing2Reader: legacy.Aliases.R[KeyInfo] = legacy.Reader[KeyInfo] {
    case Js.Obj(x) => "".asInstanceOf[KeyInfo]
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
          .doOnComplete(log("~~~~> added key to store", "yes"))
          .doOnCanceled(log("~~~~~> cancelled adding", "yes"))
          .doOnError(err => log("~~~~~> caught error adding key to store", err.asInstanceOf[js.Object]))
        add.asFuture.flatMap {
          case Some(keyinfo) => Future.successful(keyinfo._2)
          case None => Future.failed(new Throwable("could not create key"))
        }
      })
      case Some(keyinfo) => Future.successful(keyinfo._2)
    }
  }


  def createKey: Future[KeyInfo] = {
    /**
      *
      * @param base64Urlencoded string
      * @return base64 encoded string
      */
    def urlDecode(base64Urlencoded: String): String = {
      val newstr = (base64Urlencoded + "===")
        .substring(0, base64Urlencoded.length + (base64Urlencoded.length % 4))
      newstr.map {
        case '-' => '+'
        case '_' => '/'
        case other => other
      }
    }

    val xxx = RsaHashedKeyGenParams("RSASSA-PKCS1-v1_5",
      2048,
      new BigInteger(js.Array[Short](1, 0, 1)), //65537
      HashAlgorithm.`SHA-256`).asInstanceOf[KeyAlgorithmIdentifier]


    log("~~~> rsaHashedKeyGenParams", xxx.asInstanceOf[js.Dynamic])
    val promise = Promise[CryptoKeyPair]()

    val key = GlobalCrypto.crypto.subtle.generateKey(
      xxx,
      false,
      js.Array(KeyUsage.sign)
    ).asInstanceOf[raw.Promise[CryptoKeyPair]]

    key.andThen((ckp:CryptoKeyPair)=>promise.success(ckp),
      (err: Any)=>promise.failure(err.asInstanceOf[java.lang.Throwable])
    )

    key.andThen((ckp: CryptoKeyPair) => {
      log("~install> created key", ckp)
      GlobalCrypto.crypto.subtle.exportKey(KeyFormat.jwk, ckp.publicKey) andThen { x: js.Any =>
        import com.github.marklister.base64.Base64._
        val pk = x.asInstanceOf[Dictionary[String]]
        log("exported created key to ", x)
        val dec = urlDecode(pk("n"))
        println("~install> decoded:" + dec)
        val ba = dec.toByteArray
        log("~install> ba=", ba.asInstanceOf[js.Array[Byte]])
        val modHex = BigInt(ba).abs.toString(16) //could also publish as xsd:base64Binary
      val exp = BigInt(urlDecode(pk("e")).toByteArray)
        println(
          s"""
             | <#> cert:modulus "$modHex"^^xsd:hexBinary;
             |     cert:exponent $exp
           """.stripMargin)
      }
    },
      (e: Any) => log(
        "problem with key creation", e.asInstanceOf[js.Any]
      )
    )
    promise.future.map((key: CryptoKeyPair) => KeyInfo(key, new js.Date()))
  }
}
