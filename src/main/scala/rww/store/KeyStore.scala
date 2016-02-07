package rww.store

import java.net.{URI => jURI}

import com.viagraphs.idb._
import monifu.concurrent.Scheduler
import org.scalajs.dom.crypto._
import org.scalajs.dom.raw
import rww.log

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

@ScalaJSDefined
abstract class WebIdInfo extends js.Object {
  val webid: String
  val verified: Boolean
}

object WebIdInfo {
  def apply(_webid: String, _verified: Boolean): WebIdInfo = new WebIdInfo {
    val verified: Boolean = _verified
    val webid   : String  = _webid
  }
}

/**
  * Created by hjs on 13/11/2015.
  */
object KeyStore {
  val dbName = "rww.store"
  val keyInfoStoreName = dbName+".keyInfo"
  val webidInfoStoreName = dbName+".webidInfo"

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
      val store = db.createObjectStore(keyInfoStoreName, literal("autoIncrement" -> true))
      log("~~~~> created store", store)
      //      store.createIndex("testIndex", "a")
      ()
    }
    ) with Logging)

  val keyInfoStore = db.openStore[Int, KeyInfo](keyInfoStoreName)

  val webIdInfoStore = db.openStore[Int, WebIdInfo](webidInfoStoreName)

  val keyFuture: Future[KeyInfo] = {
    keyInfoStore.get(List(1)).asFuture.flatMap{
      case None => {
        // we try to store the key in the preferred storage space of the user
        // for a key we could also just store it anywhere in a distributed hash table as a fallback
        // as it does not actually matter where the key is located for privacy reasons.
        // the client actually just needs to keep track of the WebID associated with the key

        //1. find storage space by searching for ws:storage link from webid


        //2. find if there is storage space in that domain if so use it
        // this can be done by directly doing a GET on the subfolder finding the acl
        // the acl should give that agent rw access
        // this of course means that there has to be other ways of authenticating


        //3. if the above exists then create a key
        createKey.flatMap((key: KeyInfo) => {
          log(s"~~~~> createdKey now adding it to $keyInfoStore",key.asInstanceOf[js.Object])
          var add = keyInfoStore.add(List(key))
            .doOnStart(x => log("~~~~~> starting to add", x.asInstanceOf[js.Any]))
            .doOnCanceled(log("~~~~~> cancelled adding", "yes"))
            .doOnError(err => log("~~~~~> caught error adding key to store", err.asInstanceOf[js.Object]))
          add.asFuture.flatMap {
            case Some(keyinfo) => Future.successful(keyinfo._2)
            case None => Future.failed(new Throwable("could not create key"))
          }
        })
        //4.  save the public key in a the container using a Slug

        //5. make sure the acl makes the key world readable
        // this is not necessary but it helps authenticate on different apps

      }
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
