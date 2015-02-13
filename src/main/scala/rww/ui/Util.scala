package rww.ui

import org.w3.banana._
import org.w3.banana.plantain.Plantain.ops._
import org.w3.banana.plantain.Plantain

object Util {

  def getFirstLiteral(pg: PointedGraph[Plantain], matcher: Plantain#URI, default: => Any): Any =
    (pg / matcher).toList.map(_.pointer).collectFirst {
      case Literal((v, _, _)) => v
    }.getOrElse(default)

  def getModifiedCopy(pg: Option[PointedGraph[Plantain]], matcher: Plantain#URI, node: Plantain#Node): Option[PointedGraph[Plantain]] = pg match {
    case Some(pg) => {
      val p = pg.pointer
      val trip = pg.graph.triples.map {
        case (s, p, _) if p == matcher => (s, matcher, node)
        case t                         => t
      }
      Some(PointedGraph[Plantain](p, makeGraph(trip)))
    }
    case None => None
  }

  def getFirstUri(pg: PointedGraph[Plantain], matcher: Plantain#URI, default: => String): String =
    (pg / matcher).toList.map(_.pointer).collectFirst {
      case URI(l) => l
    }.getOrElse(default)

}