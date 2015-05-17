package rww.ui

import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.all._
import rww.Rdf
import rww.ontology.Person
import rww.store.WebView
import rww.ui.foaf.{PProps, Profile}
import rww.ui.rdf.NPGPath
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
        println("Window: Loading")
        Loading(P.pointer)
      }
      case Some(npg) => {
        println("Window Profile")
      //here of course one could choose the type of component, depending on the pg
        Profile(PProps(Person(npg)))
      }
    }
  })
    .componentDidMount(_.backend.mounted)
    .build

  def apply(pointer: Rdf#URI, cache: Var[WebView[Rdf]]) =
    Window(Props(pointer, cache))
}
