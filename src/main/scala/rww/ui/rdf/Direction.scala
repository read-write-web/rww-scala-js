package rww.ui.rdf

import org.w3.banana.PointedGraph
import rww.Rdf
import rww.rdf.Named

import scalaz.NonEmptyList


/** direction of a relation, either forward or reversed */
sealed trait Direction[T] {
  def rel: T
}

case class Rel[T](rel: T) extends Direction[T]
case class Rev[T](rel: T) extends Direction[T]

/**
 * an arc is a relation between two objects the source, and the target
 * We think of the pointer being on the target, and the source as the origin
 * that reached it by following the Relation Rel or the Inverse when Rev
 *
 * Usage: this helps keep track of which relations were followed to get
 * to a point.

 *
 * @tparam T anything that can be thought of as pointed, eg. PointedGraph,
 *           NamedPointedGraph, or Scala object of scala Named Object
 *           (when we wish to map the object to a particular graph.
 * @tparam R the relation:
 *       * if T is a graph or named graph then R can be an Rdf#Triple
 *       * if T is a pointedGraph or named pointed graph then R can just be the name of a relation
 *       * if T is a scala object, then this is similar to previous
 */
trait Arc[T,R] {
  def source: T
  def target: T
  def rel: Direction[R]
}

///Q: would a linked list not do
/// head - source
/// tail - target
/// with an extra field for the type of relation.

/**
 * A path of connected Arcs.
 *
 * todo: make sure they are connected
 * Q: is this just a NonEmptyList with some methods around it ?
 * Q: An ArcPath can be an Arc too, with source, target,
 *    and R the composition of relations
 *
 * @tparam T see Arc
 * @tparam R see Arc.
 */
trait ArcPath[T,R]  {
  def path: NonEmptyList[Arc[T,R]]
  def target: T
}

case class NPGArc(source: Named[Rdf,PointedGraph[Rdf]],
                  rel: Direction[Rdf#URI],
                  target: Named[Rdf,PointedGraph[Rdf]]) extends Arc[Named[Rdf,PointedGraph[Rdf]],Rdf#URI] {
  import rww.Rdf.ops._
   def arrow: Direction[Rdf#Triple] = rel match {
     case Rel(uri) => Rel(Triple(source.obj.pointer,uri,target.obj.pointer))
     case Rev(uri) => Rev(Triple(target.obj.pointer,uri,source.obj.pointer))
   }
}


/**
 * A Named Pointed Graph path
 * @param path the path
 * note: NPGPath could easily be a form of linked list
 */
case class NPGPath(target: Named[Rdf,PointedGraph[Rdf]],
                   path: List[NPGArc]=List())  {
  
  def pg =  target.obj
  import Rdf.ops

  def /->(p: Rdf#URI): Iterable[NPGPath] = {
    val nodes = ops.getObjects(pg.graph, pg.pointer, p)
    nodes.map{node=>
      val newnpg = Named(target.name,PointedGraph(node,target.obj.graph))
      NPGPath(newnpg,
        List(NPGArc(target,Rel(p),newnpg))
      )
    }
  }

  def /<-(p: Rdf#URI): Iterable[NPGPath] = {
    val nodes = ops.getSubjects(pg.graph, p, pg.pointer)
    nodes.map{ node=>
      val newnpg = Named(target.name,PointedGraph(node,target.obj.graph))
      NPGPath(newnpg,
        List(NPGArc(target,Rev(p),newnpg))
      )
    }
  }

}


class NPGPathIterableW(val inpg: Iterable[NPGPath]) extends AnyVal {
  import rww.Rdf.ops._

  //collection of utility functions
  //todo: generalise and simplify them

  //filter and select first
  def toLitStr = toPointer collect { case Literal(l,_,_) => l }
  def toUriStr = toPointer collect { case URI(u) => u }

  def toPointer = inpg.map ( _.pg.pointer )

}