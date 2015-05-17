package rww.ui.foaf

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.all._
import rww.ontology.ContactLocation
import rww.ui.foaf.{FoafStyles=>style}
import scalacss.ScalaCssReact._

/**
 * Created by hjs on 17/05/2015.
 */
object PhoneLocationInfo {
  val ContactLocationInfo = ReactComponentB[PProps[(String,ContactLocation)]]("ContactLocationInfo")
    .initialState(None)
    .render((P,S,B)=> {
    val (homeTp,cl) = P.obj
    li()(
      span(style.clearfix,style.span3)(
        div(style.titleCase)(homeTp) ,
        for (addr<-cl.address) yield
        span()(AddressInfo(PProps(addr)))
      )
    )
  }).build

  def apply(props: PProps[(String,ContactLocation)]) = ContactLocationInfo(props)
}