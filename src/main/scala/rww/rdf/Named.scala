package rww.rdf

import org.w3.banana.RDF


/** trait for things that have names */
case class Named[Rdf <: RDF, T](name: Rdf#URI, obj: T) {
  def map[O](f: T => O): Named[Rdf, O] = Named(name, f(obj))
}

