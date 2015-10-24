package rww.ui.foaf

import japgolly.scalajs.react.extra.router.RouterCtl
import rww._
import rww.store.{WebUIDB, WebActor}
import rww.ui.RwwPages
import rx.Var

/**
 * Created by hjs on 10/06/2015.
 */
case class WProps[O](about: O,
                     webAgent: WebUIDB,
                     userConfig: Var[UserConfig], //todo: later fetch this info from config graph
                     ctl: RouterCtl[RwwPages],
                     edit: Boolean = false
                     )


//very simple user config class, to be extended and then added to store
case class UserConfig(id: Option[Rdf#URI]=None)
