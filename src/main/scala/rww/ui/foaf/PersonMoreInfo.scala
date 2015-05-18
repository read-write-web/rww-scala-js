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
      val p = P.obj
      <.div(style.details)(
        <.div(^.className:="title",style.centerText,style.titleCase)("Details"),
        <.ul(style.clearfix,style.span3)(
          p.phone.map( PhoneInfo(_) ),
          p.mbox.map( MailInfo(_) ),
          p.home.map( ContactLocationInfo("home", _) ),
          p.office.map ( ContactLocationInfo("office", _) ),
          p.emergency.map ( ContactLocationInfo("emergency", _) ),
          p.mobile.map ( ContactLocationInfo("mobile", _) ),
          p.account.map ( AccountInfo(_) )
        )
      )
  })
//    .configure(LogLifecycle.verbose)
    .build

  def apply(p: PProps[Person]) = {
    println("in PersonMoreInfo constructor")
    PersonMoreInfo(p)
  }
}
