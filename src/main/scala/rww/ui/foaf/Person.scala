package rww.ui.foaf

import org.w3.banana._
import rww._

import scala.scalajs.js
import org.scalajs.dom.{HTMLInputElement, console, document, window, Node}

import japgolly.scalajs.react.{ReactComponentB,_}
import japgolly.scalajs.react.vdom.ReactVDom._
import japgolly.scalajs.react.vdom.ReactVDom.all._

import org.w3.banana
import org.w3.banana.rdfstore.rjs.Store
import org.w3.banana.rdfstorew.{JSStore, RDFStoreW}

object Person extends js.JSApp {

  import rww.rdf._

  implicit val ops: RDFOps[Rdf] = rww.rdf.ops
  val foaf = FOAFPrefix[Rdf]

  import ops._
  import banana.diesel._
  import banana.syntax._


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
        l.filter { _.pointer.fold(uri => true, bn => false, lit => false)
        }.headOption.map(_.pointer.toString).getOrElse("http://pixabay.com/static/uploads/photo/2013/07/13/12/07/avatar-159236_640.png")
      }
      )
    )).build

  val el = document getElementById "eg1"

  @js.annotation.JSExport
  override def main(): Unit = {
    example3()
  }

  val bbl = URI("http://bblfish.net/people/henry/card#me")
  val bblDocUri = bbl.fragmentLess

  //start with a locally built graph, find picture
  def example1() = {
    val graph = (
      bbl.toPG
        -- foaf.depiction ->- URI("http://farm1.static.flickr.com/164/373663745_e2066a4950.jpg")
        -- foaf.depiction ->- URI("http://bblfish.net/pix/bfish.large.jpg")
      ).graph
    val strFuture = rww.rdf.turtleWriter.asString(graph, "")
    React.render(component(PointedGraph[Rdf](bbl, graph)), el)
  }

  //parse graph from string then show picture
  def example2() = {
    //do a request on the internet to get the file for the above url
    val bblDoc =
      """
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

  def example3() = {
    import JSStore._
    //the run-now execution context should be fine as the two methods below work with callbacks
    //that presumably uses javascripts task queue (
    //todo: to be verified
    import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
    for {
      i <- rdfstoreOps.loadRemote(jsstore, bblDocUri)
      g <- JSStore.store.getGraph(JSStore.jsstore, bblDocUri)
    } yield {
      React.render(component(PointedGraph[Rdf](bbl, g)), el)
    }
  }
}
