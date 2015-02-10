package rww.ui

import org.w3.banana._
import org.w3.banana.plantain.Plantain.ops._
import org.w3.banana.plantain.Plantain

object Util {
  
  def getFirstLiteral(pg: PointedGraph[Plantain], matcher: Plantain#URI, default: => Any): Any =
    (pg / matcher).toList.map(_.pointer).collectFirst {
      case Literal((v, _, _)) => v
    }.getOrElse(default)

  def getFirstUri(pg: PointedGraph[Plantain], matcher: Plantain#URI, default: => String): String =
    (pg / matcher).toList.map(_.pointer).collectFirst {
      case URI(l) => l
    }.getOrElse(default)

}