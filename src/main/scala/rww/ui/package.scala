package rww.ui

import org.w3.banana._
import org.w3.banana.plantain.Plantain.ops._
import org.w3.banana.plantain.Plantain

package object foaf  {
  type PersonProps = PointedGraph[Plantain]
  val FOAF = FOAFPrefix[Plantain]
}