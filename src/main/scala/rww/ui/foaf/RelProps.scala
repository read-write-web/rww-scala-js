package rww.ui.foaf

import org.w3.banana.PointedGraph
import rww.Rdf
import rww.rdf.Named

/**
 * Created by hjs on 11/05/2015.
 */
case class RelProp(npg: Named[Rdf,PointedGraph[Rdf]],
                   arc: Arc,
                   edit: Boolean = false)

sealed trait Arc {
  def t: Rdf#Triple
}
case class Rev(t: Rdf#Triple) extends Arc
case class Rel(t: Rdf#Triple) extends Arc

