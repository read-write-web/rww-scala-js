package rww.ui


import java.net.URI

import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactNode, _}
import rww.Rdf
import rww.store.WebView
import rx._
import rx.ops._
import spatutorial.client.components.Icon._
import spatutorial.client.components._

import scala.collection.immutable.ListSet
import scalacss.ScalaCssReact._

/**
 *
 */
object MainMenu {

  private val menuItems = {
    List(MenuItem(_ => "Dashboard", Icon.dashboard, MainRouter.dashboardLoc))
  }

  private val MainMenu = ReactComponentB[Props]("MainMenu")
    .stateless
    .backend(new Backend(_))
    .render((P, _, B) => {
    <.ul(bss.navbar)(
    // build a list of menu items
    for (item <- menuItems) yield {
      <.li((P.activeLocation.path == item.location.path) ?= (^.className := "active"),
        MainRouter.routerLink(item.location)(item.icon, " ", item.label(P))
      )
    }, {
      var i: Int = 0
      for (u <- P.pages) yield {
        i = i + 1
        val l = MainRouter.pagesLoc(u)
        <.li((P.activeLocation.path == l.path) ?= (^.className := "active"),
          MainRouter.routerLink(l)(Icon.check, " ", "page " + i)
        )

      }
    }
    )
  })
    .componentDidMount(_.backend.mounted())
    .build

  def apply(props: Props) = MainMenu(props)

  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(activeLocation: MainRouter.Loc,
                   pages: ListSet[URI],
                   webview: Rx[WebView[Rdf]])

  case class MenuItem(label: (Props) => ReactNode,
                      icon: Icon,
                      location: MainRouter.Loc)

  class Backend(t: BackendScope[Props, _]) extends OnUnmount {
    def mounted(): Unit = {
      // hook up to Todo changes
      val obsItems = t.props.webview.foreach { wv => t.forceUpdate() }
      onUnmount {
        // stop observing when unmounted (= never in this SPA)
        obsItems.kill()
      }
      //      MainDispatcher.dispatch(RefreshTodos)
    }
  }

}
