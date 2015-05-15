package spatutorial.client
import spatutorial.components.JQueryStatic

import scala.scalajs.js

package object components extends js.GlobalScope {
  val jQuery: JQueryStatic = js.native
}
