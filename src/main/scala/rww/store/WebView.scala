package rww.store

import org.w3.banana.{PointedGraph, RDF, RDFOps}
import rww.rdf.Named

import scala.collection.immutable.HashMap
import scalaz.{-\/, \/, \/-}

/**
 * The state of the web from the agents perspective at a point in time. 
 * This is an immutable object, which can be passed to react to display one state of the web.
 */
class WebView[Rdf <: RDF](val cache: HashMap[Rdf#URI, \/[Rdf#URI, Rdf#Graph]] = new HashMap[Rdf#URI, \/[Rdf#URI, Rdf#Graph]]())
                         (implicit ops: RDFOps[Rdf]) {

  import ops._

  /**
   *
   * @param url
   * @param maxdepth redirects to follow. It never makes sense to have more than 1, since
   *                 xmlhttp requests follow redirects automatically.
   * @return
   */
  def get(url: Rdf#URI, maxdepth: Int = 1): Option[Named[Rdf, PointedGraph[Rdf]]] =
    if (maxdepth <= 0) None
    else {
      val base = url.fragmentLess
      cache.get(base) flatMap {
        case -\/(redirect) => get(redirect, maxdepth - 1)
        case \/-(graph) => Some(Named(base, PointedGraph[Rdf](url, graph)))
      }
    }

  //just to get going
  //returns the new patched WebView if there was anything to patch
  def vsimplePatch(url: Rdf#URI, add: Rdf#Triple, remove: Rdf#Triple): Option[WebView[Rdf]] =
    cache.get(url).map {
      case -\/(redirect) => WebView.this //todo later
      case \/-(graph) => {
        val g = graph.diff(Graph(remove))
        val newg = g union Graph(add)
        new WebView(cache.updated(url, \/-(newg)))
      }
    }
}
