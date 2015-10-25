package rww.ui.util

import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.{BackendScope, Callback, CallbackOption}
import rx.Rx
import rx.ops._

object RxStateObserver {
}
//todo: need to genericise this more. Currently the State can only be Option[S]
abstract class RxStateObserver[S](scope: BackendScope[_, Option[S]])
  extends OnUnmount {

  def setState(rxo: Option[Rx[S]]): Callback = scope.setState(rxo.map(_()))


  def observe(rxo: Option[Rx[S]]): Callback = {
    for {
      obs <- CallbackOption.liftOption {
        rxo.map(rx =>
          //thanks to elacin@github for help
          rx.foreach(v => scope.setState(Some(v)).runNow(), true))
      }
       _ <- onUnmount { Callback ( obs.kill() ) }
    } yield ()
  }

  def observeAndSetState(rxo: Option[Rx[S]]) = {
    setState(rxo) >>
    observe(rxo)
  }

}
