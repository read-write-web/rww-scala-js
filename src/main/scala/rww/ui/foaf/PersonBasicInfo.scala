package rww.ui.foaf

import java.net.{URI => jURI}

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import rww.ontology.Person
import rww.ui.foaf.{FoafStyles => style}

import scala.util.{Success, Try}
import scalacss.ScalaCssReact._

/**
 * Created by hjs on 17/05/2015.
 */
object PersonBasicInfo {
    import rww._

    val PersonBasicInfo = ReactComponentB[PProps[Person]]("PersonBasicInfo")
      .initialState(None)
      .render((P,S,B)=>{
        val person = P.obj
        <.div(style.basic)(
          person.name.headOption.map(NameInfo(_)).toList,
//          p.name.headOption.map(name => NAME(RelProp(P.obj,foaf.name,name)) getOrElse div(),
          {
            val n = person.givenName.toLitStr.headOption.getOrElse("(anonymous)")
            <.div(style.surname, ^.title := n)(n)
          },
          person.workPlaceHomePage.toUriStr.map( host => Try( new jURI(host)  )) collectFirst {
            case Success(u) => <.div(style.company, ^.title := u.getHost)(u.getHost)
          } getOrElse <.div()
      )
    }).build

    def apply(props: PProps[Person]) = {
      println("in PersonBasicInfo constructor")
      PersonBasicInfo(props)
    }
}
