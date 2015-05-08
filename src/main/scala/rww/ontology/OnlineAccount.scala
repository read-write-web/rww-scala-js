package rww.ontology

import org.w3.banana.{RDFSPrefix, FOAFPrefix, PointedGraph}
import rww.rdf._
import org.w3.banana.plantain.Plantain.ops._
import java.net.{URI=>jURI}

import scala.util.Try


/**
 * Created by hjs on 08/05/2015.
 */
case class OnlineAccount(pg: PointedGraph[Rdf]) {
  val foaf = FOAFPrefix[Rdf]
  val rdfs = RDFSPrefix[Rdf]

  def accountName = (pg/foaf.accountName) map (_.pointer) collect {
    case Literal(lit,_,_) => lit
  }

  def accountServiceHomepage = {
    val ash = (pg/foaf.accountServiceHomepage) map (_.pointer) collectFirst  {
      case URI(u) => Try(new jURI(u)).toOption
    }
    ash.flatten
  }

  def accountProfilePage = (pg/foaf.homepage) map (_.pointer) collect {
    case URI(u) => Try(new jURI(u)).toOption
  }

  def label = (pg/rdfs.label) map (_.pointer) collect {
    case Literal(label,_,_) => label
  }
}
