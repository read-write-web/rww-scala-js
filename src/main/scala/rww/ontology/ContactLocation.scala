package rww.ontology

import org.w3.banana.PointedGraph
import org.w3.banana.plantain.Plantain.ops._
import rww.Rdf._

/**
 * Created by hjs on 07/05/2015.
 */
case class ContactLocation(pg: PointedGraph[Rdf]) {

  val ct = ContactPrefix[Rdf]

  def address = (pg / ct.address) map {
    Address(_)
  }

  def phone = (pg / ct.mailbox) map (_.pointer) collect {
    case URI(u) => {
      val uu = URI(u)
      uu.getScheme.toLowerCase match {
        case "tel" => uu.getAuthority
        case _ => "(unknown)"
      }
    }
    case Literal(lexicalForm, xsd.string, _) => lexicalForm
  }

}
