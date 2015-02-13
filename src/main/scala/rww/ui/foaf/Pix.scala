package rww.ui.foaf

import japgolly.scalajs.react.vdom.all._
import org.w3.banana.plantain.Plantain
import org.w3.banana.plantain.Plantain.ops._
import org.scalajs.dom._
import japgolly.scalajs.react.{ ReactComponentB, _ }

case class PixProps(src: Option[Plantain#URI])
//""
object Pix {

  def apply(props: PixProps) = Pix(props)
  
  val defaultPic = URI("static/avatar-man.png")
  
  private val Pix = ReactComponentB[PixProps]("Pix")
    .render((P, PC) => div(className := "picture")(
      img(src := P.src.getOrElse(defaultPic).toString)))
    .build
}