package rww.ontology

import java.net.{URI => jURI}

import org.w3.banana.plantain.Plantain.ops._
import org.w3.banana.{FOAFPrefix, PointedGraph}
import rww.rdf._

/**
 *
 */
case class Person(pg: PointedGraph[Rdf])  {
  val foaf = FOAFPrefix[Rdf]
  val ct = ContactPrefix[Rdf]
//  val contact =

  def name = (pg/foaf.name) map (_.pointer) collect {
    case Literal(lexicalForm,xsd.string,lang) => lexicalForm
    case Literal(lexicalForm,rdf.langString,lang) => lexicalForm
  }
  def nick = (pg/foaf.nick) map (_.pointer) collect {
    case Literal(lexicalForm,xsd.string,lang) => lexicalForm
    case Literal(lexicalForm,rdf.langString,lang) => lexicalForm
  }
  def givenName = (pg/foaf.givenName)++(pg/foaf.givenname) map (_.pointer) collect {
    case Literal(lexicalForm,xsd.string,lang) => lexicalForm
    case Literal(lexicalForm,rdf.langString,lang) => lexicalForm
  }
  def familyName = (pg/foaf.familyName)++(pg/foaf.family_name) map (_.pointer) collect {
    case Literal(lexicalForm,xsd.string,lang) => lexicalForm
    case Literal(lexicalForm,rdf.langString,lang) => lexicalForm
  }
  def firstName =  (pg/foaf.firstName) map (_.pointer) collect {
    case Literal(lexicalForm,xsd.string,None) => lexicalForm
    case Literal(lexicalForm,rdf.langString,lang) => lexicalForm
  }
  def workPlaceHomePage = (pg/foaf.workplaceHomepage) map (_.pointer) collect {
    case URI(u) => u
  }
  def depiction = (pg/foaf.img)++(pg/foaf.depiction) map (_.pointer) collect {
    case URI(u) => u
  }
  def logo = (pg/foaf.logo) map (_.pointer) collect {
    case URI(u) => u
  }
  
  def account = (pg/foaf.account)++(pg/foaf.holdsAccount) map { OnlineAccount(_) }

  def mbox = (pg/foaf.mbox) map { Mbox(_) }

  def phone = (pg/foaf.phone) map { Tel(_) }

  def office = (pg/ct.office)  collect {
    case bpg @ PointedGraph(BNode(_),_) => ContactLocation(bpg)
    case upg @ PointedGraph(URI(_),_)   => ContactLocation(upg)
    //todo: how problematic is a literal ContactLocation?
  }
  def home = (pg/ct.home)  collect {
    case bpg @ PointedGraph(BNode(_),_) => ContactLocation(bpg)
    case upg @ PointedGraph(URI(_),_)   => ContactLocation(upg)
    //todo: how problematic is a literal ContactLocation?
  }
  def emergency = (pg/ct.emergency)  collect {
    case bpg @ PointedGraph(BNode(_),_) => ContactLocation(bpg)
    case upg @ PointedGraph(URI(_),_)   => ContactLocation(upg)
    //todo: how problematic is a literal ContactLocation?
  }
  def mobile = (pg/ct.mobile)  collect {
    case bpg @ PointedGraph(BNode(_),_) => ContactLocation(bpg)
    case upg @ PointedGraph(URI(_),_)   => ContactLocation(upg)
    //todo: how problematic is a literal ContactLocation?
  }

}




