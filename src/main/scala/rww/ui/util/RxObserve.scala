package rww.ui.util

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react.extra.OnUnmount
import rx.Rx
import rx.ops._

/**
 * Created by hjs on 16/05/2015.
 */
abstract class RxObserver[BS <: BackendScope[_, _]](scope: BS) extends OnUnmount {
  def observe[T](rx: Rx[T]): Unit = {
    val obs = rx.foreach(_ => scope.forceUpdate())
    // stop observing when unmounted
    onUnmount{
      obs.kill()
    }
  }
}
