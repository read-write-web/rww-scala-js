package rww.ui.foaf

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.extra.LogLifecycle
import japgolly.scalajs.react.vdom.all._
import org.w3.banana.PointedGraph
import rww.ontology._

import scalacss.ScalaCssReact._


case class PProps[O](obj: O,
                    edit: Boolean = false,
                    editText: String = "Edit")

object Profile {

  def apply(props: PProps[Person]) = {
    profile(props)
  }

  import rww._

  case class RelProps(subj: PointedGraph[Rdf], rel: Rdf#URI, thizs: Seq[PointedGraph[Rdf]])


  import rww.ui.foaf.{FoafStyles => style}

  val profile = ReactComponentB[PProps[Person]]("Profile")
    .initialState(None)
    .render((P, S, B) => {
    val person = P.obj
    // import shapeless.singleton.syntax._ <- use this when using styleC
    if (P.edit) p("in edit mode")
    else div(style.clearfix,style.center,style.body)(
      Image(person),
      PersonBasicInfo(P),
      PersonMoreInfo(P)
    )
  })
    .configure(LogLifecycle.verbose)
    .build



}
