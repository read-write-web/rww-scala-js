package rww.ui

import java.net.{URI => jURI}

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.prefix_<^._
import ScalazReact._
import rww.ontology.Person
import rww.store._
import rww.ui.foaf.WProps
import rww.ui.util.RxStateObserver
import spatutorial.client.components.GlobalStyles

import scalacss.ScalaCssReact._

/**
 * Created by hjs on 09/06/2015.
 */
object Authenticate {
  @inline private def bss = GlobalStyles.bootstrapStyles
  import rww.Rdf.ops._

  def fetch(p: WProps[List[jURI]],mode: CacheMode) = p.webAgent.fetch(p.about.head,mode)


  class Backend(t: BackendScope[WProps[List[jURI]], Option[RequestState]])
    extends RxStateObserver[RequestState](t)  {

    def auth(e: ReactEventI): Callback = {
      for {
        _ <- e.preventDefaultCB
        p <- t.props
      } yield observeAndSetState(Some(fetch(p, UnlessCached)))
    }
  }

  val component = ReactComponentB[WProps[List[jURI]]]("Authenticate")
    .initialState[Option[RequestState]](None)
    .backend(new Backend(_))
    .renderS { ($,s) =>
      println("~~~Authenticate.render with p.state=" + $.state)
      <.span()(
      <.h3("Select WebID"),
      <.form(^.onSubmit ==> $.backend.auth)(
        <.input(^.`type` := "submit", ^.value := "WebID Auth", bss.buttonXS)
      ),
      $.state.map { requestState =>
        <.p(requestState match {
          //S here is always a Some
          case ok: Ok => {
            println("~~~>Ok:" + ok)
            ok.header("User").headOption.map { id =>
              val newId = URI(id)
              if ($.props.userConfig().id != newId)
                $.props.userConfig() = $.props.userConfig().copy(id = Some(newId))
              <.span("authenticated as ", <.a($.props.ctl.setOnClick(Component.uri(newId)))(id))
            }.getOrElse(<.span("could not authenticate"))
          }
          case HttpError(_, code, _, _) => <.span("Could not authenticate. Returned with " + code)
          case UnRequested(_) => <.span()
          case _ => <.span("Authenticating...")
        })
      }
      )
    }
    .configure(OnUnmount.install)
    .build


  def apply(props: WProps[List[jURI]]) = component(props)
}
