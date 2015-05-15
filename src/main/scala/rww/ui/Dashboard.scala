package rww.ui

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

/**
 * Created by hjs on 13/05/2015.
 */
object Dashboard {
  //todo: why is it important that this pass the Router?
  val component = ReactComponentB[MainRouter.Router]("Dashboard")
    .render(router => {
    // create dummy data for the chart
    <.div(
      // header, MessageOfTheDay and chart components
      <.h2("Dashboard"),
      URLBox(URLBox.Props(MainRouter.pages))
    )
  }).build



}
