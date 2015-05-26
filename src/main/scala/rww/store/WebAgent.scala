package rww.store

import java.io.StringReader
import java.net.URLEncoder

import org.scalajs.dom.ext.Ajax
import org.w3.banana.io._
import org.w3.banana.{PointedGraph, RDF, RDFOps}
import rww.rdf.Named
import rx.Var

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.Try
import scalaz.{Id, -\/, \/-}


class WebAgent[Rdf <: RDF](proxy: Rdf#URI => Rdf#URI = (u: Rdf#URI)=>u)
                          (implicit
                           ec: ExecutionContext,
                           ops: RDFOps[Rdf],
                           rdrNT:  RDFReader[Rdf, Try, NTriples],
                           rdrTurtle:  RDFReader[Rdf, Future, Turtle],
                           rdrJSONLD: RDFReader[Rdf, Future, JsonLd]) {

  import ops._

  val cache: Var[WebView[Rdf]] = Var(new WebView[Rdf]())

  //todo Q: should fetch return anything?
  def fetch(url: Rdf#URI): Future[Named[Rdf, PointedGraph[Rdf]]] = {
    // for AJAX calls http://lihaoyi.github.io/hands-on-scala-js/#dom.extensions
    //and for CORS see http://www.html5rocks.com/en/tutorials/cors/
    val base = url.fragmentLess
    import org.w3.banana.TryW
    cache().get(url) match {
      case Some(res) => Future.successful(res)
      case None => {
        val proxiedURL = proxy(base)
        Ajax.get(
          proxiedURL.toString,
          headers = Map("Accept" -> "application/n-triples,application/ld+json;q=0.8,text/turtle;q=0.8")
        ) flatMap { xhr =>
          // responseURL is quite new. See https://xhr.spec.whatwg.org/#the-responseurl-attribute
          // hence need for UndefOr
          // todo: to remove dynamic use need to fix https://github.com/scala-js/scala-js-dom/issues/111
          val redirectURLOpt = xhr.asInstanceOf[js.Dynamic].responseURL.asInstanceOf[js.UndefOr[String]]
          val rh = Option(xhr.getResponseHeader("Content-Type"))
          println("fetching: "+proxiedURL.toString)
          println("Content-Type:: "+rh)
          val reader = new StringReader(xhr.responseText)
          for {
            g <-  rh.map(_.takeWhile(_ != ';')) match {
              case Some("application/n-triples") => rdrNT.read(reader, base.toString).asFuture
              case Some("application/ld+json") => rdrJSONLD.read(reader,base.toString)
              case Some("text/turtle") => rdrTurtle.read(reader,base.toString)
              case Some(other) => Future.failed(new Exception("could not find parser for "+other))
              case None => Future.failed(new Exception("problem fetching remote resource:"+xhr.statusText))
            }
          } yield {
            val graphUrl = redirectURLOpt.map { redirectURLstr =>
              val redirectURL = URI(redirectURLstr)
              if (redirectURL == proxiedURL) base else redirectURL
            } getOrElse {
              base
            }
            println("graphUrl="+graphUrl)
            cache.update {
              val cvh = if (graphUrl != base) {
                cache().cache.updated(base, -\/(graphUrl))
              } else cache().cache
              val cvh2 = cvh.updated(graphUrl, \/-(g))
              new WebView(cvh2)
            }
            Named(url, PointedGraph[Rdf](url, g))
          }
        }
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

