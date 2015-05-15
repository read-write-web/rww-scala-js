package rww.store

import java.io.StringReader

import org.scalajs.dom.ext.Ajax
import org.w3.banana.io.{NTriples, RDFReader}
import org.w3.banana.{PointedGraph, RDF, RDFOps}
import rww.rdf.Named
import rx.Var

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.Try
import scalaz.{-\/, \/-}


class WebAgent[Rdf <: RDF](proxy: Option[Rdf#URI])
                          (implicit
                           ec: ExecutionContext,
                           ops: RDFOps[Rdf],
                           reader: RDFReader[Rdf, Try, NTriples]) {

  import ops._

  val cache: Var[WebView[Rdf]] = Var(new WebView[Rdf]())

  //todo Q: should fetch return anything?
  def fetch(url: Rdf#URI): Future[Named[Rdf, PointedGraph[Rdf]]] = {
    println("fetching url " + url.toString)
    // for AJAX calls http://lihaoyi.github.io/hands-on-scala-js/#dom.extensions
    //and for CORS see http://www.html5rocks.com/en/tutorials/cors/
    val base = url.fragmentLess
    import org.w3.banana.TryW
    cache().get(url) match {
      case Some(res) => Future.successful(res)
      case None => Ajax.get(
        base.toString,
        headers = Map("Accept" -> "application/n-triples")
      ) flatMap { xhr =>
        // responseURL is quite new. See https://xhr.spec.whatwg.org/#the-responseurl-attribute
        // hence need for UndefOr
        // todo: to remove dynamic use need to fix https://github.com/scala-js/scala-js-dom/issues/111
        val redirectURLOpt = xhr.asInstanceOf[js.Dynamic].responseURL.asInstanceOf[js.UndefOr[String]]
        val tryNamed = for {
          g <- reader.read(new StringReader(xhr.responseText), base.toString)
        } yield {
            val graphUrl = redirectURLOpt.map { redirectURLstr =>
              val redirectURL = URI(redirectURLstr)
              if (redirectURL == base) base else redirectURL
            } getOrElse {
              base
            }

            cache.update {
              val cvh = if (graphUrl != base) {
                cache().cache.updated(base, -\/(graphUrl))
              } else cache().cache
              val cvh2 = cvh.updated(graphUrl, \/-(g))
              new WebView(cvh2)
            }
            Named(url, PointedGraph[Rdf](url, g))
          }
        tryNamed.asFuture
      }
    }
  }

  //just to get going
  //should it return the new graph?
  def vsimplePatch(url: Rdf#URI, add: Rdf#Triple, remove: Rdf#Triple): Option[Named[Rdf, Rdf#Graph]] = {
    val r = cache().get(url) map { named =>
      named map { pg =>
        val graph = pg.graph
        val g = graph.diff(Graph(remove))
        val newg = g union Graph(add)
        cache.update {
          val newcache = cache().cache.updated(url, \/-(newg))
          new WebView(newcache)
        }
        newg
      }
    }
    r
  }

}

