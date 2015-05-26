package rww.ui.foaf


import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.all._
import rww.ui.rdf.NPGPath

import scalacss.ScalaCssReact._

/**
 * Created by hjs on 16/05/2015.
 */
object Image {

  import rww.Rdf.ops._
  import rww.ui.foaf.{FoafStyles => fstyle}
  
  val component = ReactComponentB[WProps[Option[NPGPath]]]("Image")
    .initialState(())
//  .backend(new Backend(_))
    .render((P, S, B) => {
    val nameOpt = P.about.flatMap {
      _.pg.pointer match {
        case URI(u) => Some(u)
        case _ => None
      }
    }
    div(fstyle.pictureUnsafe)(
      img(src := nameOpt.getOrElse("avatar-man.png"))
    )
  }).build

  def apply(pProps: WProps[Option[NPGPath]]) = {
    //this component should be able to capture a number of images, and jump between them
    //the type should perhaps be HasPicture, so that it can be applied much more generally
    component(pProps)
  }
}
