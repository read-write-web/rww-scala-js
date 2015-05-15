package rww.ui.foaf

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.extra.LogLifecycle
import japgolly.scalajs.react.vdom.all._
import org.w3.banana.{FOAFPrefix, PointedGraph}
import rww.Rdf
import rww.ontology._
import rww.rdf.Named

import scalacss.ScalaCssReact._


object Profile {

  def apply(props: Props[Named[Rdf,Person]]) = {
    println("rendering Profile for "+props.obj.obj.pg.pointer+" on "+props.context)
    profile(props)
  }

  import rww.Rdf.ops._

  case class RelProps(subj: PointedGraph[Rdf], rel: Rdf#URI, thizs: Seq[PointedGraph[Rdf]])

  case class Props[O](obj: O,
                      context: Rdf#URI ,
                      edit: Boolean = false,
                      editText: String = "Edit")

  import rww.ui.foaf.{FoafStyles => style}

  val profile = ReactComponentB[Props[Named[Rdf,Person]]]("Profile")
    .initialState(None)
    .render((P, S, B) => {
    val person = P.obj
    // import shapeless.singleton.syntax._ <- use this when using styleC
    if (P.edit) p("in edit mode")
    else div(style.clearfix,style.center,style.body)(
      div(style.pic)(
        img(src := P.obj.obj.depiction.headOption.getOrElse("avatar-man.png"))
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
    val uri = P.npg.pointer match {
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

  val PersonBasicInfo = ReactComponentB[Props[Named[Rdf,Person]]]("PersonBasicInfo")
  .initialState(None)
  .render((P,S,B)=>{
    val person = P.obj.obj
    val foaf = FOAFPrefix[Rdf]

    div(style.basic)(
      (person.pg/foaf.name).headOption.map{pg: PointedGraph[Rdf]=>{
        Name(RelProp(Named(P.obj.name,pg),Rel(Triple(person.pg.pointer,foaf.name,pg.pointer)),true))
      }},
//      p.name.headOption.map(name => NAME(RelProp(P.obj,foaf.name,name)) getOrElse div(),
    {
        val n = person.givenName.headOption.getOrElse("(unknown)")
        div(style.surname, title := n)(n)
      },
      person.workPlaceHomePage.headOption.map { hp =>
        div(style.company, title := hp.toString)(hp.toString)
      } getOrElse
        div()
    )
  }).build

  def keys(pg: PointedGraph[Rdf]) = pg.pointer.toString

  val PersonMoreInfo =  ReactComponentB[Props[Named[Rdf,Person]]]("PersonMoreInfo")
    .initialState(None)
    .render((P,S,B)=> {
     val p = P.obj.obj
    div(style.details)(
      div(className:="title",style.centerText,style.titleCase)("Details"),
      ul(style.clearfix,style.span3)(
        for (tel <- p.phone) yield PhoneInfo.withKey(tel.pg.pointer.toString)(Props(tel,P.context)),
        for (mbox <- p.mbox) yield MailInfo(Props(mbox,P.context)),
        for (hm <- p.home) yield ContactLocationInfo(Props(("home",hm),P.context)),
        for (o <- p.office) yield ContactLocationInfo(Props(("office",o),P.context)),
        for (e <- p.emergency) yield ContactLocationInfo(Props(("emergency",e),P.context)),
        for (hm <- p.mobile) yield ContactLocationInfo(Props(("mobile",hm),P.context)),
        for (acc <- p.account) yield AccountInfo(Props(acc,P.context))
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
           span()(AddressInfo(Props(addr,P.context)))
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
