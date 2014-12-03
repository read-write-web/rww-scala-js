package rww.ui.foaf

import org.w3.banana.PointedGraph
import rww._

import scala.scalajs.js
import org.scalajs.dom.{HTMLInputElement, Node}

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.ReactVDom._
import japgolly.scalajs.react.vdom.ReactVDom.all._

class Person {
  case class PersonState(personPG: Option[PointedGraph[Rdf]],
                         edit: Boolean=false,
                         editText: String="Edit")

  val component = japgolly.scalajs.react.ReactComponentB[Unit]("Person")
}
