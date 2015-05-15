package rww.ontology

import java.net.{URI => jURI}

import org.w3.banana.PointedGraph
import org.w3.banana.plantain.Plantain.ops._
import rww.Rdf._

/**
 * Created by hjs on 07/05/2015.
 */
case class Address(pg: PointedGraph[Rdf]) {

  val ct = ContactPrefix[Rdf]

  def postalCode = (pg/ct.postalCode)++(pg/ct.zip) map ( _.pointer ) collect {
    case Literal(str,_,_) => str
  }

  def street = List(street1,street2,street3).flatMap(_.headOption)

  def street1 = pg/ct.street map ( _.pointer ) collect {
    case Literal(str,_,_) => str
  }

  def street2 = (pg/ct.street2) map ( _.pointer ) collect {
    case Literal(str,_,_) => str
  }

  def street3 = (pg/ct.street3) map ( _.pointer ) collect {
    case Literal(str,_,_) => str
  }

  def city = (pg/ct.city) map ( _.pointer ) collect {
    case Literal(str,_,_) => str
  }

  def stateOrProvince = (pg/ct.stateOrProvince) map ( _.pointer ) collect {
    case Literal(str,_,_) => str
  }

  def country = (pg/ct.country) map ( _.pointer ) collect {
    case Literal(str,_,_) => str
  }

}
