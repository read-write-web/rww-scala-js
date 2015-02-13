package rww.ui

import scala.util.{ Failure, Success }
import org.w3.banana._
import org.w3.banana.plantain.Plantain
import org.w3.banana.plantain.Plantain.ops._
import org.scalajs.dom.extensions._
import org.scalajs.dom._
import scala.scalajs.concurrent.JSExecutionContext
import scala.concurrent._
import org.w3.banana.io.NTriplesWriter
import java.io.{ ByteArrayOutputStream, StringReader }

object WebClient {

  /**
   * Issue an AJAX GET request to get a remote foaf profile
   */
  def getProfile(foafProfileUri: Plantain#URI)(implicit ec: ExecutionContext): Future[PointedGraph[Plantain]] = {
    val responseF = Ajax.get(foafProfileUri.fragmentLess.toString, headers = Map("Accept" -> "application/n-triples"))
    readResponse(foafProfileUri, foafProfileUri.fragmentLess)(responseF)
  }

  /**
   * Issue an AJAX PUT request to update a remote foaf profile
   */
  def putProfile(newGraph: PointedGraph[Plantain])(implicit ec: ExecutionContext): Future[PointedGraph[Plantain]] = {
    var os = new ByteArrayOutputStream()
    val writeTry = Plantain.ntriplesWriter.write(newGraph.graph, os, "")
    writeTry match {
      case Success(u) => {
        val profileUri = URI(newGraph.pointer.toString)
        val res = Ajax.put(profileUri.fragmentLess.toString, os.toString, headers = Map("Accept" -> "application/n-triples"))
        readResponse(profileUri, profileUri.fragmentLess)(res)
      }
      case Failure(t) => Future.failed(t)
    }
  }

  def readResponse(pointer: Plantain#URI, base: Plantain#URI)(response: Future[XMLHttpRequest])(implicit ec: ExecutionContext): Future[PointedGraph[Plantain]] = {
    response flatMap { xhr =>
      val t = for {
        g <- Plantain.ntriplesReader.read(new StringReader(xhr.responseText), base.toString)
        // one should then later also add the graph to a store
        //      _ <- appendToGraph(rww.rdf.jsstore, bblDocUri, g) //add to store
        //      graph <- getGraph(rww.rdf.jsstore,bblDocUri ) //get from store
      } yield PointedGraph[Plantain](pointer, g)
      t match {
        case Success(pg) => Future.successful(pg)
        case Failure(t)  => Future.failed(t)
      }
    }
  }

}