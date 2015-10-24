package rww.ui

import java.net.{URI => jURI}

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import rww.ui.foaf.WProps
import rww.ui.{URLBoxComponent => UBox}

/**
 * Created by hjs on 13/05/2015.
 */
object Dashboard {
  //todo: why is it important that this pass the Router?
  val component = ReactComponentB[WProps[(String,List[jURI])]]("Dashboard")
    .renderP(($,P) => {
    // create dummy data for the chart
    <.div(
      // header, MessageOfTheDay and chart components
      <.h2("Dashboard"),
      UBox(P.copy(about=P.about._1)),
      Authenticate(P.copy(P.about._2))
    )
  }).build


  def apply(p: WProps[(String,List[jURI])]) = component(p)
}
