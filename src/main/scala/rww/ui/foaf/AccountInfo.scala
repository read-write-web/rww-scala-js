package rww.ui.foaf

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import rww.ontology.OnlineAccount
import rww.ui.foaf.{FoafStyles=>style}
import java.net.{URI=>jURI}

import scalacss.ScalaCssReact._

/**
 * Created by hjs on 17/05/2015.
 */
object AccountInfo {
  val AccountInfo = ReactComponentB[OnlineAccount]("AccountInfo")
    .initialState(None)
    .render((P,S,B)=> {
    import rww.Rdf.ops._
    import rww._
    val act = P
    <.li()(
      act.accountServiceHomepage.map(_.pg.pointer) collect {
        case Literal(lit,_,_)=> new jURI(lit)
        case URI(u) => new jURI(u)
      } map { u: jURI =>
        <.a(^.href := u.toString, ^.target := "_blank", style.titleCase)(u.getAuthority)
      },
      <.dl()(
        act.label.toLitStr.headOption.toList.flatMap(t=> List(<.dt("label")(<.dd(t)))),
        act.accountName.toLitStr.headOption.toList.flatMap(n=> List(<.dt("name"),<.dd(n)))
      )
    )
  }).build

  def apply(account: OnlineAccount) = AccountInfo(account)
}
