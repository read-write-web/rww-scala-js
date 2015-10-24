package rww.ui


import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.all._
import rww._

/**
 * Created by hjs on 15/05/2015.
 */
object Loading {
  val Loading = ReactComponentB[Rdf#URI]("Profile")
    .initialState(None)
    .renderP(($, P) => {
    // import shapeless.singleton.syntax._ <- use this when using styleC
    p("loading "+P.toString)
  })
    .build

  def apply(url: Rdf#URI) = Loading(url)
}
