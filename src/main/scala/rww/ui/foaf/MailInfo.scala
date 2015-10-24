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
    .renderP(($,P)=> {
    <.li()(
      <.div(style.titleCase)("Email"),
      <.div(style.content)(
        (for (
          mstr <- P.asString;
          mbx <- P.asURIStr
        ) yield  <.a(^.href := mbx)(mstr)).getOrElse("TODO")
      )
    )
  }).build

  def apply(mbox: Mbox, edit: Boolean=false) = {
    MailInfo(mbox)
  }

}
