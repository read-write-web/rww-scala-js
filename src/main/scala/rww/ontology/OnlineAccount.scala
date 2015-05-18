package rww.ontology

import java.net.{URI => jURI}

import org.w3.banana.{FOAFPrefix, RDFSPrefix}
import rww.Rdf._
import rww.ui.rdf.NPGPath


/**
 * Created by hjs on 08/05/2015.
 */
case class OnlineAccount(npg: NPGPath) {
  val foaf = FOAFPrefix[Rdf]
  val rdfs = RDFSPrefix[Rdf]

  def accountName = (npg /-> foaf.accountName)

  def accountServiceHomepage = npg /-> foaf.accountServiceHomepage

  def accountProfilePage = (npg /-> foaf.homepage)

  def label = (npg /-> rdfs.label)
}
