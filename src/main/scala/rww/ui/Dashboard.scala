package rww.ui

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

import java.net.{URI=>jURI}

import rx.Var

import scala.collection.immutable.ListSet

/**
 * Created by hjs on 13/05/2015.
 */
object Dashboard {
  //todo: why is it important that this pass the Router?
  val component = ReactComponentB[Var[ListSet[jURI]]]("Dashboard")
    .render((P) => {
    // create dummy data for the chart
    <.div(
      // header, MessageOfTheDay and chart components
      <.h2("Dashboard"),
      URLBoxComponent(URLBoxComponent.Props(P().headOption,
        uri => P() = P() + uri ))
    )
  }).build



  def apply(uris: Var[ListSet[jURI]]) = component(uris)
}
