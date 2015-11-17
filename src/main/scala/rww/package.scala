import java.math.BigInteger

import org.scalajs.dom.experimental.Headers
import org.scalajs.dom.raw
import org.w3.banana.PointedGraph
import org.w3.banana.io.{JsonLd, RDFReader, Turtle}
import org.w3.banana.jsonldjs.io.JsonLdJsParser
import org.w3.banana.n3js.io.N3jsTurtleParser
import org.w3.banana.plantain.Plantain
import rww.rdf.Named
import rww.ui.rdf.{NPGPath, NPGPathIterableW}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.Error


package object rww {
  import scala.scalajs.js.Dynamic.{global => g}

  type Rdf = Plantain
  val Rdf = Plantain
  import Rdf.ops


  implicit val jsonLDparser: RDFReader[Rdf, Future, JsonLd] = {
    import Implicits.runNow
    new JsonLdJsParser[Plantain]
  }
  implicit val turtleParser: RDFReader[Rdf, Future, Turtle] = {
    import Implicits.runNow
    new N3jsTurtleParser[Plantain]
  }

  implicit def NamedPointedGraphToNGPath(npg: Named[Rdf,PointedGraph[Rdf]]): NPGPath =
    NPGPath(npg)

  implicit def toNPGPathIterableW(npgi: Iterable[NPGPath]): NPGPathIterableW =
    new NPGPathIterableW(npgi)

   def log(msg: String,err: js.Any): Unit = {
    g.console.log(msg,err)
  }

  def headerToString(headers: Headers): String = {
    import scala.scalajs.js.collection.JSIterator._
    val l = for (h <- headers.iterator.toList) yield h(0) + ":" + h.jsSlice(1).mkString(",")
    l.mkString("\n")
  }

  implicit class JSPromiseOps[R](rawPromise: raw.Promise[R]) {
    def toFuture: Future[R] = {
      val promise = Promise[R]()
      rawPromise.andThen((ckp: R) => promise.success(ckp),
        (err: Any) => promise.failure(err.asInstanceOf[java.lang.Throwable])
      )
      promise.future
    }
  }

  implicit class JSFutureOps[R](f: Future[R]) {
    def toPromise
    (implicit ectx: ExecutionContext): raw.Promise[R] =
      new raw.Promise[R]((
        resolve: js.Function1[R, Any],
        reject: js.Function1[Any, Any]
      ) => {
        f.onSuccess {
          case (f: R) => resolve(f)
          case other => reject(new Error(s"Mhh. could not map $f to type "))
        }
        f.onFailure {
          case e: Throwable => reject(new Error(e.toString))
        }
      })
  }


}
