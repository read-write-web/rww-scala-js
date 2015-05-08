package rww.ui.foaf

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.all._
import rww.ontology._

import scalacss.ScalaCssReact._


object Profile {


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
  }).build

  val PersonBasicInfo = ReactComponentB[Props[Person]]("PersonBasicInfo")
  .initialState(None)
  .render((P,S,B)=>{
    val p = P.obj
    div(style.basic)(
      p.name.headOption.map(name => div(style.name, title := name)(name)) getOrElse div(), {
        val n = p.givenName.headOption.getOrElse("(unknown)")
        div(style.surname, title := n)(n)
      },
      p.workPlaceHomePage.headOption.map { hp =>
        div(style.company, title := hp.toString)(hp.toString)
      } getOrElse
        div()
    )
  }).build

  val PersonMoreInfo =  ReactComponentB[Props[Person]]("PersonMoreInfo")
    .initialState(None)
    .render((P,S,B)=> {
     val p = P.obj
    div(style.details)(
      div(className:="title",style.centerText,style.titleCase)("Details"),
      ul(style.clearfix,style.span3)(
        for (tel <- p.phone) yield PhoneInfo(Props(tel)),
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
