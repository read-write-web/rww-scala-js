package rww.ui

import java.io.StringReader

import org.scalajs.dom
import org.scalajs.dom.ext._
import org.w3.banana._
import org.w3.banana.plantain.Plantain
import org.w3.banana.plantain.Plantain.ops._

import scala.concurrent._

object WebClient {
  import rww.rdf._

  /**
   * Issue an AJAX GET request to get a remote foaf profile
   */
  def getProfile(foafProfileUri: Plantain#URI)(implicit ec: ExecutionContext): Future[PointedGraph[Plantain]] = {
    val base = foafProfileUri.fragmentLess.toString
    for {
      xhr <- Ajax.get(foafProfileUri.fragmentLess.toString, headers = Map("Accept" -> "application/n-triples"))
        g <- ntriplesReader.read(new StringReader(xhr.responseText), base.toString).asFuture
      // one should then later also add the graph to a store
      //      _ <- appendToGraph(rww.rdf.jsstore, bblDocUri, g) //add to store
      //      graph <- getGraph(rww.rdf.jsstore,bblDocUri ) //get from store
      } yield PointedGraph[Plantain](foafProfileUri, g)
  }

  /**
   * Issue an AJAX PUT request to update a remote foaf profile
   */
  def putProfile(newGraph: PointedGraph[Plantain])(implicit ec: ExecutionContext): Future[dom.XMLHttpRequest] = {
    val writeTry = ntriplesWriter.asString(newGraph.graph, "")
    writeTry.asFuture.flatMap { nt =>
        val profileUri = URI(newGraph.pointer.toString)
        Ajax.put(profileUri.fragmentLess.toString, nt, headers = Map("Accept" -> "application/n-triples"))
    }
  }


}