import org.scalajs.dom.crypto._
import org.scalajs.dom.experimental.Headers
import org.scalajs.dom.raw.Promise
import org.w3.banana.PointedGraph
import org.w3.banana.io.{JsonLd, RDFReader, Turtle}
import org.w3.banana.jsonldjs.io.JsonLdJsParser
import org.w3.banana.n3js.io.N3jsTurtleParser
import org.w3.banana.plantain.Plantain
import rww.rdf.Named
import rww.ui.rdf.{NPGPath, NPGPathIterableW}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.typedarray.Uint8Array


package object rww {
  import scala.scalajs.js.Dynamic.{global => g}

  type Rdf = Plantain
  val Rdf = Plantain
  import Rdf.ops


  implicit val jsonLDparser: RDFReader[Rdf, Future, JsonLd] = new JsonLdJsParser[Plantain]
  implicit val turtleParser: RDFReader[Rdf, Future, Turtle] = new N3jsTurtleParser[Plantain]

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

}
