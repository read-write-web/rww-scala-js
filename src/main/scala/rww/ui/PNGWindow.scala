package rww.ui

import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, ReactElement}
import org.w3.banana.PointedGraph
import rww.ontology.Person
import rww.rdf.Named
import rww.store._
import rww.ui.foaf.{Profile, WProps}
import rww.ui.rdf.NPGPath
import rww.ui.util.RxStateObserver

/**
 * Pointed Named Graph Window
 */
object PNGWindow {
  import rww._


  class Backend(t: BackendScope[WProps[Rdf#URI], Option[RequestState]])
    extends RxStateObserver[RequestState](t)  {
    def mounting(props: WProps[Rdf#URI]) = observeAndSetState(Some(props.webAgent.fetch(props.about)))
  }


  val Window = ReactComponentB[WProps[Rdf#URI]]("Window")
    //todo: would it be better if initial state this was settable by props for testing?
    .initialState[Option[RequestState]](None)
    .backend(new Backend(_))
    .renderPS(($, P, S) => {
      val r: Option[ReactElement] = S map { x =>
        x match {
          case Ok(_, url, _, _, parsed) => {
            parsed match {
              case scala.util.Success(graph) => {
                //here of course one could choose the type of component, depending on the npg
                Profile(P.copy(about =
                  Person(NPGPath(Named[Rdf, PointedGraph[Rdf]](url, PointedGraph[Rdf](P.about, graph))))
                ))
              }
              case scala.util.Failure(e) => <.div()("error: " + e)
            }
          }
          //Downloading(_) | Redirected(_) | HttpError(_, _, _) |
          case _ => Loading(P.about)
        }
      }
      r.getOrElse(<.div("huh?"))
    })
    .componentWillReceiveProps( f => Callback {
      f.$.backend.unmount
      f.$.backend.mounting(f.nextProps)
    })
    .componentWillMount(f => {
      Callback(f.backend.mounting(f.props))
    })
    .configure(OnUnmount.install)
    .build

  def apply(props: WProps[Rdf#URI]) =
    Window(props)
}
