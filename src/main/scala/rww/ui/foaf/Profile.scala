package rww.ui.foaf

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.w3.banana.PointedGraph
import rww.ontology._
import rww.ui.rdf.NPGPath

import scalacss.ScalaCssReact._


case class PProps[O](about: O,
                    edit: Boolean = false)

object Profile {

  def apply(person: Person) = {
    profile(person)
  }

  import rww._

  case class RelProps(subj: PointedGraph[Rdf], rel: Rdf#URI, thizs: Seq[PointedGraph[Rdf]])
  case class State(edit: Boolean=false) {
    def text = if (!edit) "Edit" else "Save"
  }
  class ProfileBackend($: BackendScope[Person, State]) {

    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      $.modState(s=>State(!s.edit))
    }
    def handleCancel(e: ReactEventI) = {
      e.preventDefault()
      $.modState(_=>State())
    }
  }

  import rww.ui.foaf.{FoafStyles => style}

  val profile = ReactComponentB[Person]("Profile")
    .initialState(State())
    .backend(new ProfileBackend(_))
    .render((P, S, B) => {
    // import shapeless.singleton.syntax._ <- use this when using styleC
    <.div(style.clearfix,style.center,style.body)(
      <.div(style.editProfile, ^.onClick==> B.handleSubmit)(
        S.text,
        if (S.edit) <.a(^.onClick==>B.handleCancel)("cancel") else EmptyTag
      ),
      Image(P,S.edit),
      PersonBasicInfo(P,S.edit),
      PersonMoreInfo(P,S.edit),
      FOAFInfo(P)
    )
  })
//    .configure(LogLifecycle.short)
    .build



}
