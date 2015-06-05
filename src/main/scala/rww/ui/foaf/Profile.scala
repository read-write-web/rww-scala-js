package rww.ui.foaf

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.LogLifecycle
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import org.w3.banana.PointedGraph
import rww.ontology._
import rww.store.WebAgent
import rww.ui.RwwPages

import scalacss.ScalaCssReact._


case class WProps[O](about: O,
                     webAgent: WebAgent,
                     ctl: RouterCtl[RwwPages],
                     edit: Boolean = false
                     )

object Profile {

  def apply(person: WProps[Person]) = {
    profile(person)
  }

  import rww._

  case class RelProps(subj: PointedGraph[Rdf], rel: Rdf#URI, thizs: Seq[PointedGraph[Rdf]])
  case class State(edit: Boolean=false) {
    def text = if (!edit) "Edit" else "Save"
  }
  class ProfileBackend($: BackendScope[WProps[Person], State]) {

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

  val profile = ReactComponentB[WProps[Person]]("Profile")
    .initialState(State())
    .backend(new ProfileBackend(_))
    .render((P, S, B) => {
    val newProps: WProps[Person] = P.copy(edit=S.edit)
    // import shapeless.singleton.syntax._ <- use this when using styleC
    <.div(style.clearfix,style.center,style.body)(
      <.div(style.editProfile, ^.onClick==> B.handleSubmit)(
        S.text,
        if (S.edit) <.a(^.onClick==>B.handleCancel)("cancel") else EmptyTag
      ),
      Image(newProps.copy(about=P.about.depiction.headOption)),
      PersonBasicInfo(newProps),
      PersonMoreInfo(newProps),
      FOAFInfo(newProps)
    )
  })
    .configure(LogLifecycle.short)
    .build



}
