package rww.ui.foaf

import japgolly.scalajs.react.ScalazReact.{ReactS, _}
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, ReactEventI, _}
import org.w3.banana.syntax.NodeW
import rww.Rdf.ops._
import rww._
import rww.ontology.Person
import rww.store._
import rww.ui.Component
import rww.ui.foaf.{FoafStyles => style}
import rww.ui.rdf.NPGPath
import rww.ui.util.RxStateObserver

import scala.collection.immutable.ListSet
import scalacss.Attrs.color
import scalacss.ScalaCssReact._
import scalaz.\/

/**
 * Created by hjs on 25/05/2015.
 */
object FOAFInfo {

  val FOAF = ReactComponentB[WProps[Person]]("FOAF Info")
    .initialState(None)
    .renderP(($,P) => {
    <.div(style.details)(
      <.div(^.className:="title",style.centerText,style.titleCase)("Friends"),
      <.ul(style.clearfix,style.span3,style.contacts)(
        P.about.knows map { acquaintance => MiniPersonInfo(P.copy(about=acquaintance))}
      )
    )
  }).build

  def apply(p: WProps[Person]) = FOAF(p)
}


object MiniPersonInfo {
  type JumpTyp = \/[RequestState,NPGPath]

  class Backend(t: BackendScope[WProps[Person], Option[JumpTyp]])
    extends RxStateObserver[\/[RequestState,NPGPath]](t)  {
    def mounting(p: WProps[Person]): Unit = observeAndSetState(p.about.npg.jump(p.webAgent))
  }


  val Mini = ReactComponentB[WProps[Person]]("Person Mini Box Info")
    .initialState[Option[JumpTyp]](None)
    .backend(new Backend(_))
    .renderPS(($, P, S) => {
    <.li(style.contact,^.position.relative)(
      <.div(style.titleCase)(
      P.about.name.toPointer.collectFirst {
        case Literal(name, _, _) => name
      }),
      <.div(style.titleCase)(
        P.about.familyName.toPointer.collectFirst {
          case Literal(name, _, _) => name
        }),
      MiniPix(Person(S.flatMap(_.toOption).getOrElse(P.about.npg))),
      WebIDBar(P.copy(about=(P.about.npg.pg.pointer,S.flatMap(_.swap.toOption))))
    )
  })
    .componentWillMount(f => Callback(f.backend.mounting(f.props)))
    .componentWillReceiveProps(f => Callback {
      f.$.backend.unmount
      f.$.backend.mounting(f.nextProps)
    })
    .configure(OnUnmount.install)
    .build

  def apply(p: WProps[Person]) = Mini(p)

}

object WebIDBar {

  //todo: rewrite all this with external style sheets
  val Bar = ReactComponentB[WProps[(Rdf#Node,Option[RequestState])]]("Person WebID bar")
    .stateless
    .renderP(($,P) => {
    new NodeW[Rdf]( P.about._1).fold (
        u => {
          val bgcolorOpt = P.about._2 map {
            case Downloading(_, p) => p.map(p =>
              if (p < 0.3) color.lightyellow
              else if (p < 0.6) color.yellowgreen
              else color.lightgreen
            ).getOrElse(color.yellow)
            case h: HttpError => color.lightpink
            case o: Ok => color.white
            case r: Redirected => color.antiquewhite
            case u: UnRequested => color.white
            //         <.div(style.webIdBar(S))(webid(P))

          }
          val bgcolor = bgcolorOpt.getOrElse(color.beige)

          //todo: requires that the Component know how to show errors
            <.div(^.overflow.hidden, ^.position.absolute,
              ^.bottom := (10 px),
              ^.backgroundColor := bgcolor.value
            )(  P.ctl.link(Component(u))(u.toString) )
      },
      bn => <.div(^.overflow.hidden,
        ^.position.absolute,^.bottom := (10 px),
        ^.backgroundColor := color.blue.value)(
          "add webid"
        ),
      lit => <.div(^.overflow.hidden,^.position.absolute,
        ^.bottom:=(10 px),
        ^.backgroundColor := color.purple.value)(
          "??"+lit.lexicalForm
        )
    )
  }).build

  def apply(p: WProps[(Rdf#Node,Option[RequestState])]) = Bar(p)

}

object MiniPix {

  val ST = ReactS.Fix[Int]

  val Pix = ReactComponentB[ListSet[NPGPath]]("Person Mini Pixture")
    .initialState(0)
    .renderP(($,P) => {
    val pixs = P.collect{
      case value if value.pg.pointer.isURI => value.pg.pointer.asInstanceOf[Rdf#URI] //is there a nicer way to do this?
    }.toSeq match {
      case Seq() => Seq(URI("img/avatar-man.png"))
      case other => other
    }
    def increment(e: ReactEventI) = ST.mod(_+1)
    val i = if (P.size==0) 0 else $.state % P.size
    if (pixs.size > 1) println("more than one picture for "+pixs(0))
    <.div(style.contactPixOuterBox)(
      pixs.slice(i,i+1).zipWithIndex.map { case (uri,ii) =>
        <.img(^.src := uri.getString,
          style.contactPix(ii),
          ^.onClick ==> $._runState(increment))
      }
    )
  }).build


  def apply(p: Person) = {
    val pix = ListSet((p.depiction ++ p.logo).toSeq:_*)
    Pix(pix)
  }

}

object RxJumpComponent {
  // the component must
  // - refresh when one of the RXVarialbes changes
  //  ? when a variable arrives, what should it do with the info?
  //  ? fuse the graph from the Rx component when it arrives with the graph of the original component
  //  ? perhaps be able to switch between views of the component for the different graphs.
  // - minimal component

//  def apply(props: WProps[], children: ReactNode*) = component(props, children)
}