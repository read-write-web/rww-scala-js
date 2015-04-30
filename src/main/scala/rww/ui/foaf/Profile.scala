package rww.ui.foaf

import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import org.w3.banana.PointedGraph
import rww.ui.foaf.TestApp.Rdf

import scalacss.ScalaCssReact._


object Profile {


  case class PersonProps(personPG: PointedGraph[Rdf],
                         edit: Boolean = false,
                         editText: String = "Edit")

  class Backend(t: BackendScope[Unit, PersonProps]) {

  }


  val component = ReactComponentB[PersonProps]("Person")
    .initialState(None)
    .render((P, S, B) => {
    val x = Person(P.personPG)
    if (P.edit) p("in edit mode")
    else div(className := "basic")(
    x.name.headOption.map(name => div(FoafStyles.name, title := name)(name)) getOrElse div(), {
      val n = x.givenName.headOption.getOrElse("(unknown)")
      div(FoafStyles.surname, title := n)(n)
    },
    x.workPlaceHomePage.headOption.map { hp =>
      div(FoafStyles.company, title := hp.toString)(hp.toString)
    } getOrElse
      div(),
    //      FoafStyles.picture('outer)( outerStyle =>
    //        _('image)( imageStyle =>
    div(FoafStyles.picTop)(
      img(img, src := x.depiction.headOption.getOrElse("avatar-man.png"))
    )
    //        )
    //      )
    )
  }).build

}
