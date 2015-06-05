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
import scala.scalajs.js.URIUtils._


/**
 * Created by hjs on 13/05/2015.
 */

sealed trait RwwPages
case object URLEntry extends RwwPages
case class Component(encodedUrl: String) extends RwwPages {
  import rww.Rdf.ops._
  lazy val url = decodeURIComponent(encodedUrl)
  lazy val asURI = URI(url)
  lazy val asJUri = new jURI(url)
}
object Component {
  def apply(uri: jURI) = new Component(encodeURIComponent(uri.toString))
}



@JSExport("SPAMain")
object SPAMain extends JSApp {
  import rww.Rdf
  import rww.Rdf.ops._
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

  val defaultUri = "http://bblfish.net/people/henry/card#me"  //todo: pass this as parameter
  val windowHistory = Var(ListSet[jURI]())
  val urlPathMatcher = ".*"

  def openWindow(u: jURI, ctl: RouterCtl[RwwPages]) = {
   
    
  }

  val routerConfig = RouterConfigDsl[RwwPages].buildConfig { dsl =>
    import dsl._
    val displayRoute: Route[Component] = ("#url" / string(urlPathMatcher)).caseclass1(Component.apply)(Component.unapply)
    ( emptyRule
      | staticRoute(root,URLEntry) ~> renderR{ (ctl) => Dashboard( defaultUri, u=>ctl.set(Component(u)) ) }
      | dynamicRouteCT(displayRoute) ~> dynRenderR{ (cmpnent,ctl) =>
         windowHistory() = windowHistory() + cmpnent.asURI
         PNGWindow(cmpnent.asURI,ws, ctl)
      }
      ).notFound(redirectToPage(URLEntry)(Redirect.Replace))
  }.renderWith(layout)

  // base layout for all pages
  def layout(c: RouterCtl[RwwPages], r: Resolution[RwwPages]) = {
    <.div(
      // here we use plain Bootstrap class names as these are specific to the top level layout defined here
      <.nav(^.className := "navbar navbar-inverse navbar-fixed-top")(
        <.div(^.className := "container")(
          <.div
            (^.className := "navbar-header")(<.span(^.className := "navbar-brand")("RWW")),
          <.div(^.className := "collapse navbar-collapse")(
            MainMenu(MainMenu.Props(c, r.page,windowHistory))
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