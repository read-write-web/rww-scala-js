package rww.ui.foaf

import japgolly.scalajs.react.vdom.all._
import org.scalajs.dom._
import japgolly.scalajs.react.{ ReactComponentB, _ }

case class PixProps(src: String = "")

object Pix {
  
  def apply(props: PixProps) = Pix(props)
  
  private val Pix = ReactComponentB[PixProps]("Pix")
    .render((P, PC) => div(className := "picture")(
      img(src := P.src)))
      .build
}