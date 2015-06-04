package rww.ui

import java.net.{URI => jURI}

import japgolly.scalajs.react.React
import japgolly.scalajs.react.extra.router2.StaticDsl.Route
import japgolly.scalajs.react.extra.router2._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs
import org.scalajs.dom
import rww._
import rww.store.WebAgent
import rww.ui.foaf.FoafStyles
import rx.core.Var
import spatutorial.client.components.GlobalStyles

import scala.collection.immutable.ListSet
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scalacss.Defaults._
import scalacss.ScalaCssReact._


/**
 * Created by hjs on 13/05/2015.
 */

sealed trait MyPages
case object URLEntry extends MyPages
case class Component(url: String) extends MyPages



@JSExport("SPAMain")
object SPAMain extends JSApp {
  import rww.Rdf
  import rww.Rdf.ops._
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
  import scala.scalajs.js.URIUtils._

  val defaultUri = "http://bblfish.net/people/henry/card#me"  //todo: pass this as parameter
  val webids = Var(ListSet[jURI]())
  val urlPathMatcher = ".*"

  val routerConfig = RouterConfigDsl[MyPages].buildConfig { dsl =>
    import dsl._
    val displayRoute: Route[Component] = ("#url" / string(urlPathMatcher)).caseclass1(Component.apply)(Component.unapply)
    ( emptyRule
      | staticRoute(root,URLEntry) ~> renderR{ (ctl)  =>
           Dashboard( defaultUri, { u: jURI => webids() = webids() + u;  ctl.set(Component(u.toString)) } )
         }
      | dynamicRouteCT(displayRoute) ~> dynRender{e: Component =>  PNGWindow(URI(e.url),ws) }
      ).notFound(redirectToPage(URLEntry)(Redirect.Replace))
  }.renderWith(layout)

  // base layout for all pages
  def layout(c: RouterCtl[MyPages], r: Resolution[MyPages]) = {
    <.div(
      // here we use plain Bootstrap class names as these are specific to the top level layout defined here
      <.nav(^.className := "navbar navbar-inverse navbar-fixed-top")(
        <.div(^.className := "container")(
          <.div
            (^.className := "navbar-header")(<.span(^.className := "navbar-brand")("SPA Tutorial")),
          <.div(^.className := "collapse navbar-collapse")(
            MainMenu(MainMenu.Props(c, r.page,webids))
          )
        )
      ),
      // currently active module is shown in this container
      <.div(^.className := "container")(r.render())
    )
  }

  def proxy(uri: Rdf#URI): Rdf#URI = URI("https://rww.io/proxy.php?uri="+uri.toString)

  val ws = new WebAgent(proxy)


  @JSExport
  def main(): Unit = {
    // tell React to render the router in the document body
    GlobalStyles.addToDocument()
    FoafStyles.addToDocument()


    val baseUrl = BaseUrl(scalajs.dom.window.location.href.takeWhile(_ != '#'))
    val router =  Router(baseUrl, routerConfig)
    React.render( router(), dom.document.body)
   }



}