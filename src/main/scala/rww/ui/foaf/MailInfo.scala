package rww.ui.foaf

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import rww.ontology.Mbox
import rww.ui.foaf.{FoafStyles => style}
import scalacss.ScalaCssReact._


/**
 * Created by hjs on 17/05/2015.
 */
object MailInfo {
  val MailInfo = ReactComponentB[Mbox]("Mail Info")
    .initialState(None)
    .render((P,S,B)=> {
    val m = P
    <.li(style.floatLeft)(
      <.div(style.titleCase)("Email"),
      <.div(style.content)(
        (for (
          mstr <- m.asString;
          mbx <- m.asURIStr
        ) yield  <.a(^.href := mbx)(mstr)).getOrElse("TODO")
      )
    )
  }).build

  def apply(mbox: Mbox) = {
    println("in mail info constructor")
    MailInfo(mbox)
  }

}