package rww.ui.foaf

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import rww.ontology.Tel
import rww.ui.foaf.{FoafStyles=>style}

import scalacss.ScalaCssReact._

/**
 * Created by hjs on 17/05/2015.
 */
object PhoneInfo {
  val PhoneInfo = ReactComponentB[Tel]("Phone Info")
    .initialState(None)
    .render((P,S,B)=> {
    val m = P
    <.li(style.floatLeft)(
      <.div(style.titleCase)("Phone"),
      <.div(style.content)(
        (for (
          mstr <- m.asString;
          mbx <- m.asURIStr
        ) yield  <.a(^.href := mbx)(mstr)).getOrElse("TODO")
      )
    )
  }).build

  def apply(tel: Tel) = {
    println("In phone info constructor")
    PhoneInfo(tel)
  }

}
