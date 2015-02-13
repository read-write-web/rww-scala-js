package rww.ui

import scala.scalajs.js.JSApp
import scala.scalajs.js
import org.w3.banana._
import org.w3.banana.plantain.Plantain.ops._
import org.w3.banana.plantain.Plantain
import japgolly.scalajs.react._
import org.scalajs.dom._
import rww.ui.foaf._
import java.io.StringReader
import scala.util.{ Failure, Success, Try }

//NB: passed as an implicit parameter to WebClient.getRemoteProfile
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

object AppStarter extends JSApp {

  //Profile to load
  val foafUri = URI("http://bblfish.net/people/henry/card#me")

  //html element for mounting our application
  val content = document getElementById "container"

  def main = {
    val f = WebClient.getProfile(foafUri)
    f.onSuccess({ case g => println("Success" + g.pointer.toString()); React.render(Person(g), content) })
    f.onFailure({ case t => println("error:" + t.getStackTrace) })
  }
}