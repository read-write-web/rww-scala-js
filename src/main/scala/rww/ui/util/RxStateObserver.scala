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

  def observe(rxo: Option[Rx[S]]): Unit = {
    val obs = rxo.map(rx => rx.foreach(v => {
      scope.setState(Some(v))
    }))
    // stop observing when unmounted
    onUnmount {
      obs.map(o => o.kill())
    }
  }

}
