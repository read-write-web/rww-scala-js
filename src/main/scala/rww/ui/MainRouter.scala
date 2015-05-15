package rww.ui

import java.net.URI

import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import japgolly.scalajs.react.extra.{Listenable, OnUnmount}
import japgolly.scalajs.react.extra.router.RouteCmd.{PushState, BroadcastLocChange}
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs
import rww.Rdf
import rww.ontology.Person
import rww.store.WebAgent
import rww.ui.foaf.Profile
import rx._
import rx.ops._


/**
 * Created by hjs on 13/05/2015.
 */
object MainRouter extends RoutingRules {
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

  val ws = new WebAgent[Rdf](None)
  val pages = Var(List[URI]())

  // build a baseUrl, this method works for both local and server addresses (assuming you use #)
  val baseUrl = BaseUrl(scalajs.dom.window.location.href.takeWhile(_ != '#'))

  // register the modules and store locations
  val dashboardLoc = register(rootLocation(Dashboard.component))

  //dynamic route
  private val namePathMatch = "^#url/(.+)$".r

  register(parser { case namePathMatch(url) => url }.thenMatch{url =>
     ws.cache().get(rww.Rdf.ops.URI(url)) match {
      case None => redirect(dashboardLoc, Redirect.Push)
      case Some(npg) => render(Profile(Profile.Props(npg.map(Person(_)),npg.name)))  //todo: a mess
    }
  })

  val pagesLoc = dynLink[URI](id => s"#url/${id.toString}") //todo: does one have to urlencode this?



  // functions to provide links (<a href...>) to routes
  def dashboardLink = router.link(dashboardLoc)
//  def todoLink = router.link(todoLoc)
  def routerLink(loc: ApprovedPath[P]) = router.link(loc)

  // initialize router and its React component
  val router = routingEngine(baseUrl)
  val routerComponent = componentUnbuilt(router).buildU

    /**
     * adapt the Router method for our case
     * @param router
     * @return
     */
  import japgolly.scalajs.react.ScalazReact._

  def componentUnbuilt(router: Router) =
      ReactComponentB[Unit]("Router")
        .initialState(router.syncToWindowUrl.unsafePerformIO())
        .backend(new Backend(_))
        .render((_, route, _) => route.render(router))
        .componentWillMount(_ => router.init.unsafePerformIO())
        .componentDidMount{scope=> scope.backend.mounted()}
        .configure(Listenable.installSF(_ => router, (_: Unit) => router.syncToWindowUrlS))

  abstract class RxObserver[BS <: BackendScope[_, _]](scope: BS) extends OnUnmount {
      def observe[T](rx: Rx[T]): Unit = {
        val obs = rx.foreach(_ => scope.forceUpdate())
        // stop observing when unmounted
        onUnmount(obs.kill())
      }
    }

  class Backend(t: BackendScope[Unit, router.Loc]) extends RxObserver(t) {
    def mounted(): Unit = {
      // hook up to Todo changes
      val obsItems = pages.foreach { _ => t.forceUpdate() }
      onUnmount {
        // stop observing when unmounted (= never in this SPA)
        obsItems.kill()
      }
      //MainDispatcher.dispatch(RefreshTodos)
    }
  }


  // redirect all invalid routes to dashboard
  override protected val notFound = redirect(dashboardLoc, Redirect.Replace)

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
            MainMenu(MainMenu.Props(ic.loc, pages(),ws.cache))
          )
        )
      ),
      // currently active module is shown in this container
      <.div(^.className := "container")(ic.element)
    )
  }


}

//object MainRouter {
//  /**
//   * adapt the Router method for our case
//   * @param router
//   * @return
//   */
//  def componentUnbuilt[P](router: Router[P]) =
//    ReactComponentB[Unit]("Router")
//      .initialState(router.syncToWindowUrl.unsafePerformIO())
//      .backend(_ => new OnUnmount.Backend)
//      .render((_, route, _) => route.render(router))
//      .componentWillMount(_ => router.init.unsafePerformIO())
//      .configure(Listenable.installSF(_ => router, (_: Unit) => router.syncToWindowUrlS))
//
//  abstract class RxObserver[BS <: BackendScope[_, _]](scope: BS) extends OnUnmount {
//    protected def observe[T](rx: Rx[T]): Unit = {
//      val obs = rx.foreach(_ => scope.forceUpdate())
//      // stop observing when unmounted
//      onUnmount(obs.kill())
//    }
//  }
//
//  class Backend(t: BackendScope[Unit, router.Loc]) extends RxObserver(t) {
//
//  }
//
//
//}