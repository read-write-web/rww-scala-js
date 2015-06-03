package rww.ui.foaf

import java.net.{URI => jURI}

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import org.w3.banana.PointedGraph
import rww.ontology.Person
import rww.rdf.Named
import rww.ui.foaf.{FoafStyles => style}
import rww.ui.rdf.NPGPath

import scala.util.{Success, Try}
import scalacss.ScalaCssReact._

/**
 * Created by hjs on 17/05/2015.
 */
object PersonBasicInfo {
    import rww._
    import Rdf.ops._

    val PersonBasicInfo = ReactComponentB[WProps[Person]]("PersonBasicInfo")
      .initialState(None)
      .render((P,S,B)=>{
        val person = P.about
        <.div(style.basic)(
          //todo: what should one do with multiple fields?
          person.name.headOption.map(TextField(_,P.edit,"name",P.webAgent,style.name)),
          //todo: idem
          person.givenName.headOption.map(TextField(_,P.edit,"given name",P.webAgent,style.surname)),
          //todo: can one extend the EditableTextField for URIs?
          person.workPlaceHomePage collectFirst {
            case npg @ NPGPath(Named(_,PointedGraph(URI(u),_)),_)  =>
              URLField(npg,P.edit,"work place home page",P.webAgent,style.company)
          }
      )
    }).build

    def apply(props: WProps[Person]) = {
      PersonBasicInfo(props)
    }
}
