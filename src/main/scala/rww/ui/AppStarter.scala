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
  
  //TODO: cache graph in local store on success
  val success: Plantain#URI => Element => XMLHttpRequest => Unit = profileUri => mountPoint => xhr => {
    val docUri = profileUri.fragmentLess
    for {
      g <- Plantain.ntriplesReader.read(new StringReader(xhr.responseText), docUri.toString)
      // one should then later also add the graph to a store
      //      _ <- appendToGraph(rww.rdf.jsstore, bblDocUri, g) //add to store
      //      graph <- getGraph(rww.rdf.jsstore,bblDocUri ) //get from store
    } yield React.render(Person(PointedGraph[Plantain](profileUri, g)), mountPoint)
  }

  val failure: Plantain#URI => Throwable => Unit = docUri => t => {
    println("error: " + t.getStackTrace)
  }
  
  //Profile to load
  val foafUri = URI("http://bblfish.net/people/henry/card#me")
  
  //html element for mounting our application
  val content = document getElementById "container"

  //NB: Having some trouble with CORS proxy
  //  var corsProxyUri = URI("https://localhost:8443/srv/cors")
  
  def main = {
    val successF = success(foafUri)(content)
    val failureF = failure(foafUri)
    val onComplete = WebClient.generateOnComplete(foafUri.fragmentLess)(successF, failureF)
    WebClient.getRemoteProfile(foafUri).onComplete(onComplete)
  }
}