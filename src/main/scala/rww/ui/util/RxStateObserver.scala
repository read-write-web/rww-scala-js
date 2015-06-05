package rww.ui.util

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react.extra.OnUnmount
import rx.Rx
import rx.ops._

object RxStateObserver {
}
//todo: need to genericise this more. Currently the State can only be Option[S]
abstract class RxStateObserver[S](scope: BackendScope[_, Option[S]])
  extends OnUnmount {

  def setState(rxo: Option[Rx[S]]): Unit = scope.setState(rxo.map(_()))


  def observe(rxo: Option[Rx[S]]): Unit = {
    val obs = rxo.map(rx => rx.foreach(v => {
      scope.setState(Some(v))
    },true))
    onUnmount {
      obs.map(o => o.kill())
    }
  }

  def observeAndSetState(rxo: Option[Rx[S]]): Unit = {
    setState(rxo)
    observe(rxo)
  }

}
