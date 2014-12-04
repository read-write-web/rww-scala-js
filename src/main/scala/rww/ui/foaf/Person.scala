package rww.ui.foaf

import org.w3.banana._
import rww._

import scala.scalajs.js
import org.scalajs.dom.{HTMLInputElement, console, document, window, Node}

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.ReactVDom._
import japgolly.scalajs.react.vdom.ReactVDom.all._

import org.w3.banana
import org.w3.banana.rdfstore.Store
import org.w3.banana.rdfstorew.RDFStoreW

object Person extends js.JSApp {
  import rww.rdf._
  implicit val ops: RDFOps[Rdf] = rww.rdf.ops
  val foaf = FOAFPrefix[Rdf]
  import ops._
  import banana.diesel._
  import banana.syntax._

  case class PersonState(personPG: Option[PointedGraph[Rdf]],
                         edit: Boolean=false,
                         editText: String="Edit")

  class Backend(t: BackendScope[Unit, PersonState]) {

  }

  val component = ReactComponentB[PointedGraph[Rdf]]("Person")
    .initialState(PersonState(None))
    .render((P, S,B) =>
    div(className := "clearfix center")(
      img(src := (P/foaf.depiction).filter(_.pointer.fold(uri=>true,bn=>false,lit=>false))
        .headOption.map(_.pointer.toString).getOrElse("img/avatar.png"))
    )
    ).build

  @js.annotation.JSExport
  override def main(): Unit = {
    import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
     val bbl = URI("http://bblfish.net/people/henry/card#me")

     val futureFoaf = rww.rdf.store.getGraph(rww.rdf.jsstore,URI("http://bblfish.net/people/henry/card.ttl"))

//     val person: PointedGraph[Rdf] = (
//       URI("http://bblfish.net/people/henry/card#me").toPG
//         -- foaf.depiction ->- URI("http://farm1.static.flickr.com/164/373663745_e2066a4950.jpg")
//         -- foaf.depiction ->- URI("http://bblfish.net/pix/bfish.large.jpg")
//       )
    val el = document getElementById "eg1"
    futureFoaf.map{ graph=>
      console.log("graph=",graph.asInstanceOf[js.Any])
      React.render(component(PointedGraph[Rdf](bbl,graph)),el) }
  }
}
