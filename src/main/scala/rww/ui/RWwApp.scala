package rww.ui

import java.net.{URI => jURI}

import japgolly.scalajs.react.ReactDOM
import japgolly.scalajs.react.extra.router.StaticDsl.Route
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs
import org.scalajs.dom
import rww._
import rww.store.WebAgent
import rww.ui.foaf.{UserConfig, WProps, FoafStyles}
import rx.core.Var
import spatutorial.client.components.GlobalStyles

import scala.collection.immutable.ListSet
import scala.scalajs.js
import scala.scalajs.js.URIUtils._
import scala.scalajs.js.annotation.{JSExport, JSExportNamed}
import scala.util.Try
import scalacss.Defaults._
import scalacss.ScalaCssReact._


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
  def uri(uri: Rdf#URI) = new Component(encodeURIComponent(uri.toString))

}

@JSExport
object RWwApp {

  @JSExportNamed
  def main(dashboardUri: js.UndefOr[String],
           proxySrvc: String,
           dev_proxySrvc: js.UndefOr[String],
           webIDauth: js.Array[String]) = {
    val authEndpoints = webIDauth.flatMap(u=> Try(new jURI(u)).toOption.toList)
    val origin = new jURI(scalajs.dom.window.location.origin)
    val proxy = dev_proxySrvc.map(new jURI(_)).flatMap(p=>

      if (List("localhost","127.0.0.1").contains(origin.getHost)
        && origin.getPort == p.getPort
        && origin.getScheme == p.getScheme)
         dev_proxySrvc
      else js.undefined
    ).getOrElse(proxySrvc)

    new RWwApp(dashboardUri.getOrElse(""), proxy, authEndpoints.toList).run()
  }
}



class RWwApp( startURI: String,
              proxyService: String,
              authEndpoints: List[jURI]) {
  import rww.Rdf.ops._

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

  val baseUrl = BaseUrl(scalajs.dom.window.location.href.takeWhile(_ != '#'))
  val origin = new jURI(scalajs.dom.window.location.origin)

  val ws = new WebAgent(proxy)

  val windowHistory = Var(ListSet[jURI]())
  val id: Var[UserConfig] = Var(UserConfig())  //todo: later add to app preferences https://github.com/read-write-web/rww-scala-js/issues/17

  val urlPathMatcher = ".*"

  def openWindow(u: jURI, ctl: RouterCtl[RwwPages]) = {
   
    
  }

  val routerConfig = RouterConfigDsl[RwwPages].buildConfig { dsl =>
    import dsl._
    val displayRoute: Route[Component] = ("#url" / string(urlPathMatcher)).caseClass[Component]
    ( emptyRule
      | staticRoute(root,URLEntry) ~> renderR{ (ctl) => Dashboard(WProps((startURI,authEndpoints),ws,id,ctl) ) }
      | dynamicRouteCT(displayRoute) ~> dynRenderR{ (cmpnent,ctl) =>
         windowHistory() = windowHistory() + cmpnent.asURI
         PNGWindow(WProps(cmpnent.asURI,ws,id,ctl))
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

  //todo: should the method return \/[URI,URI] to indicate if a proxy was used?
  def proxy(uri: Rdf#URI): Rdf#URI =
    if (uri.getScheme == origin.getScheme
      && uri.getAuthority == origin.getAuthority
      && uri.getPort == origin.getPort)
      uri
    else URI(proxyService + uri.toString)


  def run(): Unit = {
    // tell React to render the router in the document body
    GlobalStyles.addToDocument()
    FoafStyles.addToDocument()

    val router =  Router(baseUrl, routerConfig)
    ReactDOM.render( router(), dom.document.body)
  }

}


