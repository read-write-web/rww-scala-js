package rww.ontology

import org.w3.banana.{PrefixBuilder, RDFOps, RDF}

/**
 * Created by hjs on 06/05/2015.
 */

object ContactPrefix {
  def apply[Rdf <: RDF : RDFOps](implicit ops: RDFOps[Rdf]) = new ContactPrefix(ops)
}

class ContactPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends PrefixBuilder[Rdf]("contact", "http://www.w3.org/2000/10/swap/pim/contact#")(ops) {

  val Address = apply("Address")
  val ContactLocation = apply("ContactLocation")
  val Female = apply("Female")
  val LanguageCode = apply("LanguageCode")
  val Male = apply("Male")
  val Phone = apply("Phone")
  val SocialEntity = apply("SocialEntity")
  val address = apply("address")
  val assistant = apply("assistant")
  val birthday = apply("birthday")
  val city = apply("city")
  val country = apply("country")
  val child = apply("child")
  val description = apply("description")
  val emailAddress = apply("emailAddress")
  val emergency = apply("emergency")
  val fax = apply("fax")
  val fullName = apply("fullName")
  val home = apply("home")
  val homePage = apply("homePage")
  val homePageAddress = apply("homePageAddress")
  val knownAs = apply("knownAs")
  val mailbox = apply("mailbox")
  val mailboxURI = apply("mailboxURI")
  val mobile = apply("mobile")
  val motherTongue = apply("motherTongue")
  val nearestAirport = apply("nearestAirport")
  val office = apply("office")
  val participant = apply("participant")
  val partner = apply("partner")
  val postalCode = apply("postalCode")
  val phone = apply("phone")
  val preferredURI = apply("preferredURI")
  val region = apply("region")
  val sortName = apply("sortName")
  val stateOrProvince = apply("stateOrProvince")
  val street = apply("street")
  val street2 = apply("street2")
  val street3 = apply("street3")
  val vacationHome = apply("vacationHome")
  val webPage = apply("webPage")
  val zip = apply("zip")
}
