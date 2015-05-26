import org.w3.banana.PointedGraph
import org.w3.banana.io.{JsonLd, Turtle, RDFReader}
import org.w3.banana.jsonldjs.io.JsonLdJsParser
import org.w3.banana.n3js.io.N3jsTurtleParser
import org.w3.banana.plantain.Plantain
import rww.rdf.Named
import rww.ui.rdf.{NPGPath, NPGPathIterableW}
import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.runNow


package object rww {

  type Rdf = Plantain
  val Rdf = Plantain
  import Rdf.ops


  implicit val jsonLDparser: RDFReader[Rdf, Future, JsonLd] = new JsonLdJsParser[Plantain]
  implicit val turtleParser: RDFReader[Rdf, Future, Turtle] = new N3jsTurtleParser[Plantain]

  implicit def NamedPointedGraphToNGPath(npg: Named[Rdf,PointedGraph[Rdf]]): NPGPath =
    NPGPath(npg)

  implicit def toNPGPathIterableW(npgi: Iterable[NPGPath]): NPGPathIterableW =
    new NPGPathIterableW(npgi)
}
