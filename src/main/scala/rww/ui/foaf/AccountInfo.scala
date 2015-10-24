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
  val AccountInfo = ReactComponentB[WProps[OnlineAccount]]("AccountInfo")
    .initialState(None)
    .renderP(($,P)=> {
    import rww.Rdf.ops._
    import rww._
    val act = P.about
    <.li()(
      act.accountServiceHomepage.map(_.pg.pointer) collect {
        case Literal(lit,_,_)=> new jURI(lit)
        case URI(u) => new jURI(u)
      } map { u: jURI =>
        <.a(^.href := u.toString, ^.target := "_blank", style.titleCase)(u.getAuthority)
      },
      <.dl()(
        act.label.toLitStr.headOption.toSeq.flatMap(t=> Seq(<.dt("label"),<.dd(t))),
        act.accountName.toLitStr.headOption.toSeq.flatMap(n=> Seq(<.dt("name"),<.dd(n)))
      )
    )
  }).build

  def apply(p: WProps[OnlineAccount]) = AccountInfo(p)
}
