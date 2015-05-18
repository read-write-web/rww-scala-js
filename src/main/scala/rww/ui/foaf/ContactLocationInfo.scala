package rww.ui.foaf

import japgolly.scalajs.react.ReactComponentB
import rww.ontology.{ContactLocation, Person}
import japgolly.scalajs.react.vdom.prefix_<^._
import scalacss.ScalaCssReact._
import rww.ui.foaf.{FoafStyles => style}

/**
 * Created by hjs on 17/05/2015.
 */
object ContactLocationInfo {
  val ContactLocationInfo = ReactComponentB[(String,ContactLocation)]("ContactLocationInfo")
    .initialState(None)
    .render((P,S,B)=> {
    val (homeTp,cl) = P
    <.li()(
      <.span(style.clearfix,style.span3)(
        <.div(style.titleCase)(homeTp) ,
        for (addr<-cl.address) yield <.span()(AddressInfo(PProps(addr)))
      )
    )
  }).build

  def apply(locationType: String, loc: ContactLocation) = {
    ContactLocationInfo((locationType,loc))
  }
}
