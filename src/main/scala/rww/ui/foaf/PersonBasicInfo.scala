package rww.ui.foaf

import japgolly.scalajs.react.vdom.all._
import org.scalajs.dom._
import japgolly.scalajs.react.{ ReactComponentB, _ }
import org.w3.banana.plantain.Plantain.ops._
import rww.ui.Util

object PersonBasicInfo {

  def apply(props: PersonProps, state: PersonState, backend: PersonBackend) = PersonBasicInfo((props, state, backend))

  def drawInfo(graph: PersonProps, edit: Boolean, backend: PersonBackend) = {
    div(className := "basic") {
      val name = Util.getFirstLiteral(graph, FOAF.name, "(name missing)").toString
      val givenname = Util.getFirstLiteral(graph, FOAF.givenname, "(givenname missing)").toString
      val company = Util.getFirstUri(graph, FOAF.workplaceHomepage, "workplaceHomepage missing")

      if (!edit) {
        div(className := "name title-case")(name) ::
          div(className := "surname title-case")(givenname) ::
          div(className := "company")(company) ::
          Nil
      } else {
        div(className := "name title-case")(form(onSubmit ==> backend.handleSubmit)(input(tpe := "text", placeholder := "Enter name", onChange ==> backend.onTextChange(FOAF.name)(Literal(_)), value := name))) ::
          div(className := "surname title-case")(form(onSubmit ==> backend.handleSubmit)(input(tpe := "text", placeholder := "Enter givenname", onChange ==> backend.onTextChange(FOAF.givenname)(Literal(_)), value := givenname))) ::
          div(className := "company")(form(onSubmit ==> backend.handleSubmit)(input(tpe := "text", placeholder := "Enter company website", onChange ==> backend.onTextChange(FOAF.workplaceHomepage)(URI(_)), value := company))) ::
          Nil
      }
    }
  }

  private val PersonBasicInfo = ReactComponentB[(PersonProps, PersonState, PersonBackend)]("PersonBasicInfo")
    .render(P => {
      div(className := "basic") {

        val (props: PersonProps, state: PersonState, backend: PersonBackend) = P

        // First time load graph from props- subsequent times load it from state
        state.personPG match {
          case None        => drawInfo(props, state.edit, backend)
          case Some(graph) => drawInfo(graph, state.edit, backend)
        }
      }
    }).build
}