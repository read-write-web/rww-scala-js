//package rww.ui.foaf
//
//import java.io.StringReader
//import java.net.{URI => jURI}
//
//import japgolly.scalajs.react.React
//import org.scalajs.dom
//import org.scalajs.dom.document
//import org.w3.banana.binder.ToPG
//import org.w3.banana.plantain._
//import org.w3.banana.{FOAFPrefix, PointedGraph, RDF, RDFOps}
//import rww.store.WebAgent
//import rww.ontology.Person
//import rww.rdf.Named
//import rww.ui.URLBox.Props
//import rww.ui.foaf.Profile.Props
//
//import scala.scalajs.js
//import scala.scalajs.js.Date
//import scala.util.{Failure, Success}
//import scalacss.Defaults._
//import scalacss.ScalaCssReact._
//
//
///**
// * Created by hjs on 30/04/15.
// */
//object TestApp extends js.JSApp {
//  import rww.Rdf
//  implicit val ops = Plantain.ops
//
//  val foaf = FOAFPrefix[Rdf]
//
//  implicit def ToURIToPG[Rdf <: RDF, T](implicit ops: RDFOps[Rdf]) = new ToPG[Rdf, jURI] {
//    def toPG(t: jURI): PointedGraph[Rdf] = PointedGraph(t.asInstanceOf[Rdf#URI])
//  }
//
//  import org.w3.banana.plantain.Plantain.ops._
//
//  val el = document getElementById "eg1"
//
//  @js.annotation.JSExport
//  override def main(): Unit = {
//    FoafStyles.addToDocument()
//    example3()
//  }
//
//  val bbl = URI("http://bblfish.net/people/henry/card#me")
//  val bblDocUri = bbl.fragmentLess
//
//  //start with a locally built graph, find picture
//  def example() = {
//    val graph = (
//      bbl -- foaf.depiction ->- "hello"
//        -- foaf.depiction ->- URI("http://farm1.static.flickr.com/164/373663745_e2066a4950.jpg")
//        -- foaf.depiction ->- URI("http://bblfish.net/pix/bfish.large.jpg")
//      ).graph
//    React.render(Profile(Props(Named(bblDocUri,Person(PointedGraph[Rdf](bbl, graph))))), el)
//  }
//
//  //parse graph from string then show picture
//  def example2() = {
//    // do a request on the internet to get the file for the above url
//    val foaf = "http://xmlns.com/foaf/0.1/"
//    val xsd = "http://www.w3.org/2001/XMLSchema#"
//    val base = bblDocUri.toString
//    val bblDoc =
//      s"""<$base#me> <${foaf}depiction> <http://farm1.static.flickr.com/164/373663745_e2066a4950.jpg> .
//         |<$base#me> <${foaf}depiction> <http://bblfish.net/pix/bfish.large.jpg> .
//         |<$base#me> <${foaf}name> "Henry" .
//         |<$base#me> <${foaf}age> "42"^^<${xsd}int> .
//         |<$base#me> <${foaf}near> "England"@en .
//         |""".stripMargin
//
//    println(bblDoc)
//
//
//    //parse document above with Readers to get graph below
//
//    val f = for {
//      g <- Plantain.ntriplesReader.read(new StringReader(bblDoc), bblDocUri.toString)
//    // one should then later also add the graph to a store
//    //      _ <- appendToGraph(rww.rdf.jsstore, bblDocUri, g) //add to store
//    //      graph <- getGraph(rww.rdf.jsstore,bblDocUri ) //get from store
//    } yield {
//        React.render(Profile(Props(Named(bblDocUri,Person(PointedGraph[Rdf](bbl, g))))), el)
//      }
//    f.get
//  }
//
//
//
//  import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
//  val ws = new WebAgent[Rdf](None)
//
//
//  def example3() = {
//
//    ws.fetch(bbl).map(npg => {
//
//      React.render(Profile(Props(npg.map(Person(_)))), el)
//    }).onComplete(x => dom.console.log(x.asInstanceOf[js.Any]))
//  }
//
//  def example4() = {
//    ws.fetch(URI("http://bblfish.net/people/fake/me")).map(npg => {
//
//      React.render(Profile(Props(npg.map(Person(_)))), el)
//    }).onComplete(x => dom.console.log(x.asInstanceOf[js.Any]))
//  }
//
//  //    import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
//  //    for {
//  //      i <- rdfstoreOps.loadRemote(jsstore, bblDocUri)
//  //      g <- JSStore.store.getGraph(JSStore.jsstore, bblDocUri)
//  //    } yield {
//  //      React.render(component(PointedGraph[Rdf](bbl, g)), el)
//  //    }
//
//}
