package rww.ui

import java.net.{URI=>jURI}

import japgolly.scalajs.react.ScalazReact._
import japgolly.scalajs.react.extra.Listenable
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import org.scalajs
import rww.Rdf
import rww.store.WebAgent
import rww.ui.util.RxObserver
import rx._

import scala.collection.immutable.ListSet


/**
 * Created by hjs on 13/05/2015.
 */
object MainRouter extends RoutingRules {

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

  // redirect all invalid routes to dashboard
  override protected val notFound = redirect(dashboardLoc, Redirect.Replace)
  //todo: Web Agent should be passed in constructor. ( but is tricky with router )
  val ws = new WebAgent[Rdf](None)
  val pages = Var(ListSet[jURI]())

  // build a baseUrl, this method works for both local and server addresses (assuming you use #)
  val baseUrl = BaseUrl(scalajs.dom.window.location.href.takeWhile(_ != '#'))
  // register the modules and store locations
  val dashboardLoc = register(rootLocation(Dashboard.component))


  // initialize router and its React component
  val router = routingEngine(baseUrl)
  val routerComponent = componentUnbuilt(router).buildU

  //
  //dynamic route
  //
  private val namePathMatch = "^#url/(.+)$".r
  val pagesLoc = dynLink[jURI](id => s"#url/${id.toString}")
  register(
    parser { case namePathMatch(url) => url }.thenMatch { urlstr =>
      import rww.Rdf.ops._
      val url = URI(urlstr)
      render(PNGWindow(url,ws.cache))
  })

  // functions to provide links (<a href...>) to routes
  def dashboardLink = router.link(dashboardLoc)

  def routerLink(loc: ApprovedPath[P]) = router.link(loc)

  /**
   * adapt the Router method for our case, where we need to add a rx.Var listener
   * @param router
   * @return
   */
  def componentUnbuilt(router: Router) =
    ReactComponentB[Unit]("Router")
      .initialState(router.syncToWindowUrl.unsafePerformIO())
      .backend(new Backend(_))
      .render((_, route, _) => route.render(router))
      .componentWillMount(_ => router.init.unsafePerformIO())
      .componentDidMount { scope => scope.backend.mounted() }
      .configure(Listenable.installSF(_ => router, (_: Unit) => router.syncToWindowUrlS))

  /**
   * Creates the basic page structure under the body tag.
   *
   * @param ic
   * @return
   */
  override protected def interceptRender(ic: InterceptionR) = {
    <.div(
      // here we use plain Bootstrap class names as these are specific to the top level layout defined here
      <.nav(^.className := "navbar navbar-inverse navbar-fixed-top")(
        <.div(^.className := "container")(
          <.div(^.className := "navbar-header")(
            <.span(^.className := "navbar-brand")("RWW demo")),
          <.div(^.className := "collapse navbar-collapse")(
            MainMenu(MainMenu.Props(ic.loc, pages(), ws.cache))
          )
        )
      ),
      // currently active module is shown in this container
      <.div(^.className := "container")(ic.element)
    )
  }


  class Backend(t: BackendScope[Unit, router.Loc]) extends RxObserver(t) {
    def mounted(): Unit = observe(pages)
  }


}