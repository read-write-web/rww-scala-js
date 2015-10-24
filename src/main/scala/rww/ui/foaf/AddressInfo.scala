package rww.ui.foaf

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import rww.ontology.Address
import rww.ui.foaf.{FoafStyles => style}

import scalacss.ScalaCssReact._

/**
 * Created by hjs on 17/05/2015.
 */
object AddressInfo {
  import rww._
  val AddressInfo = ReactComponentB[WProps[Address]]("AddressInfo")
    .initialState(None)
    .renderP(($,P)=> {
    val a = P.about
    <.div(style.content){
      val c = a.street.toLitStr.toList:::
        List(a.city.toLitStr.headOption.map(_+" ").getOrElse("")+
          a.postalCode.toLitStr.headOption.getOrElse(""),
          a.country.toLitStr.headOption.getOrElse(""))
      c.flatMap(x=>List(<.span()(x),<.br()))
    }
  }).build

  def apply(add: WProps[Address]) = AddressInfo(add)

}
