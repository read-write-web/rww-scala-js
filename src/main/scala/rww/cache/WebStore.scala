package rww.cache

import java.io.StringReader

import org.scalajs.dom.ext.Ajax
import org.w3.banana.PointedGraph
import org.w3.banana.plantain.Plantain

import scala.collection.mutable.HashMap
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.Date
import scalaz.{-\/, \/, \/-}


class WebStore(implicit ec: ExecutionContext) {

  type Rdf = Plantain
  implicit val ops = Plantain.ops
  import ops._

  val cache = new HashMap[Rdf#URI,\/[Rdf#URI,Rdf#Graph]]()

  def get(url: Rdf#URI): Future[PointedGraph[Rdf]] = {
    val base = url.fragmentLess
    import org.w3.banana.TryW
    Ajax.get(
      base.toString,
      headers = Map("Accept" -> "application/n-triples")
    ).flatMap { xhr =>
      val start = new Date()
      val redirectURLOpt = xhr.asInstanceOf[js.Dynamic].responseURL.asInstanceOf[js.UndefOr[String]]
      println("starting to parse " + start.toLocaleTimeString())
      val f = for {
        g <- Plantain.ntriplesReader.read(new StringReader(xhr.responseText), base.toString)
      } yield {
          val end = new Date()
          println("ending parse. Time taken (in ms) " + (end.getTime() - start.getTime()))
          val graphUrl = redirectURLOpt.map { redirectURLstr =>
            val redirectURL = URI(redirectURLstr)
            if (redirectURL == base) base
            else {
              cache.put(base,-\/(redirectURL))
              redirectURL
            }
          } getOrElse {
            base
          }
          cache.put(graphUrl, \/-(g))

          PointedGraph[Rdf](url, g)
        }
      f.asFuture
    }
  }


}
