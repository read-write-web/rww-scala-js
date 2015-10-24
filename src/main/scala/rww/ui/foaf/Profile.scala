package rww.ui.foaf

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.LogLifecycle
import japgolly.scalajs.react.vdom.prefix_<^._
import org.w3.banana.PointedGraph
import rww.ontology._

import scalacss.ScalaCssReact._


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
    .renderPS(($, P, S) => {
    val newProps: WProps[Person] = P.copy(edit=S.edit)
    // import shapeless.singleton.syntax._ <- use this when using styleC
    <.div(style.clearfix,style.center,style.body)(
      <.div(style.editProfile, ^.onClick==> $.backend.handleSubmit)(
        S.text,
        if (S.edit) <.a(^.onClick==>$.backend.handleCancel)("cancel") else EmptyTag
      ),
      <.div()(
        Image(newProps.copy(about=P.about.depiction.headOption)),
        PersonBasicInfo(newProps)
      ),
      PersonMoreInfo(newProps),
      WebIDInfo(P),
      FOAFInfo(newProps)
    )
  })
    .configure(LogLifecycle.short)
    .build

}

object WebIDInfo {

  import rww.Rdf.ops._
  import rww.ui.foaf.{FoafStyles => style}

  val component = ReactComponentB[WProps[Person]]("Profile")
    .stateless
    .renderP(($,P) =>
    P.about.npg.pg.pointer match {
      case URI(url) =>
        <.div(style.clearfix, style.webidbar)(
          <.a(^.href := url)(
            <.img(^.alt := "Web ID logo", style.floatLeft, ^.src := "img/webid.png")
          ),
          <.div(style.floatLeft, style.webidAddress)(
            <.div(style.titleCase)("Web-ID"),
            url)
      )
      case BNode(t) => <.div()
      case Literal(t, _, _) => <.div()
    }).build

  def apply(person: WProps[Person]) = {
    component(person)
  }


}

