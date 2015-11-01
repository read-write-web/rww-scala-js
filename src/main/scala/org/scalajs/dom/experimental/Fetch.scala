package org.scalajs.dom.experimental

import org.scalajs.dom.crypto.BufferSource
import org.scalajs.dom.raw.{FormData, MessagePort, Promise}
import org.scalajs.dom.{Blob, Event}

import scala.scalajs.js
import scala.scalajs.js.|
import scala.scalajs.js.typedarray.ArrayBuffer
import scala.scalajs.js.{Dictionary, UndefOr, collection}

/**
  * The Request interface of the Fetch API represents a resource request.
  *
  * MDN
  * see https://fetch.spec.whatwg.org/#request-class
  */
@js.native
class Request(input: String | Request, init: RequestInit = null) extends js.Object {

  /**
    * Contains the request's method (GET, POST, etc.)
    */
  def method: HttpMethod = js.native

  /**
    * Contains the URL of the request.
    */
  def url: String = js.native //should be USVString

  /**
    * Contains the associated Headers object of the request.
    */
  def headers: Headers = js.native

  def destination: RequestDestination = js.native

  def referrer: String = js.native

  //should be USVString
  def referrerPolicy: ReferrerPolicy = js.native

  def mode: RequestMode = js.native

  def credentials: RequestCredentials = js.native

  def cache: RequestCache = js.native

  def redirect: RequestRedirect = js.native

  def integrity: String = js.native //should be DOMString
}

@js.native
class Response(content: Blob | BufferSource | FormData | String = null, init: ResponseInit)
  extends Body {

  def `type`: ResponseType = js.native

  def url: String = js.native

  def ok: Boolean = js.native

  def status: Int = js.native //actually returns unsigned short

  def statusText: String = js.native //actuall returns ByteString

  def headers: Headers = js.native //the definition says SameObject, what does that mean?

  def error(): Response = js.native

  def body: ReadableStream = js.native


}

/**
  * https://fetch.spec.whatwg.org/#body
  *
  * bblfish: not clear from the definition if this is a number of traits that may be implemented
  * by the Response or not
  */
@js.native
trait Body extends js.Object {

  def bodyUsed: Boolean = js.native

  def arrayBuffer(): Promise[ArrayBuffer] = js.native

  def blob(): Promise[Blob] = js.native

  def formData(): Promise[FormData] = js.native

  //todo: define the JSON type
  // def json(): Promise[JSON] = js.native

  def text(): Promise[String] = js.native

}


@js.native
class ResponseInit(val status: Int, val statusText: String, val headers: Headers) extends js.Object


/**
  *
  * https://fetch.spec.whatwg.org/#headers-class
  *
  * The Headers interface of the Fetch API allows you to perform various actions on HTTP request and response headers.
  * These actions include retrieving, setting, adding to, and removing. A Headers object has an associated header
  * list, which is initially empty and consists of zero or more name and value pairs.  You can add to this using
  * methods like append() (see Examples.) In all methods of this interface, header names are matched by
  * case-insensitive byte sequence.
  *
  * For security reasons, some headers can only be controller by the user agent. These headers include the forbidden
  * header names  and forbidden response header names.
  *
  * A Headers object also has an associated guard, which takes a value of immutable, request, request-no-cors,
  * response, or none. This affects whether the set(), delete(), and append() methods will mutate the header. For more
  * information see Guard.
  *
  * You can retrieve a Headers object via the Request.headers and Response.headers properties, and create a new
  * Headers object using the Headers.Headers() constructor.
  */
@js.native
class Headers(map: js.Dictionary[String] | Array[Array[String]] | Headers = Dictionary[String]())
  extends collection.JSIterable[js.Array[String]] {

  /**
    * The append() method of the Headers interface appends a new value onto an existing header inside a Headers
    * object, or adds the header if it does not already exist.
    *
    * The difference between Headers.set and append() is that if the specified header already exists and accepts
    * multiple values, Headers.set will overwrite the existing value with the new one, whereas append() will append
    * the new value onto the end of the set of values.
    */
  def append(name: String, value: String): Unit = js.native


  /**
    * The set() method of the Headers interface sets a new value for an existing header inside a Headers object, or
    * adds the header if it does not already exist.
    *
    * The difference between set() and Headers.append is that if the specified header already exists and accepts
    * multiple values, set() overwrites the existing value with the new one, whereas Headers.append appends the new
    * value to the end of the set of values.
    */
  def set(name: String, value: String): Unit = js.native

  /**
    * The delete() method of the Headers interface deletes a header from the current Headers object.
    */
  def delete(name: String): Unit = js.native

  /**
    * The get() method of the Headers interface returns the first value of a given header from within a Headers object
    * . If the requested header doesn't exist in the Headers object, the call returns null.
    */
  def get(name: String): UndefOr[String] = js.native

  /**
    * The getAll() method of the Headers interface returns an array of all the values of a header within a Headers
    * object with a given name. If the requested header doesn't exist in the Headers object, it returns an empty array.
    *
    */
  def getAll(name: String): js.Array[String] = js.native

  /**
    * The has() method of the Headers interface returns a boolean stating whether a Headers object contains a certain
    * header.
    */
  def has(name: String): Boolean = js.native


}

@js.native
class FetchEvent extends Event {
  /**
    * @return Boolean that is true if the event was dispatched with the user's intention for the page to reload,
    *         and false otherwise. Typically, pressing the refresh button in a browser is a reload, while clicking a
    *         link and
    *         pressing the back button is not.
    **/
  def isReload: Boolean = js.native

  /**
    * @return the Request that triggered the event handler.
    */
  def request: Request = js.native

  /**
    *
    * @return
    */
  def client: Client = js.native
}

@js.native
class RequestInit(
  val method: HttpMethod,
  val headers: Headers | js.Dictionary[String], //could also be sequence<sequence<ByteString>>
  val body: String, //can also be Blob or BufferSource or FormData or URLSearchParams
  val referrer: String, //should be USVString
  val referrerPolicy: ReferrerPolicy,
  val mode: RequestMode,
  val credentials: RequestCredentials,
  val requestCache: RequestCache,
  val requestRedirect: RequestRedirect,
  val integrity: String, //should be DomString
  val window: js.Any
) extends js.Object

@js.native
sealed trait ReferrerPolicy extends js.Any

object ReferrerPolicy {
  val empty                        = "".asInstanceOf[ReferrerPolicy]
  val `no-referrer`                = "no-referrer".asInstanceOf[ReferrerPolicy]
  val `no-referrer-when-downgrade` = "no-referrer-when-downgrade".asInstanceOf[ReferrerPolicy]
  val `origin-only`                = "origin-only".asInstanceOf[ReferrerPolicy]
  val `origin-when-cross-origin`   = "origin-when-cross-origin".asInstanceOf[ReferrerPolicy]
  val `unsafe-url`                 = "unsafe-url".asInstanceOf[ReferrerPolicy]
}

@js.native
sealed trait HttpMethod extends js.Any

object HttpMethod {
  val GET    = "GET".asInstanceOf[HttpMethod]
  val PUT    = "PUT".asInstanceOf[HttpMethod]
  val PATCH  = "PATCH".asInstanceOf[HttpMethod]
  val DELETE = "DELETE".asInstanceOf[HttpMethod]
  val QUERY  = "QUERY".asInstanceOf[HttpMethod]
}

@js.native
sealed trait RequestDestination extends js.Any

object RequestDestination {
  val empty        = "".asInstanceOf[RequestDestination]
  val document     = "document".asInstanceOf[RequestDestination]
  val sharedworker = "sharedworker".asInstanceOf[RequestDestination]
  val subresource  = "subresource".asInstanceOf[RequestDestination]
  val unknown      = "unknown".asInstanceOf[RequestDestination]
  val worker       = "worker".asInstanceOf[RequestDestination]
}

@js.native
sealed trait RequestMode extends js.Any

object RequestMode {
  val navigate      = "navigate".asInstanceOf[RequestMode]
  val `same-origin` = "same-origin".asInstanceOf[RequestMode]
  val `no-cors`     = "no-cors".asInstanceOf[RequestMode]
  val cors          = "cors".asInstanceOf[RequestMode]
}

@js.native
sealed trait RequestCredentials extends js.Any

object RequestCredentials {
  val omit          = "omit".asInstanceOf[RequestCredentials]
  val `same-origin` = "same-origin".asInstanceOf[RequestCredentials]
  val include       = "include".asInstanceOf[RequestCredentials]
}

@js.native
sealed trait RequestCache extends js.Any

object RequestCache {
  val default          = "default".asInstanceOf[RequestCache]
  val `no-store`       = "no-store".asInstanceOf[RequestCache]
  val reload           = "reload".asInstanceOf[RequestCache]
  val `no-cache`       = "no-cache".asInstanceOf[RequestCache]
  val `force-cache`    = "force-cache".asInstanceOf[RequestCache]
  val `only-if-cached` = "only-if-cached".asInstanceOf[RequestCache]
}


@js.native
sealed trait RequestRedirect extends js.Any

object RequestRedirect {
  val follow = "follow".asInstanceOf[RequestRedirect]
  val error  = "error".asInstanceOf[RequestRedirect]
  val manual = "manual".asInstanceOf[RequestRedirect]
}

@js.native
sealed trait ResponseType extends js.Any

object ResponseType {
  val basic          = "basic".asInstanceOf[ResponseType]
  val cors           = "cors".asInstanceOf[ResponseType]
  val default        = "default".asInstanceOf[ResponseType]
  val error          = "error".asInstanceOf[ResponseType]
  val opaque         = "opaque".asInstanceOf[ResponseType]
  val opaqueredirect = "opaqueredirect".asInstanceOf[ResponseType]
}

@js.native
sealed trait FrameType extends js.Any

/**
  * part of ServiceWorker
  * https://slightlyoff.github.io/ServiceWorker/spec/service_worker_1/#client-frametype
  */
object FrameType {
  /**
    * The window client's global object's browsing context is an auxiliary browsing context.
    */
  val auxiliary = "auxiliary".asInstanceOf[FrameType]

  /** The window client's global object's browsing context is a top-level browsing context. */
  val `top-level` = "top-level".asInstanceOf[FrameType]

  /** The window client's global object's browsing context is a nested browsing context. */
  val nested = "nested".asInstanceOf[FrameType]

  val none = "none".asInstanceOf[FrameType]
}

/**
  * part of ServiceWorker defined
  * https://slightlyoff.github.io/ServiceWorker/spec/service_worker_1/
  */
@js.native
trait Client extends js.Object {
  /**
    * The url attribute must return the context object's associated service worker client's serialized creation url.
    * @return
    */
  def url: String = js.native

  def frameType: FrameType = js.native

  /**
    * The id attribute must return its associated service worker client's id.
    * bblfish: not clear what the type is
    */
  def id: Any = js.native

  /**
    *
    * @param message bblfish not actually clear what its type is
    * @param transfer https://html.spec.whatwg.org/multipage/infrastructure.html#transferable-objects
    *                 //todo: missing the CanvasProxy type
    *                 //todo: is it really Seq ? it says `sequence` in the spec
    *                 //todo: The ArrayBuffer object is a lot more complex, see
    *                 //https://html.spec.whatwg.org/multipage/infrastructure.html#arraybuffer
    *                 //todo: What really is sequence here?
    *
    */
  def postMessage(message: js.Any, transfer: Seq[js.Array[Byte] | MessagePort]): Unit = js.native

}

