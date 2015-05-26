package rww.ui.foaf

import japgolly.scalajs.react.ScalazReact.ReactS
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, ReactEventI}
import rww.Rdf.ops._
import rww._
import rww.ontology.Person
import rww.ui.MainRouter
import rww.ui.foaf.{FoafStyles => style}
import rww.ui.rdf.NPGPath

import scala.collection.immutable.ListSet
import scalacss.ScalaCssReact._

/**
 * Created by hjs on 25/05/2015.
 */
object FOAFInfo {

  val FOAF = ReactComponentB[WProps[Person]]("FOAF Info")
    .initialState(None)
    .render((P, S, B) => {
    <.div(style.details)(
      <.div(^.className:="title",style.centerText,style.titleCase)("Friends"),
      <.ul(style.clearfix,style.span3,style.contacts)(
        P.about.knows map { x=> MiniPersonInfo(P.copy(about=x))}
      )
    )
  }).build

  def apply(p: WProps[Person]) = FOAF(p)
}


object MiniPersonInfo {

  val Mini =   ReactComponentB[WProps[Person]]("Person Mini Box Info")
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
      MiniPix(P)
    )
  }).build

  def apply(p: WProps[Person]) = Mini(p)

}

object MiniPix {

  val ST = ReactS.Fix[Int]

  def onClick(e: ReactEventI) = ST.mod(_+1)



  val Pix = ReactComponentB[WProps[ListSet[NPGPath]]]("Person Mini Box Info")
    .initialState[Int](0)
    .render((P, _, S) => {
    val pixs = P.about.collect{
      case value if value.pg.pointer.isURI => value.pg.pointer.asInstanceOf[Rdf#URI] //is there a nicer way to do this?
    }.toSeq.lift
    val i : Int = if (P.about.size==0) 0 else { S % P.about.size }
    println(s"Pix($i)")
    <.div(style.contactPix)(
      <.img(^.src := pixs(i).map(_.getString).getOrElse("avatar-man.png"))
    )
  }).build


  def apply(wProps: WProps[Person]) = {
    val localpics = ListSet(wProps.about.depiction.toSeq:_*)
    val remotePics = wProps.about.npg.jump(wProps.webView) match {
      case None => {
        MainRouter.ws.fetch(wProps.about.npg.pg.pointer.asInstanceOf[Rdf#URI])
        None
      }
      case Some(npg) =>
        if (npg eq wProps.about.npg) None
        else {
          Some(Person(npg).depiction)
        }
    }
    val allPics = localpics ++ remotePics.getOrElse(Iterable())
    Pix(wProps.copy(about=allPics))
  }

}