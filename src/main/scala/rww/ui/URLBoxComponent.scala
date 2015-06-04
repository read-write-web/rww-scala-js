package rww.ui


import java.net.{URI => jURI}

import japgolly.scalajs.react.ScalazReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import spatutorial.client.components.Bootstrap.CommonStyle
import spatutorial.client.components.GlobalStyles

import scala.util.Try
import scalacss.ScalaCssReact._
import scalaz.effect.IO


/**
 * Created by hjs on 13/05/2015.
 */
object URLBoxComponent {
  val ST = ReactS.Fix[State]
  @inline private def bss = GlobalStyles.bootstrapStyles


  def onChange(e: ReactEventI) =
    ST.mod(state => State(e.target.value))


  def handleSubmit( props: Props, state: State)(e: ReactEventI) =
    e.preventDefaultIO.flatMap(_=>state.url.map(u=> props.onSubmit(u)).getOrElse(IO()))

  private val UrlBox = ReactComponentB[Props]("MainMenu")
    .initialStateP(p => State(p.initialValue))
    .renderS(($, P, S) =>
    <.form(^.onSubmit ~~> handleSubmit(P,S)_)(
      <.input(^.`type` := "text", ^.onChange ~~> $._runState(onChange), bss.formControl,
        ^.placeholder := "Enter/Paste URL to object", ^.value := S.urlStr),
      <.span(bss.alert(CommonStyle.warning), bss.pullRight,
        (S.url.isSuccess) ?= ^.visibility.hidden)(S.url.failed.map(_.getMessage ).toOption), <.br(),
      <.input(^.`type` := "submit", bss.buttonXS)
    )
  ).build

  def apply(uri: Props) = UrlBox(uri)

  case class Props(initialValue: String, onSubmit: jURI => IO[Unit]  )

  case class State(urlStr: String) {
    val url = Try(new jURI(urlStr))
  }

}

