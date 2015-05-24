package rww.ui.foaf

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.extra.LogLifecycle
import rww.ontology.Person
import rww.ui.foaf.{FoafStyles => style}
import japgolly.scalajs.react.vdom.prefix_<^._

import scalacss.ScalaCssReact._

/**
 * Created by hjs on 17/05/2015.
 */
object PersonMoreInfo {
  val PersonMoreInfo =  ReactComponentB[PProps[Person]]("PersonMoreInfo")
    .initialState(None)
    .render((P,S,B)=> {
      val p = P.about
      <.div(style.details)(
        <.div(^.className:="title",style.centerText,style.titleCase)("Details"),
        <.ul(style.clearfix,style.span3)(
          p.phone map ( PhoneInfo(_,P.edit) ),
          p.mbox map ( MailInfo(_,P.edit) ),
          p.home map ( ContactLocationInfo("home", _, P.edit) ),
          p.office map ( ContactLocationInfo("office", _, P.edit) ),
          p.emergency map ( ContactLocationInfo("emergency", _, P.edit) ),
          p.mobile map ( ContactLocationInfo("mobile", _, P.edit) ),
          p.account map ( AccountInfo(_, P.edit) )
        )
      )
  })
//    .configure(LogLifecycle.verbose)
    .build

  def apply(p: Person, edit: Boolean) = {
    PersonMoreInfo(PProps(p,edit))
  }
}
