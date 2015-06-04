package rww.ui


import java.net.{URI => jURI}

import japgolly.scalajs.react.extra.router2._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactNode, _}
import rx._
import spatutorial.client.components.Icon.Icon
import spatutorial.client.components.{GlobalStyles, Icon}

import scala.collection.immutable.ListSet
import scalacss.ScalaCssReact._

/**
 *
 */
object MainMenu {




  private val menuItems = {
    List(MenuItem(_ => "Dashboard", Icon.dashboard, URLEntry))
  }

  private val MainMenu = ReactComponentB[Props]("MainMenu")
    .initialStateP(p=>p.pages)
    .render((P, S) => {
    <.ul(bss.navbar)(
    // build a list of menu items
    for (item <- menuItems) yield {
      <.li((P.currentLoc == item.location) ?= (^.className := "active"),
        P.ctl.link(item.location)(item.icon, " ", item.label(P))
      )
    }, {
      var i: Int = 0
      for (u: jURI <- S().toList.reverse) yield { //todo: reverse list to get page numbering ugly
        i = i + 1
        <.li((P.currentLoc == Component(u)) ?= (^.className := "active"),
          P.ctl.link(Component(u))(Icon.check, " ", "page " + i)
        )

      }
    }
    )
  })
    .build

  def apply(props: Props) = MainMenu(props)

  @inline private def bss = GlobalStyles.bootstrapStyles

//  case class Props(activeLocation: MainRouter.Loc,
//                   pages: ListSet[jURI],
//                   webAgent: WebAgent )

  case class Props(ctl: RouterCtl[MyPages], currentLoc:MyPages, pages: Var[ListSet[jURI]])


  case class MenuItem(label: (Props) => ReactNode,
                      icon: Icon,
                      location: MyPages)



}
