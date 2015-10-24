package spatutorial.client.components

import spatutorial.components.JQuery

import scala.language.implicitConversions
import scala.scalajs.js

/**
 * Common Bootstrap components for scalajs-react
 */
object Bootstrap {

  implicit def jq2bootstrap(jq: JQuery): BootstrapJQuery = jq.asInstanceOf[BootstrapJQuery]

  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  trait BootstrapJQuery extends JQuery {
    def modal(action: String): BootstrapJQuery = js.native

    def modal(options: js.Any): BootstrapJQuery = js.native
  }

  // Common Bootstrap contextual styles
  object CommonStyle extends Enumeration {
    val default, primary, success, info, warning, danger = Value
  }

  //removed a bunch of stuff moving to scala-js.react 0.10
}
