package rww.ui.foaf

import org.w3.banana._
import rww._

import scala.scalajs.js
import org.scalajs.dom.{HTMLInputElement, console, document, window, Node}

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.ReactVDom._
import japgolly.scalajs.react.vdom.ReactVDom.all._

import org.w3.banana

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
     val person: PointedGraph[Rdf] = (
       URI("http://bblfish.net/people/henry/card#me").toPG
         -- foaf.depiction ->- URI("http://farm1.static.flickr.com/164/373663745_e2066a4950.jpg")
         -- foaf.depiction ->- URI("http://bblfish.net/pix/bfish.large.jpg")
       )
    val el = document getElementById "eg1"
    React.render(component(person),el)
  }
}
