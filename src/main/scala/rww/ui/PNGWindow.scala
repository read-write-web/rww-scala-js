package rww.ui

import japgolly.scalajs.react.{ReactElement, BackendScope, ReactComponentB}
import org.w3.banana.PointedGraph
import rww.ontology.Person
import rww.rdf.Named
import rww.store._
import rww.ui.foaf.{Profile, WProps}
import rww.ui.rdf.NPGPath
import rww.ui.util.RxStateObserver
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.util.{Failure, Success}

/**
 * Pointed Named Graph Window
 */
object PNGWindow {
  import rww._


  class Backend(t: BackendScope[WProps[Rdf#URI], Option[RequestState]])
    extends RxStateObserver[RequestState](t)  {
    def mounting(): Unit = observe(Some(t.props.webAgent.fetch(t.props.about)))
  }


  val Window = ReactComponentB[WProps[Rdf#URI]]("Window")
    //todo: would it be better if initial state this was settable by props for testing?
    .initialState[Option[RequestState]](None)
    .backend(new Backend(_))
    .render((P, S, B) => {
      val r: Option[ReactElement] = S map {
        case Ok(_, url, _, _, parsed) => {
          parsed match {
            case scala.util.Success(graph) => {
              //here of course one could choose the type of component, depending on the npg
              Profile(
                Person(NPGPath(Named[Rdf, PointedGraph[Rdf]](url, PointedGraph[Rdf](P.about, graph)))),
                P.webAgent
              )
            }
            case scala.util.Failure(e) => <.div()("error: "+e)
          }
        }
        //Downloading(_) | Redirected(_) | HttpError(_, _, _) |
        case  _ => Loading(P.about)
      }
      r.getOrElse(<.div("huh?"))
    })
    .componentWillMount(_.backend.mounting)
//    .configure(OnUnmount.install)
    .build

  def apply(pointer: Rdf#URI, ws: WebAgent) =
    Window(WProps(pointer,ws))
}
