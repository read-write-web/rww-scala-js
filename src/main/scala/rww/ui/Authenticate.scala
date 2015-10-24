package rww.ui

import java.net.{URI => jURI}

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.prefix_<^._
import scalacss.ScalaCssReact._

import rww.store._
import rww.ui.foaf.WProps
import rww.ui.util.RxStateObserver
import spatutorial.client.components.GlobalStyles

/**
 * Created by hjs on 09/06/2015.
 */
object Authenticate {
  @inline private def bss = GlobalStyles.bootstrapStyles
  import rww.Rdf.ops._

  def fetch(p: WProps[List[jURI]],mode: CacheMode) = p.webAgent.fetch(p.about.head,mode)


  class Backend(t: BackendScope[WProps[List[jURI]], Option[RequestState]])
    extends RxStateObserver[RequestState](t)  {
    def auth(e: ReactEventI) = CallbackTo {
      unmount
      t.props.map{p=>
        observeAndSetState(Some(fetch(p,UnlessCached)))
        e.preventDefaultCB
      }
    }.void
  }

  val component = ReactComponentB[WProps[List[jURI]]]("Authenticate")
    .initialState_P[Option[RequestState]](p=>Some(fetch(p,CacheOnly)()))
    .backend(new Backend(_))
    .render( $ =>
    <.span()(
      <.h3("Select WebID"),
      <.form(^.onSubmit ==> $.backend.auth)(
        <.input(^.`type` := "submit", ^.value := "WebID Auth", bss.buttonXS)
      ),
      <.p($.state.get match {
        //S here is always a Some
        case ok: Ok => {
          ok.header("User").headOption.map { id =>
            val newId = URI(id)
            if ($.props.userConfig().id != newId)
              $.props.userConfig() = $.props.userConfig().copy(id = Some(newId))
            <.span("authenticated as ", <.a($.props.ctl.setOnClick(Component.uri(newId)))(id))
          }.getOrElse(<.span("could not authenticate"))
        }
        case HttpError(_,code, _, _) => <.span("Could not authenticate. Returned with " + code)
        case UnRequested(_) => <.span()
        case _ => <.span("Authenticating...")
      })
    )
    )
    .configure(OnUnmount.install)
    .build


  def apply(props: WProps[List[jURI]]) = component(props)
}
