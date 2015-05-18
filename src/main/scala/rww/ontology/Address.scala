package rww.ontology

import java.net.{URI => jURI}

import org.w3.banana.PointedGraph
import org.w3.banana.plantain.Plantain.ops._
import rww.Rdf._
import rww.ui.rdf.NPGPath

/**
 * Created by hjs on 07/05/2015.
 */
case class Address(npg: NPGPath) {

  val ct = ContactPrefix[Rdf]

  def postalCode = (npg /-> ct.postalCode) ++ (npg /-> ct.zip)

  def street = Iterable(street1.headOption, street2.headOption, street3.headOption).flatten

  def street1 = npg /-> ct.street

  def street2 = npg /-> ct.street

  def street3 = npg /-> ct.street3

  def city = npg /-> ct.city

  def stateOrProvince = npg /-> ct.stateOrProvince

  def country = npg /-> ct.country

}
