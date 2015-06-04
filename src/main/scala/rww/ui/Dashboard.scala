package rww.ui

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

import java.net.{URI=>jURI}
import rww.ui.{URLBoxComponent=>UBox}

import scalaz.effect.IO

/**
 * Created by hjs on 13/05/2015.
 */
object Dashboard {
  //todo: why is it important that this pass the Router?
  val component = ReactComponentB[UBox.Props]("Dashboard")
    .render((P) => {
    // create dummy data for the chart
    <.div(
      // header, MessageOfTheDay and chart components
      <.h2("Dashboard"),
      UBox(P)
    )
  }).build


  def apply(u: String, submitUrl: jURI=>IO[Unit]) = component(UBox.Props(u,submitUrl))
}
