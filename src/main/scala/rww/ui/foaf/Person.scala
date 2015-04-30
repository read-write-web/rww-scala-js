package rww.ui.foaf

import org.w3.banana.{FOAFPrefix, PointedGraph}
import rww.rdf._

/**
 * Created by hjs on 30/04/15.
 */
case class Person(pg: PointedGraph[Rdf])  {
  import org.w3.banana.plantain.Plantain.ops._
  val foaf = FOAFPrefix[Rdf]

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
    case Literal(lexicalForm,xsd.string,lang) => lexicalForm
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

}
