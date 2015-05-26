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
  val PersonMoreInfo =  ReactComponentB[WProps[Person]]("PersonMoreInfo")
    .initialState(None)
    .render((P,S,B)=> {
      val p = P.about
      <.div(style.details)(
        <.div(^.className:="title",style.centerText,style.titleCase)("Details"),
        <.ul(style.clearfix,style.span3)(
          p.phone map ( PhoneInfo(_,P.edit) ),
          p.mbox map ( MailInfo(_,P.edit) ),
          p.home map (  x=> ContactLocationInfo(P.copy(about=("home", x)) )),
          p.office map ( x => ContactLocationInfo(P.copy(about=("office", x)) )),
          p.emergency map (x => ContactLocationInfo(P.copy(about=("emergency", x)) )),
          p.mobile map ( x => ContactLocationInfo(P.copy(about=("mobile", x)) )),
          p.account map ( x => AccountInfo(P.copy(about=x)) )
        )
      )
  })
//    .configure(LogLifecycle.verbose)
    .build

  def apply(p: WProps[Person]) = {
    PersonMoreInfo(p)
  }
}
