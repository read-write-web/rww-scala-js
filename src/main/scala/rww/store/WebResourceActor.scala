package rww.store

import java.io.StringReader
import java.net.{URI => jURI}
import java.nio.ByteBuffer

import akka.actor.{Actor, ActorRef, Props}
import org.scalajs.dom
import org.scalajs.dom.crypto._
import org.scalajs.dom.experimental.{Request => HttpRequest, Response => HttpResponse, _}
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.raw.{ProgressEvent, Promise, XMLHttpRequest}
import org.w3.banana.RDFOps
import org.w3.banana.io.{JsonLd, NTriples, RDFReader, Turtle}
import rww.{Rdf, log}

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.UndefOr
import scala.scalajs.js.collection.JSIterator._
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js.typedarray.{ArrayBuffer, TypedArrayBuffer, Uint8Array}
import scala.util.{Failure, Success, Try}



object WebResourceActor {
  //see http://doc.akka.io/docs/akka/2.3.11/scala/actors.html#props
  def props(u: Rdf#URI)(implicit
    ops: RDFOps[Rdf],
    rdrNT: RDFReader[Rdf, Try, NTriples],
    rdrTurtle: RDFReader[Rdf, Future, Turtle],
    rdrJSONLD: RDFReader[Rdf, Future, JsonLd]
  ): Props = {
    Props(new WebResourceActor(u)(ops,rdrNT,rdrTurtle,rdrJSONLD))
  }
}

/**
 * Created by hjs on 19/06/2015.
 */
class WebResourceActor(
  resourceName: Rdf#URI
)(implicit
  ops: RDFOps[Rdf],
  rdrNT: RDFReader[Rdf, Try, NTriples],
  rdrTurtle: RDFReader[Rdf, Future, Turtle],
  rdrJSONLD: RDFReader[Rdf, Future, JsonLd]
) extends Actor {

  val rdfMimeTypes = "application/n-triples,application/ld+json;q=0.8,text/turtle;q=0.8"

  import ops._

  var state: RequestState = UnRequested(resourceName)


  def isSignature(ah: String): Boolean = ah.toLowerCase.startsWith("signature")

  def update(newState: RequestState,sender: ActorRef) = {
    state = newState
    sender ! newState  // one could extend this to a collection of listeners
  }


  override def receive = {
    case g@Get(base, proxy, mode) => forceFetch(base, proxy, context.sender())
    case Update(base, remove, add) => vsimplePatch(base,remove,add)
    case rs: RequestState =>  state = rs //for dealing with redirects
  }

  protected
  def forceFetch(base: Rdf#URI, proxiedURL: Rdf#URI, sender: ActorRef): Unit = {
    import org.scalajs.dom.experimental.fetch

    def consume(reader: ReadableStreamReader): Promise[Unit] = {
      var data: String = ""
      new Promise[Unit](
        (resolve: js.Function1[Unit, Any], reject: js.Function1[Any, Any]) => {
          def pump(): Unit = {
            reader.read().andThen { chunk: Chunk =>
              if (chunk.done) {
                resolve()
                ()
              }
              else {
                pump()
              }
            }.recover(reject)
          }
          pump()
        }
      )
    }

    /**
      * If the response contains <code>WWW-Authenticate: Signature ...</code> header
      * then this will retry the request but with a <code>Authorization: Signature ...</code>
      * header as defined by the
      * <a href="https://tools.ietf.org/html/draft-cavage-http-signatures-05">http signatures</a>
      * draft rfc.
      *
      * This can result in the creation of a keypair ( todo: though that would also require publication
      * of the public key to have any chance of working  ).
      *
      * The signature will contain the User header if the <code>userId</code> field is set in the
      * KeyInfo
      *
      * @param request
      * @param response
      * @return
      */
    def sign(request: HttpRequest, response: HttpResponse): Promise[HttpResponse] = {
      val x = for {
        ah<-response.headers.get("WWW-Authenticate")
        if (isSignature(ah))
      } yield {
        //1. calculate signature
        import JSExecutionContext.Implicits.queue
        import rww.JSFutureOps
        val keyPromise = KeyStore.keyFuture.toPromise
        keyPromise.andThen { ki: KeyInfo =>
          val ckp: CryptoKeyPair = ki.keyPair

          val url = new java.net.URI(request.url)
          val method = request.method.toString.toLowerCase
          val path = url.getPath + {if(url.getQuery==null)""else "?"+url.getQuery}
          val host = url.getAuthority
          val now = new js.Date().toISOString()
          val nr = new HttpRequest(request)
          nr.headers.set("Signature-Date",now)
          val sigText =
            s"(request-target): $method $path\n"+
            s"host: $host\n"+
              ki.userId.map(u=>s"user: $u\n").getOrElse("")+
            s"signature-date: $now"

          log("~sign> sigText=",">>>"+sigText+"<<<")
          log("~sign>private key", ckp.privateKey)
          log("~sign>pubkey algorithm", ckp.privateKey.algorithm)

          import js.JSConverters._
          val s = GlobalCrypto.crypto.subtle.sign(
            ckp.publicKey.algorithm,
            ckp.privateKey,
            new Uint8Array(sigText.getBytes("ASCII").toJSArray).buffer
          )
          s.andThen { sig: js.Any =>
            //2. add signature
            val bb = TypedArrayBuffer.wrap(sig.asInstanceOf[ArrayBuffer])
            val hashedSig = {
              import com.github.marklister.base64.Base64._

              val arraybuf: Array[Byte] = new Array[Byte](bb.remaining())
              bb.get(arraybuf)
              arraybuf.toBase64
            }

            def user = ki.userId.fold(" ")(_=>" user ")
            val sigHdr =
              s"""Signature keyId="https://joe.example:8443/2013/key#",algorithm="rsa-sha256",
                  |headers="(request-target) host${ user }signature-date",
                  |signature="${hashedSig}"
                  |""".stripMargin.replaceAll("\n", "")
            log("~sign>returning sig", sigHdr)
            nr.headers.set("Authorization", sigHdr)
            fetch(nr)
          }
        }
      }
      val res = x.getOrElse(new Promise((resolve: js.Function1[Unit, Any], reject: js.Function1[Any, Any])=>response))
      res.asInstanceOf[Promise[HttpResponse]]
    }


    def cacheStateOf(response: HttpResponse)(finalURLToState: Rdf#URI => RequestState): Unit = {
      // responseURL is quite new. See https://xhr.spec.whatwg.org/#the-responseurl-attribute
      // hence need for UndefOr ( but according to latest xhr spec it should return "" not undefined )
      // todo: to remove dynamic use need to fix https://github.com/scala-js/scala-js-dom/issues/111
      val finalURL = {
        val redirectURLstr = response.url
        val redirectURL = URI(redirectURLstr)
        if (redirectURL == proxiedURL || redirectURLstr == "")
          base
        else
          redirectURL
      }
      if (finalURL != base) {
        update(Redirected(base, finalURL), sender)
        //todo: need to send back a request to change the state of the final URL
      } else update(finalURLToState(finalURL), sender)

    }

    val requestInit = literal(
      headers = literal(  "Accept" -> rdfMimeTypes ),
      requestCache = RequestCache.reload,
      credentials = RequestCredentials.include //<- does not work if server's Access-Control-Allow-Origin is set to *
//      window = null // should work in the future
    ).asInstanceOf[js.Dictionary[js.Any]]

    log("request init",requestInit)
    val request = new HttpRequest(proxiedURL.toString, requestInit)

    def process(res: HttpResponse, txt: String): Future[Unit] = {
      import org.w3.banana.TryW
      import JSExecutionContext.Implicits.queue

      val rh = res.headers.get("Content-Type").toOption
      println(s"<$proxiedURL> content is ${txt.substring(0, 80)}")
      val reader = new StringReader(txt)
      for {
        g <- rh.map(_.takeWhile(_ != ';').trim.toLowerCase) match {
          case Some("application/n-triples") => rdrNT.read(reader, base.toString).asFuture
          case Some("application/ld+json") => rdrJSONLD.read(reader, base.toString)
          case Some("text/turtle") => rdrTurtle.read(reader, base.toString)
          case Some(other) => Future.failed(new scala.Exception("could not find parser for " + other))
          case None => Future.failed(
            new scala.Exception("missing content type on response - unable to parse response"
            ))
        }
      } yield {
        cacheStateOf(res) { graphUrl: Rdf#URI =>
          Ok(res.status,
            graphUrl,
            res.headers.iterator().map(a => a.mkString(": ")).toList.mkString("\n"),
            txt,
            Success(g)
          )
        }
      }
    }
    fetch(request) andThen { res: HttpResponse =>
      log(s"~fetch> ${res.status} <$base> headers:",
        rww.headerToString(res.headers))

      // todo: make this asynchronous
      // consume(res.body.getReader(),res.headers.get("Content-Length"))
      res.text() andThen { txt: String =>
        if (res.ok) {
          process(res, txt)
        } else if (res.status == 401) {
          log("~~fetchListener> intercepted 401", res.url)
          //              response
          // one cannot just use an old request! so one has to make a new one.
          val newRequest = new HttpRequest(request.url,requestInit)
          sign(newRequest, res).andThen { res2: HttpResponse =>
            log(s"~fetch signed response> ${res2.status} <$base> headers:",
              rww.headerToString(res2.headers))

            //todo: ugly manual recursion
            if (res2.ok) {
              res2.text() andThen { txt: String => process(res2, txt) }
            } else {
              cacheStateOf(res2)(
                url => HttpError(url,
                  code = res2.status,
                  headers = res2.headers.iterator().map(a => a.mkString(": ")).toList.mkString("\n"),
                  body = "could not authenticate")
              )
            }
          }
          //              fetch(sign(response))
        } else {
          //todo: deal with redirects here too
          cacheStateOf(res)(
            url => HttpError(url,
              code = res.status,
              headers = res.headers.iterator().map(a => a.mkString(": ")).toList.mkString("\n"),
              body = txt)
          )
        }
      } recover { e: Any =>
        log("~~> in recover from fetch",e.asInstanceOf[js.Any])
        cacheStateOf(res)(
          url => HttpError(url,
            code = res.status,
            headers = res.headers.iterator().map(a => a.mkString(": ")).toList.mkString("\n"),
            body = e.toString)
        )
      }
    }
  }



  protected
  def forceFetchAjax(base: Rdf#URI, proxiedURL: Rdf#URI, sender: ActorRef) = {
    import JSExecutionContext.Implicits.queue
    import scalaz.Scalaz._

    AjaxPlus.get(
      proxiedURL.toString,
      headers = Map("Accept" -> rdfMimeTypes),
      progress = (ev: ProgressEvent) => {
        if (ev.lengthComputable && ev.loaded != ev.total)
          sender ! Downloading(base, (ev.lengthComputable).option(ev.loaded / ev.total))
      }
    ) onComplete { xhrTry: Try[dom.XMLHttpRequest] =>

      def cacheStateOf(xhr: XMLHttpRequest)(finalURLToState: (Rdf#URI) => RequestState): Unit = {
        // responseURL is quite new. See https://xhr.spec.whatwg.org/#the-responseurl-attribute
        // hence need for UndefOr ( but according to latest xhr spec it should return "" not undefined )
        // todo: to remove dynamic use need to fix https://github.com/scala-js/scala-js-dom/issues/111
        val redirectURLOpt = xhr.asInstanceOf[js.Dynamic].responseURL.asInstanceOf[UndefOr[String]]
        val finalURL = redirectURLOpt.map { redirectURLstr =>
          val redirectURL = URI(redirectURLstr)
          if (redirectURL == proxiedURL || redirectURLstr == "")
            base
          else
            redirectURL
        } getOrElse {
          base
        }
        if (finalURL != base) {
          update(Redirected(base, finalURL),sender)
          //todo: need to send back a request to change the state of the final URL
        } else update(finalURLToState(finalURL),sender)

      }

      xhrTry match {
        case Success(xhr) => {
          import org.w3.banana.TryW
          val rh = Option(xhr.getResponseHeader("Content-Type"))
          val reader = new StringReader(xhr.responseText)
          for {
            g <- rh.map(_.takeWhile(_ != ';').trim.toLowerCase) match {
              case Some("application/n-triples") => rdrNT.read(reader, base.toString).asFuture
              case Some("application/ld+json") => rdrJSONLD.read(reader, base.toString)
              case Some("text/turtle") => rdrTurtle.read(reader, base.toString)
              case Some(other) => Future.failed(new scala.Exception("could not find parser for " + other))
              case None => Future.failed(
                new scala.Exception(
                  "missing content type on response - unable to parse data!"
                ))
            }
          } yield {
            cacheStateOf(xhr) { graphUrl =>
              Ok(xhr.status,
                graphUrl,
                xhr.getAllResponseHeaders(),
                xhr.responseText,
                Success(g)
              )
            }
          }
        }
        case Failure(AjaxException(xhr)) => {
          println(s"Failure for <$base> with code ${xhr.status}")
          //todo: deal with redirects here too
          cacheStateOf(xhr)(
            url => HttpError(url, code = xhr.status, headers = xhr.getAllResponseHeaders(), body = xhr.responseText))
        }
        case Failure(other) => {
          //todo: deal correctly with redirects here too if it makes sense
          println(s"Other failure! " + other.toString)
          update(HttpError(resourceName,code = 477, headers = "", body = other.toString),sender)
        }
      }
    }
  }

  protected
  def vsimplePatch(url: Rdf#URI, remove: Rdf#Triple, add: Rdf#Triple): Unit = {
    state match {
      case Ok(code, url, headers, body, parsed) => {
        //todo: we should of course send a PATCH to the server here
        update(
          Ok(code, url, headers, body, parsed.map { graph =>
            graph.diff(Graph(remove)) union Graph(add)
          }),
          context.sender
        )
      }
//      case other => other //send back error message. we can't update
    }
  }


}
