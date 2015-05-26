package rww.ui

import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import rww.ontology.Person
import rww.store.WebView
import rww.ui.foaf.{WProps, Profile}
import rww.ui.util.RxObserver
import rx._

/**
 * Pointed Named Graph Window
 */
object PNGWindow {
  import rww._
  case class Props(pointer: Rdf#URI, cache: Var[WebView[Rdf]])


  class Backend(t: BackendScope[Props, Unit]) extends RxObserver(t) {
    def mounted(): Unit =
      observe(t.props.cache)
  }

  val Window = ReactComponentB[Props]("Window")
    .initialState(())
    .backend(new Backend(_))
    .render((P, S, B) => {
    P.cache().get(P.pointer) match {
      case None => {
        Loading(P.pointer)
      }
      case Some(npg) => {
      //here of course one could choose the type of component, depending on the npg
        Profile(Person(npg),P.cache())
      }
    }
  })
    .componentDidMount(_.backend.mounted)
    .configure(OnUnmount.install)
    .build

  def apply(pointer: Rdf#URI, cache: Var[WebView[Rdf]]) =
    Window(Props(pointer, cache))
}
