package rww.ui

import scala.util.{ Failure, Success, Try }
import org.w3.banana._
import org.w3.banana.plantain.Plantain
import org.w3.banana.plantain.Plantain.ops._
import org.scalajs.dom.extensions._
import org.scalajs.dom._
import scala.scalajs.js.Date
import scala.scalajs.concurrent.JSExecutionContext
import scala.concurrent._

object WebClient {

  /**
   * Issue an ajax request to get a remote foaf profile
   */
  def getRemoteProfile(foafProfileUri: Plantain#URI)(implicit ec: ExecutionContext): Future[XMLHttpRequest] =
    Ajax.get(foafProfileUri.fragmentLess.toString, headers = Map("Accept" -> "application/n-triples"))

  /**
   * Generates a onComplete function for the future gotten from "getRemoteProfile" method
   */
  def generateOnComplete(docUri: Plantain#URI)(successF: XMLHttpRequest => Unit, failureF: Throwable => Unit): Try[XMLHttpRequest] => Unit = {
    case Success(xhr) => {
      val start = new Date()
      println("starting to parse" + start.toLocaleTimeString())

      //###########
      successF(xhr)
      //###########

      val end = new Date()
      println("parsed. Time taken (in ms) " + (end.getTime() - start.getTime()))

    }

    case Failure(f) => failureF(f)
  }

}