package rww.ui

import japgolly.scalajs.react.React
import org.scalajs
import rww.ui.foaf.FoafStyles
import spatutorial.client.components.GlobalStyles

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scalacss.Defaults._
import scalacss.ScalaCssReact._

/**
 * Created by hjs on 13/05/2015.
 */
@JSExport("SPAMain")
object SPAMain extends JSApp {
  @JSExport
  def main(): Unit = {
    // tell React to render the router in the document body
    GlobalStyles.addToDocument()
    FoafStyles.addToDocument()
    React.render(MainRouter.routerComponent(), scalajs.dom.document.body)
  }
}