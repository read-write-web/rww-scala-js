package rww.ontology

import java.net.{URI => jURI}

import org.w3.banana.{FOAFPrefix, PointedGraph}
import rww.Rdf
import rww.ui.rdf.NPGPath

/**
 *
 */
case class Person(npg: NPGPath) {
  val foaf = FOAFPrefix[Rdf]
  val ct = ContactPrefix[Rdf]
  import rww._
  //  val contact =


  def name = (npg /-> foaf.name)

  def nick = (npg /-> foaf.nick)

  def givenName = (npg /-> foaf.givenName) ++ (npg /-> foaf.givenname)

  def familyName = (npg /-> foaf.familyName) ++ (npg /-> foaf.family_name)

  def firstName = (npg /-> foaf.firstName)

  def workPlaceHomePage = (npg /-> foaf.workplaceHomepage)

  def depiction = (npg /-> foaf.img) ++ (npg /-> foaf.depiction)

  def logo = (npg /-> foaf.logo)

  def account = (npg /-> foaf.account) ++ (npg /-> foaf.holdsAccount) map {
    OnlineAccount(_)
  }

  def mbox = (npg /-> foaf.mbox) map { Mbox(_) }

  def phone = (npg /-> foaf.phone) map { Tel(_) }

  def office = (npg /-> ct.office) map { ContactLocation(_) }

  def home = (npg /-> ct.home) map { ContactLocation(_) }

  def emergency = (npg /-> ct.emergency) map { ContactLocation(_) }

  def mobile = (npg /-> ct.mobile) map { ContactLocation(_) }

}




