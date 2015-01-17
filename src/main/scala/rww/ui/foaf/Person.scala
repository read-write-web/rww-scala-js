package rww.ui.foaf

import japgolly.scalajs.react.vdom.ReactVDom.all._
import japgolly.scalajs.react.{ReactComponentB, _}
import org.scalajs.dom.document
import org.w3.banana._
import org.w3.banana.plantain.Plantain

import scala.scalajs.js

object Person extends js.JSApp {
  type Rdf = Plantain
  implicit val ops = Plantain.ops
  val foaf = FOAFPrefix[Rdf]

  import org.w3.banana.diesel._
  import rww.ui.foaf.Person.ops._


  case class PersonState(personPG: Option[PointedGraph[Rdf]],
                         edit: Boolean = false,
                         editText: String = "Edit")

  class Backend(t: BackendScope[Unit, PersonState]) {

  }

  val component = ReactComponentB[PointedGraph[Rdf]]("Person")
    .initialState(PersonState(None))
    .render((P, S, B) =>
    div(className := "clearfix center")(
      img(src := {
        val l = (P / foaf.depiction).toList
        println("xxx=" + l.map(_.pointer))
        val link = l.map(_.pointer).collectFirst{
          case URI(uri) =>  uri
        }.getOrElse("avatar-man.png")
        println(link)
        link
      }
      )
    )).build

  val el = document getElementById "eg1"

  @js.annotation.JSExport
  override def main(): Unit = {
    example1()
  }

  val bbl = URI("http://bblfish.net/people/henry/card#me")
  val bblDocUri = bbl.fragmentLess

  //start with a locally built graph, find picture
  def example1() = {
    val graph = (
      bbl.toPG
        -- foaf.depiction ->- "hello"
        -- foaf.depiction ->- URI("http://farm1.static.flickr.com/164/373663745_e2066a4950.jpg")
        -- foaf.depiction ->- URI("http://bblfish.net/pix/bfish.large.jpg")
      ).graph
    React.render(component(PointedGraph[Rdf](bbl, graph)), el)
  }

/* we don't have a Turtle parser in Plantain so give this a miss for the moment
  //parse graph from string then show picture
  def example2() = {
    //do a request on the internet to get the file for the above url
    val bblDoc =
      s"""
        |@prefix foaf: <http://xmlns.com/foaf/0.1/>   .
        |@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
        |
        |<#me> foaf:depiction <http://farm1.static.flickr.com/164/373663745_e2066a4950.jpg>,
        |                     <http://bblfish.net/pix/bfish.large.jpg> ;
        |      foaf:name "Henry";
        |      foaf:age "42"^^xsd:int ;
        |      foaf:near "England"@en .
      """.stripMargin

    import rww.rdf.store._


    //parse document above with Readers to get graph below
    import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

    val f = for {
      g <- turtleReader.read(toReader(bblDoc),bblDocUri.toString())
    // the following would be required, where it not that here reading already added the
    // graph to the store, but that needs to be fixed, by allowing also parsers to be
    // streaming
    //      _ <- appendToGraph(rww.rdf.jsstore, bblDocUri, g) //add to store
    //      graph <- getGraph(rww.rdf.jsstore,bblDocUri ) //get from store
    } yield {
      React.render(component(PointedGraph[Rdf](URI("#me"), g)), el)
    }
    f.value
  }
*/

//  def example3() = {
//    import JSStore._
//    //the run-now execution context should be fine as the two methods below work with callbacks
//    //that presumably uses javascripts task queue (
//    //todo: to be verified
//    import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
//    for {
//      i <- rdfstoreOps.loadRemote(jsstore, bblDocUri)
//      g <- JSStore.store.getGraph(JSStore.jsstore, bblDocUri)
//    } yield {
//      React.render(component(PointedGraph[Rdf](bbl, g)), el)
//    }
//  }
}
