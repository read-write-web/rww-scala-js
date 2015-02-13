package rww.ui

import org.w3.banana._
import org.w3.banana.plantain.Plantain.ops._
import org.w3.banana.plantain.Plantain

object Util {

  def getFirstLiteral(pointedGraph: Option[PointedGraph[Plantain]], matcher: Plantain#URI): Option[Plantain#Literal] = pointedGraph match {
    case Some(pg) => (pg / matcher).toList.map(_.pointer).collectFirst {
      case Literal((v, _, _)) => println("Match literal"); Literal(v)
    }
    case _ => None
  }

  def getFirstUri(pointedGraph: Option[PointedGraph[Plantain]], matcher: Plantain#URI): Option[Plantain#URI] = pointedGraph match {
    case Some(pg) => (pg / matcher).toList.map(_.pointer).collectFirst {
      case URI(l) => URI(l)
    }
    case _ => None
  }

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
}