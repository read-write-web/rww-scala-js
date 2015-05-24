package rww.ui


import java.net.URI

import japgolly.scalajs.react.ScalazReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import rx.Var
import spatutorial.client.components.Bootstrap.CommonStyle
import spatutorial.client.components.GlobalStyles

import scala.collection.immutable.ListSet
import scala.util.Try
import scalacss.ScalaCssReact._
import scalaz.effect.IO


/**
 * Created by hjs on 13/05/2015.
 */
object URLBox {
  val ST = ReactS.Fix[State]
  @inline private def bss = GlobalStyles.bootstrapStyles


  def onChange(e: ReactEventI) =
    ST.mod(state => State(e.target.value))


  def handleSubmit( props: Props, state: State)(e: ReactEventI) = ST.retM{
    e.preventDefaultIO
  } addCallback IO {
    state.url.map { uri =>
      MainRouter.ws.fetch(rww.Rdf.ops.URI(state.urlStr)) //todo: move elswhere
      props.uri.update(props.uri() + uri) //todo: use the message passing pattern from tutorial?
    }
    ()
  }.flatMap { _ =>
    state.url.map{ uri =>
      MainRouter.router.setIO(MainRouter.pagesLoc(uri))}.getOrElse(IO())
  }

  private val UrlBox = ReactComponentB[Props]("MainMenu")
    .initialStateP(p =>
    State(p.uri().headOption.map(_.toString)
      //todo: make default settable ( not quite as easy as expected, due to typing of router )
      .getOrElse("http://bblfish.net/people/henry/card#me")
    )
    )
    .renderS(($, P, S) =>
    <.form(^.onSubmit ~~> $._runState(handleSubmit(P,S)))(
      <.input(^.`type` := "text", ^.onChange ~~> $._runState(onChange), bss.formControl,
        ^.placeholder := "Enter/Paste URL to object", ^.value := S.urlStr),
      <.span(bss.alert(CommonStyle.warning), bss.pullRight, (S.url.isSuccess) ?= ^.visibility.hidden)(S.url.failed.map(_.getMessage ).toOption), <.br(),
      <.input(^.`type` := "submit", bss.buttonXS)
    )
  ).build

  def apply(uri: Props) = UrlBox(uri)


  case class Props(uri: Var[ListSet[URI]])

  case class State(urlStr: String) {
    val url = Try(new java.net.URI(urlStr))
  }

}
