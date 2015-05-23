package rww.ui.foaf

import java.net.{URI => jURI}

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import rww.ontology.Person
import rww.ui.foaf.{FoafStyles => style}

import scala.util.{Success, Try}
import scalacss.ScalaCssReact._

/**
 * Created by hjs on 17/05/2015.
 */
object PersonBasicInfo {
    import rww._

    val PersonBasicInfo = ReactComponentB[PProps[Person]]("PersonBasicInfo")
      .initialState(None)
      .render((P,S,B)=>{
        val person = P.about
        <.div(style.basic)(
          //todo: what should one do with multiple fields?
          person.name.headOption.map(EditableTextField(_,P.edit,"name",style.name)),
          //todo: idem
          person.givenName.headOption.map(EditableTextField(_,P.edit,"given name",style.surname)),
          //todo: can one extend the EditableTextField for URIs?
          person.workPlaceHomePage.toUriStr.map( host => Try( new jURI(host)  )) collectFirst {
            case Success(u) => <.div(style.company, ^.title := u.getHost)(u.getHost)
          } getOrElse <.div()
      )
    }).build

    def apply(person: Person, edit: Boolean) = {
      PersonBasicInfo(PProps(person,edit))
    }
}
