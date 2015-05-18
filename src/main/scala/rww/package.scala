import org.w3.banana.PointedGraph
import org.w3.banana.plantain.Plantain
import rww.rdf.Named
import rww.ui.rdf.{NPGPath, NPGPathIterableW}


package object rww {

  type Rdf = Plantain
  val Rdf = Plantain

  implicit def NamedPointedGraphToNGPath(npg: Named[Rdf,PointedGraph[Rdf]]): NPGPath =
    NPGPath(npg)

  implicit def toNPGPathIterableW(npgi: Iterable[NPGPath]): NPGPathIterableW =
    new NPGPathIterableW(npgi)
}
