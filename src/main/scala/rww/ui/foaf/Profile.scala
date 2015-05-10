package rww.ui.foaf

import japgolly.scalajs.react.{ReactEvent, ReactEventI, BackendScope, ReactComponentB}
import japgolly.scalajs.react.extra.LogLifecycle
import japgolly.scalajs.react.vdom.all._
import org.scalajs
import org.scalajs.dom
import org.w3.banana.{FOAFPrefix, PointedGraph}
import rww.ontology._
import rww.ui.foaf.TestApp.Rdf

import scala.scalajs
import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scalacss.ScalaCssReact._
import scalaz.Alpha.{P, S}
import scalaz.State


object Profile {

  import rww.rdf.ops._
  case class RelProp(parent: PointedGraph[Rdf],
                     rel: Rdf#URI,
                     thiz: PointedGraph[Rdf],
                     edit: Boolean = false)

  case class RelProps(subj: PointedGraph[Rdf], rel: Rdf#URI, thizs: Seq[PointedGraph[Rdf]])

  case class Props[O](obj: O,
                      edit: Boolean = false,
                      editText: String = "Edit")

//  class Backend(t: BackendScope[Unit, PersonProps]) {
//
//  }

  import rww.ui.foaf.{FoafStyles => style}

  val profile = ReactComponentB[Props[Person]]("Profile")
    .initialState(None)
    .render((P, S, B) => {
    val person = P.obj
    // import shapeless.singleton.syntax._ <- use this when using styleC
    if (P.edit) p("in edit mode")
    else div(style.clearfix,style.center,style.body)(
      div(style.pic)(
        img(src := P.obj.depiction.headOption.getOrElse("avatar-man.png"))
      ),
      PersonBasicInfo(P),
      PersonMoreInfo(P)
    )
  })
    .configure(LogLifecycle.short)
    .build

  val IMG = ReactComponentB[RelProp]("img")
    .initialState(None)
    .render((P,S,B)=> {
    val uri = P.thiz.pointer match {
      case URI(u) =>  u
      case _ => "avatar-man.png"
    }
    img(src:=uri)
  })

//  val IMGs = ReactComponentB[RelProp]("img")
//    .initialState(None)
//    .render((P,S,B)=> {
//    img(src := P.subj)
//  })

  val PersonBasicInfo = ReactComponentB[Props[Person]]("PersonBasicInfo")
  .initialState(None)
  .render((P,S,B)=>{
    val p = P.obj
    val foaf = FOAFPrefix[Rdf]

    div(style.basic)(
      (P.obj.pg/foaf.name).headOption.map(pg=>{
        println("name="+pg.pointer.toString)
        NAME(RelProp(P.obj.pg,foaf.name,pg,true))
      }).toSeq,
//      p.name.headOption.map(name => NAME(RelProp(P.obj,foaf.name,name)) getOrElse div(),
    {
        val n = p.givenName.headOption.getOrElse("(unknown)")
        div(style.surname, title := n)(n)
      },
      p.workPlaceHomePage.headOption.map { hp =>
        div(style.company, title := hp.toString)(hp.toString)
      } getOrElse
        div()
    )
  }).build

  case class NameState(edit: Option[String]=None)
  class NameBackend($: BackendScope[RelProp,NameState]) {
    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      $.state.edit.map { newName =>
        val oldTriple = Triple($.props.parent.pointer, $.props.rel, $.props.thiz.pointer)
        val newTriple = Triple($.props.parent.pointer, $.props.rel, Literal(newName))
        println("remove:"+oldTriple)
        println("add:"+newTriple)
      }
    }
    def enterEdit(e: ReactEvent) =
      $.modState(_=>NameState(Some("")))

    def onChange(e: ReactEventI) =
      $.modState(s => NameState(Some(e.target.value)))

  }

  val NAME = ReactComponentB[RelProp]("PersonName")
    .initialState(NameState())
    .backend(new NameBackend(_))
    .render((P,S,B)=> {
    val nameOpt = P.thiz.pointer match {
      case Literal(name, _, _) => Some(name)
      case _ => None
    }
    if (S.edit.isEmpty && nameOpt.isDefined)
      div(style.name, title := nameOpt.get, onClick ==> B.enterEdit)(nameOpt.get)
    else {
      form(onSubmit ==> B.handleSubmit)(
        input(style.name, tpe := "text", placeholder := "Enter name",
          value := S.edit.get,
          onChange ==> B.onChange
        )
      )
    }
  }).build

  def keys(pg: PointedGraph[Rdf]) = pg.pointer.toString

  val PersonMoreInfo =  ReactComponentB[Props[Person]]("PersonMoreInfo")
    .initialState(None)
    .render((P,S,B)=> {
     val p = P.obj
    div(style.details)(
      div(className:="title",style.centerText,style.titleCase)("Details"),
      ul(style.clearfix,style.span3)(
        for (tel <- p.phone) yield PhoneInfo.withKey(tel.pg.pointer.toString)(Props(tel)),
        for (mbox <- p.mbox) yield MailInfo(Props(mbox)),
        for (hm <- p.home) yield ContactLocationInfo(Props(("home",hm))),
        for (o <- p.office) yield ContactLocationInfo(Props(("office",o))),
        for (e <- p.emergency) yield ContactLocationInfo(Props(("emergency",e))),
        for (hm <- p.mobile) yield ContactLocationInfo(Props(("mobile",hm))),
        for (acc <- p.account) yield AccountInfo(Props(acc))
    )
    )
  }).build

  val MailInfo = ReactComponentB[Props[Mbox]]("PersonMoreInfo")
    .initialState(None)
    .render((P,S,B)=> {
    val m = P.obj
    li(style.floatLeft)(
      div(style.titleCase)("Email"),
      div(style.content)(
        (for (
          mstr <- m.asString;
          mbx <- m.asURIStr
        ) yield  a(href := mbx)(mstr)).getOrElse("TODO")
      )
    )
  }).build

  val PhoneInfo = ReactComponentB[Props[Tel]]("PersonMoreInfo")
    .initialState(None)
    .render((P,S,B)=> {
    val m = P.obj
    li(style.floatLeft)(
      div(style.titleCase)("Phone"),
      div(style.content)(
        (for (
          mstr <- m.asString;
          mbx <- m.asURIStr
        ) yield  a(href := mbx)(mstr)).getOrElse("TODO")
      )
    )
  }).build

  val ContactLocationInfo = ReactComponentB[Props[(String,ContactLocation)]]("ContactLocationInfo")
    .initialState(None)
    .render((P,S,B)=> {
      val (homeTp,cl) = P.obj
      li()(
        span(style.clearfix,style.span3)(
          div(style.titleCase)(homeTp) ,
          for (addr<-cl.address) yield
           span()(AddressInfo(Props(addr)))
        )
      )
  }).build

  val AddressInfo = ReactComponentB[Props[Address]]("AddressInfo")
    .initialState(None)
    .render((P,S,B)=> {
       val a = P.obj
       div(style.content){
         val c = a.street:::
          List(a.city.headOption.map(_+" ").getOrElse("")+a.postalCode.headOption.getOrElse(""),
          a.country.headOption.getOrElse(""))
         c.flatMap(span()(_)::br()::Nil)
       }
  }).build

  val AccountInfo = ReactComponentB[Props[OnlineAccount]]("AccountInfo")
    .initialState(None)
    .render((P,S,B)=> {
      val act = P.obj
      li()(
        act.accountServiceHomepage.map(u=>a(href := u.toString,style.titleCase)(u.getAuthority)).toSeq,
        dl()(
          act.label.headOption.map(t=>span(dt("label"),dd(t))),
          act.accountName.headOption.map(n=>span(dt("name"),dd(n)))
        )
      )
    }).build


}
