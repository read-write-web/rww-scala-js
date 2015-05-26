package rww.ui.foaf

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.w3.banana.PointedGraph
import rww.Rdf
import rww.ontology._
import rww.store.WebView
import rww.ui.rdf.NPGPath

import scalacss.ScalaCssReact._


case class WProps[O](about: O,
                     edit: Boolean = false,
                     webView: WebView[Rdf])

object Profile {

  def apply(person: Person, webView: WebView[Rdf]) = {
    profile(WProps(person,false,webView))
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
    val newProps = P.copy(edit=S.edit)
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
//    .configure(LogLifecycle.short)
    .build



}
