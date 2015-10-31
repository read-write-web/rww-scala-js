package rww.ui.util

import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.{BackendScope, Callback}
import rx.Rx
import rx.ops._

/**
 * Created by hjs on 16/05/2015.
 */
abstract class RxObserver[BS <: BackendScope[_, _]](scope: BS) extends OnUnmount {
  def observe[T](rx: Rx[T]): Unit = {
    val obs = rx.foreach(_ => scope.forceUpdate)
    // stop observing when unmounted
    onUnmount{
      Callback {
        obs.kill()
      }
    }
  }
}

//@codingismy11to7 proposed this:
//
//trait RxObserver extends OnUnmount {
//  def observe[S](rx: Rx[S])($: CompState.ReadCallbackWriteCallbackOps[S]) =
//    CallbackTo(rx foreach (r ⇒ $.setState(r).runNow())) >>= (obs ⇒ onUnmount(Callback(obs.kill())))
//
//  def observeT[R, S](rx: Rx[R])(xform: (R) ⇒ S)($: CompState.ReadCallbackWriteCallbackOps[S]) =
//    CallbackTo(rx foreach (r ⇒ $.setState(xform(r)).runNow())) >>= (obs ⇒ onUnmount(Callback(obs.kill())))
//
//  def observeCB[T](rx: Rx[T])(f: (T) ⇒ Callback) =
//    CallbackTo(rx foreach (r ⇒ f(r).runNow())) >>= (obs ⇒ onUnmount(Callback(obs.kill())))
//
//  def clearAllObservations = unmount
//}
//
//object RxObserver {
//  def install[P, S, B <: RxObserver, N <: TopNode] = OnUnmount.install[P, S, B, N]
//}
//
// used like this:
//
//case class Props(r1: Rx[Int], r2: Rx[Boolean])
//@Lenses case class State(f1: Int, f2: Boolean)
//class Backend($: BackendScope[Props, State]) extends RxObserver {
//  def onMount(P: Props) =
//    observe(P.r1)($.zoomL(State.f1)) >>
//      observe(P.r2)($.zoomL(State.f2))
//  def render(s: State) = <.span(s"s.f1=${s.f1}; s.f2=${s.f2}")
//}
//val component = ReactComponentB[Props]("MyComponent")
//.initialState_P(P => State(P.r1(), P.r2()))
//.renderBackend[Backend]
//.componentDidMount($ => $.backend.onMount($.props))
//.configure(RxObserver.install)
//.build
//
//
//and nothing too difficult or opaque, but just to explain:
//if your rx should go straight to state (Rx[StateType] in props, $: BackendScope[Props, StateType]) then you don't have to .zoomL, it'd just be observe(P.rx)($)
//observeT just lets you transform each value from the Rx before setting state
//observeCB just lets you run an arbitrary Callback instead of modifying state
//the other thing that's a hassle (not specific to my implementation, really, and why i really want to check out all of @japgolly 's ReusableVars and everything), is that you can get new Props, so you gotta figure out during willReceiveProps if you have to leave things as-is, or if you need to clearAllObservations >> onMount(nextProps) if you got new Rx's