package rww.ui.foaf

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import rww.ontology.Person
import rww.ui.foaf.{FoafStyles => style}

import scalacss.ScalaCssReact._

/**
 * Created by hjs on 25/05/2015.
 */
object FOAFInfo {

  val FOAF = ReactComponentB[PProps[Person]]("FOAF Info")
    .initialState(None)
    .render((P, S, B) => {
    <.div(style.details)(
      <.div(^.className:="title",style.centerText,style.titleCase)("Friends"),
      <.ul(style.clearfix,style.span3,style.contacts)(
        P.about.knows map { MiniPersonInfo(_)}
      )
    )
  }).build

  def apply(person: Person) = FOAF(PProps(person))
}


object MiniPersonInfo {
  import rww.Rdf.ops._
  import rww._

  val Mini =   ReactComponentB[PProps[Person]]("Person Mini Box Info")
    .initialState(None)
    .render((P, S, B) => {
    <.li(style.contact)(
      <.div(style.titleCase)(
      P.about.name.toPointer.collectFirst {
        case Literal(name, _, _) => name
      }),
      <.div(style.titleCase)(
        P.about.familyName.toPointer.collectFirst {
          case Literal(name, _, _) => name
        }),
      <.div(style.contactPix)(
        <.img(^.src := P.about.depiction.toPointer.collectFirst {
          case URI(u) => u
        }.getOrElse("avatar-man.png"))
      )
    )
  }).build

  def apply(person: Person) = Mini(PProps(person))

}