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
import scalaz.effect.IO
import japgolly.scalajs.react._, ScalazReact._

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

  val Pix = ReactComponentB[WProps[ListSet[NPGPath]]]("Person Mini Box Info")
    .initialState[Int](0)
    .renderS(($,P,S) => {
    val pixs = P.about.collect{
      case value if value.pg.pointer.isURI => value.pg.pointer.asInstanceOf[Rdf#URI] //is there a nicer way to do this?
    }.toSeq match {
      case Seq() => Seq(URI("avatar-man.png"))
      case other => other
    }
    def increment(e: ReactEventI) = ST.mod(_+1)
    val i = if (P.about.size==0) 0 else S % P.about.size
    if (pixs.size > 1) println("more than one picture for "+pixs(0))
    <.div(style.contactPixOuterBox)(
      pixs.slice(i,i+1).zipWithIndex.map { case (uri,ii) =>
        <.img(^.src := uri.getString,
          style.contactPix(ii),
          ^.onClick ~~> $._runState(increment))
      }
    )
  }).build


  def apply(wProps: WProps[Person]) = {
    val p = wProps.about
    val localpics = ListSet((p.depiction ++ p.logo).toSeq:_*)
    val remotePics = p.npg.jump(wProps.webView) match {
      case None => {
        MainRouter.ws.fetch(p.npg.pg.pointer.asInstanceOf[Rdf#URI])
        None
      }
      case Some(npg) =>
        if (npg eq p.npg) None
        else {
          Some(Person(npg).depiction)
        }
    }
    val allPics = localpics ++ remotePics.getOrElse(Iterable())
    Pix(wProps.copy(about=allPics))
  }

}