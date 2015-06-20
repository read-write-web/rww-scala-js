package rww.store

import java.net.{URI => jURI}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.scalajs.dom
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.raw.ProgressEvent
import org.w3.banana.RDFOps
import org.w3.banana.io._
import rww.Rdf
import rx.{Rx, Var}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

sealed trait RequestState {
  def url: Rdf#URI
}
sealed trait Response {
  import Response._
  def code: Int
  def headers: String
  def body: String
  def header(name: String) = {
    headers.lines.collect{
      case attVal(att,value) if name.equalsIgnoreCase(att.trim) => value.trim
    }.toList
  }
}
object Response {
  val attVal = "([^:]+):(.*)".r
}

//trait HttpAction
//case class Post extends HttpAction
//case class Patch extends HttpAction
//case class Delete extends HttpAction
//case class Query extends HttpAction

case class UnRequested(url: Rdf#URI) extends RequestState
case class Downloading(url: Rdf#URI, percentage: Option[Double]=None) extends RequestState
case class Redirected(url: Rdf#URI, to: Rdf#URI) extends RequestState
case class HttpError(url: Rdf#URI, code: Int, headers: String, body: String) extends RequestState with Response
case class Ok(code: Int,
              url: Rdf#URI,
              headers: String,
              body: String,
              parsed: Try[Rdf#Graph] ) extends RequestState with Response

//for things that still need to be pushed remotely.
//case class ToDo(url: Rdf#URI,action: HttpAction) extends RequestState


sealed trait CacheMode
/** Only Fetch from Cache */
object CacheOnly extends CacheMode
/** Fetch if not in the cache */
object UnlessCached extends CacheMode
/** Force a fetch even if something is in the cache */
object Force extends CacheMode


//commands
case class Get(uri: Rdf#URI, proxy: Rdf#URI, mode: CacheMode)
case class Update(uri: Rdf#URI, oldTriple: Rdf#Triple, newTriple: Rdf#Triple)
case class Redirect(from: Rdf#URI, to: Rdf#URI, state: RequestState)

object WebUIDB {
  def apply(
    proxy: Rdf#URI => Rdf#URI = (u: Rdf#URI) => u
  )(implicit
    ops: RDFOps[Rdf],
    rdrNT: RDFReader[Rdf, Try, NTriples],
    rdrTurtle: RDFReader[Rdf, Future, Turtle],
    rdrJSONLD: RDFReader[Rdf, Future, JsonLd]
  ) = {
    val db = new WebUIDB(proxy)
    WebActor.system.actorOf(Props(new WebActor(db)))
    db
  }

}

/**
 * WebUIDB objects are those that are known to UI elements.
 * They hide the relations to actors.
 *
 * @param proxy a function to map a URI to a proxy URI through which requests
 *              can be made as if for the original one.
 */
class WebUIDB(
  proxy: Rdf#URI => Rdf#URI
)(implicit
  ops: RDFOps[Rdf],
  rdrNT: RDFReader[Rdf, Try, NTriples],
  rdrTurtle: RDFReader[Rdf, Future, Turtle],
  rdrJSONLD: RDFReader[Rdf, Future, JsonLd]
) {
  import ops._

  var actorRef: ActorRef = _
  def setWebActor(ref: ActorRef) = {
    println("WebUIDB setting actorRef="+ref)
    actorRef = ref
  }

  //todo: use WeakMap https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/WeakMap
  //use code from https://github.com/scala-js/scala-js/blob/master/javalanglib/src/main/scala/java/lang/System.scala
  val cache = {
    val c = new collection.mutable.HashMap[Rdf#URI, Var[RequestState]]()
    c.withDefault{key=>
      val v: Var[RequestState] = new Var(UnRequested(key))
      c.put(key,v) //we still need to actually add the value into the collection
      v
    }
  }

  //note: it is the caller's responsibility here to follow redirects
  def fetch(url: Rdf#URI, mode: CacheMode = UnlessCached, counter: Int = 1): Rx[RequestState] = {
    // for AJAX calls http://lihaoyi.github.io/hands-on-scala-js/#dom.extensions
    //and for CORS see http://www.html5rocks.com/en/tutorials/cors/
    println(s"fetching $url")
    val base = url.fragmentLess
    val valueRx = cache(base)
    valueRx() match {
      case Redirected(_,to) => if( counter >= 0) fetch(to, mode, counter-1) else valueRx
      case _ if mode == CacheOnly => valueRx  // we still want to follow redirects
      case UnRequested(url) => {
        actorRef ! Get(base,  proxy(base), mode)
        valueRx
      }
      case _ => valueRx
    }
    //todo: wrap Var in Rx so that no client can change the cache
    //Rx{ valueRx() }
    //but see discussion https://github.com/lihaoyi/scala.rx/issues/38
  }

  //just to get going
  //should it return the new graph?
  def vsimplePatch(url: Rdf#URI, add: Rdf#Triple, remove: Rdf#Triple): Unit = {
    actorRef ! Update(url,remove,add)
  }

}


object WebActor {
  implicit val system = ActorSystem("web",ConfigFactory.parseString(
    """{
      |"akka": {
      |  "actor": {
      |    "debug": {
      |      "receive" : "on"
      |    }
      |  }
      |}
      |}
    """.stripMargin))
}

/**
 *
 * The WebActor is an object that runs in the UI thread and that manages all the other
 * actors that deal with fetching, caching, editing resources.
 *
 * The actor transforms potentially remote actor requests into lighter weight Rx.Rx,
 * Future or Rx.Observables that the UI components can deal with.
 *
 */
class WebActor(
  db: WebUIDB
)(implicit
  ops: RDFOps[Rdf],
  rdrNT: RDFReader[Rdf, Try, NTriples],
  rdrTurtle: RDFReader[Rdf, Future, Turtle],
  rdrJSONLD: RDFReader[Rdf, Future, JsonLd]
) extends Actor {

  import WebActor._

  println("WebActor.ops="+ops)

  @throws[Exception](classOf[Exception])
  override def preStart() = {
    db.setWebActor(context.self)
  }

  private val resourceActors = new collection.mutable.HashMap[Rdf#URI,ActorRef]

  //todo: garbage collection of actors, to limit memory usage
  def actor(u: Rdf#URI): ActorRef =
    resourceActors.get(u).getOrElse {
      val newRA = system.actorOf(Props(classOf[WebResourceActor], u))
      resourceActors += u -> newRA
      newRA
    }


  //  def methodsFor(resource: Rdf#URI, )
  //
  override def receive = {
    //
    //commands coming from the UI
    //
    case g@Get(uri, _, _) => actor(uri) ! g
    case u@Update(uri, _, _) => actor(uri) ! u
    //
    //commands coming from the resource actors
    //
    case r@Redirect(from, to, newstate) => {
      db.cache(from).update(Redirected(from, to))
      actor(to) ! newstate
    }
    case state: RequestState => db.cache(state.url).update(state)
  }
}

object AjaxPlus {


  def apply(method: String,
            url: String,
            data: String,
            progress: ProgressEvent => Unit = e => (),
            timeout: Int,
            headers: Map[String, String],
            withCredentials: Boolean,
            responseType: String): Future[dom.XMLHttpRequest] = {
    val req = new dom.XMLHttpRequest()
    val promise = Promise[dom.XMLHttpRequest]()

    req.onreadystatechange = {(e: dom.Event) =>
      if (req.readyState.toInt == 4){
        req.onprogress = null
        if ((req.status >= 200 && req.status < 300) || req.status == 304) {
          promise.success(req)
        } else
          promise.failure(AjaxException(req))
      }
    }
    req.onprogress = progress

    req.open(method, url)
    req.responseType = responseType
    req.timeout = timeout
    req.withCredentials = withCredentials
    headers.foreach(x => req.setRequestHeader(x._1, x._2))
    req.send(data)
    promise.future
  }

  def get(url: String,
          data: String = "",
          progress: ProgressEvent => Unit = e => (),
          timeout: Int = 0,
          headers: Map[String, String] = Map.empty,
          withCredentials: Boolean = false,
          responseType: String = "") = {
    apply("GET", url, data, progress, timeout, headers, withCredentials, responseType)
  }

}