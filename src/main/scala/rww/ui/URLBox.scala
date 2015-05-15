package rww.ui


import java.net.{URISyntaxException, URI}

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.prefix_<^._
import rx.Var
import spatutorial.client.components.Bootstrap.CommonStyle
import scalacss.ScalaCssReact._
import spatutorial.client.components.GlobalStyles

/**
 * Created by hjs on 13/05/2015.
 */
object URLBox {
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(uri: Var[List[URI]])
  case class State(url: String,errmsg:String="")

  class Backend(t: BackendScope[Props, State]) extends OnUnmount {

    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      try {
        val uri = new URI(t.state.url)
        MainRouter.ws.fetch(rww.Rdf.ops.URI(t.state.url)) //todo: move elswhere
        t.props.uri.update(uri::t.props.uri())  //todo: use the message passing pattern from tutorial?
      } catch {
        case e: URISyntaxException => {
          t.modState(s => s.copy(errmsg = e.getMessage))
        }
        case other: Throwable => println(other)
      }
    }

    def clearInput(e: ReactEvent) =
      t.modState(_ => State(""))

    def onChange(e: ReactEventI) =
      t.modState(s => State(e.target.value))

  }

  private val URLBox = ReactComponentB[Props]("MainMenu")
    .initialStateP(p =>State(p.uri().headOption.map(_.toString).getOrElse("")))
    .backend(new Backend(_))
    .render((P, S, B) => {
    <.form(^.onSubmit ==> B.handleSubmit)(
      <.input(^.`type`:="text", ^.onChange ==> B.onChange, bss.formControl,
        ^.placeholder:="Enter/Paste URL to object", ^.value:=S.url),
      <.span(bss.alert(CommonStyle.warning),bss.pullRight,(S.errmsg=="")?= ^.visibility.hidden)(S.errmsg), <.br(),
    <.input(^.`type`:="submit", bss.buttonXS)

    )
  }).build

  def apply(uri: Props) = URLBox(uri)
}
