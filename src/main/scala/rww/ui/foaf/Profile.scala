package rww.ui.foaf

import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}

import scalacss.ScalaCssReact._


object Profile {


  case class PersonProps(person: Person,
                         edit: Boolean = false,
                         editText: String = "Edit")

  class Backend(t: BackendScope[Unit, PersonProps]) {

  }

  import rww.ui.foaf.{FoafStyles => style}

  val profile = ReactComponentB[PersonProps]("Profile")
    .initialState(None)
    .render((P, S, B) => {
    // import shapeless.singleton.syntax._ <- use this when using styleC
    if (P.edit) p("in edit mode")
    else div(style.clearfix,style.center,style.body)(
      div(style.pic)(
        img( src := P.person.depiction.headOption.getOrElse("avatar-man.png"))
      ),
    personBasicInfo(P),
    personMoreInfo(P)
    )
  }).build

  val personBasicInfo = ReactComponentB[PersonProps]("BasicInfo")
  .initialState(None)
  .render((P,S,B)=>{
    val p = P.person
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

  val personMoreInfo =  ReactComponentB[PersonProps]("MoreInfo")
    .initialState(None)
    .render((P,S,B)=> {
     val p = P.person
    div(style.details)(
      div(className:="title title-case",style.centerText,style.titleCase)("Details"),
      ul(style.clearfix,style.span3)(
        li(style.floatLeft)(
           div(className:="email")(
             div(className:="title-case")("Email"),
             div(className:="content email-content")(p.mbox.headOption.getOrElse[String](""))
           ),
           div(className:="phone")(
             div(className:="title-case")("Phone"),
             div(className:="content email-content")(p.phone.headOption.getOrElse[String](""))
           )
         )
        )

    )
  }).build

}
