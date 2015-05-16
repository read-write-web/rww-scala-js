package rww.ui


import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.extra.LogLifecycle
import japgolly.scalajs.react.vdom.all._
import rww._
import rww.ontology.Person
import rww.rdf.Named
import rww.ui.foaf.FoafStyles

/**
 * Created by hjs on 15/05/2015.
 */
object Loading {
  val Loading = ReactComponentB[Rdf#URI]("Profile")
    .initialState(None)
    .render((P, S, B) => {
    // import shapeless.singleton.syntax._ <- use this when using styleC
    p("loading "+P.toString)
  })
    .build

  def apply(url: Rdf#URI) = Loading(url)
}
