package rww.ui


import java.net.{URI => jURI}

import japgolly.scalajs.react.ScalazReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import spatutorial.client.components.Bootstrap.CommonStyle
import spatutorial.client.components.GlobalStyles

import scala.util.Try
import scalacss.ScalaCssReact._



/**
 * Created by hjs on 13/05/2015.
 */
object URLBoxComponent {
  val ST = ReactS.Fix[State]
  @inline private def bss = GlobalStyles.bootstrapStyles


  def onChange(e: ReactEventI) =
    ST.mod(state => State(e.target.value))


  def handleSubmit( props: Props, state: State)(e: ReactEventI) = ST.retM (
    e.preventDefaultIO
  )

  private val UrlBox = ReactComponentB[Props]("MainMenu")
    .initialStateP(p => State(p.uri.map(_.toString).getOrElse("")))
    .renderS(($, P, S) =>
    <.form(^.onSubmit ~~> $._runState(handleSubmit(P,S)))(
      <.input(^.`type` := "text", ^.onChange ~~> $._runState(onChange), bss.formControl,
        ^.placeholder := "Enter/Paste URL to object", ^.value := S.urlStr),
      <.span(bss.alert(CommonStyle.warning), bss.pullRight, (S.url.isSuccess) ?= ^.visibility.hidden)(S.url.failed.map(_.getMessage ).toOption), <.br(),
      <.input(^.`type` := "submit", bss.buttonXS)
    )
  ).build

  def apply(uri: Props) = UrlBox(uri)


  case class Props(uri: Option[jURI], onSubmit: jURI => Unit  )

  case class State(urlStr: String) {
    val url = Try(new jURI(urlStr))
  }

}
