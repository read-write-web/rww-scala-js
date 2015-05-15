package rww.ontology

import org.w3.banana.PointedGraph
import rww.Rdf._
import org.w3.banana.plantain.Plantain.ops._
import java.net.{URI=>jURI}

import scala.util.Try

/**
 * Created by hjs on 08/05/2015.
 */
case class Tel(pg: PointedGraph[Rdf]) {
  def asString: Option[String] = {
    val str = pg.pointer match {
      case Literal(mbox,_,_) => Some(mbox)
      case URI(s) => Some(s)
      case _ => None // could check other relations from here
    }
    str.map { s =>
      if (s.startsWith("tel:")) s.substring("tel:".size)
      else s
    }
  }
  def asURIStr: Option[String] = pg.pointer  match {
    case URI(u) => Some(u)
    case Literal(mboxstr,_,_) => {
      val optUriStr = if (mboxstr.startsWith("tel:")) Some(mboxstr)
      else None
      optUriStr.flatMap(mb=>Try(new jURI(mb).toString).toOption)
    }
    case _ => None
  }
}
