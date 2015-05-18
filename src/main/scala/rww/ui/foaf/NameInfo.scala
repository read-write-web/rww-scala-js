package rww.ui.foaf

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import rww.ontology.Person
import rww.ui.MainRouter
import rww.ui.rdf.NPGPath

import scalacss.ScalaCssReact._

/**
 * Created by hjs on 11/05/2015.
 */
object NameInfo {

  import rww.Rdf.ops._
  import rww.ui.foaf.{FoafStyles => fstyle}

  val component = ReactComponentB[NPGPath]("PersonName")
    .initialState(State())
    .backend(new NameBackend(_))
    .render((P, S, B) => {
      val nameOpt = P.pg.pointer match {
        case Literal(name, _, _) => Some(name)
        case _ => None
      }
     if (S.edit.isEmpty && nameOpt.isDefined)
      <.div(fstyle.name, ^.title := nameOpt.get, ^.onClick ==> B.enterEdit)(nameOpt.get)
    else <.form(^.onSubmit ==> B.handleSubmit)(
      <.input(fstyle.name, ^.tpe := "text", ^.placeholder := "Enter name",
        ^.value := S.edit.get,
        ^.onChange ==> B.onChange
      )
    )
  }).build

  def apply(npg: NPGPath) = {
    //todo: get all the various pieces of name out
    component(npg)
  }

  case class State(edit: Option[String] = None)

  class NameBackend($: BackendScope[NPGPath, State]) {
    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      $.state.edit.map { newName =>
        $.props.path.headOption.map{ arc =>
          val removeTriple = arc.arrow.rel
          val newTriple = Triple(removeTriple.subject, removeTriple.predicate, Literal(newName))
          MainRouter.ws.vsimplePatch($.props.target.name, newTriple, removeTriple).map { _ =>
            //todo: as this should return a future this will need to be changed
            $.modState(p => State(None))
          }
        } getOrElse {
          println("warning: path did not have an origin")
        }
        // send a message to x in order to update the graph.
        // we need a named graph so we know where we can send the update
      }
    }

    def enterEdit(e: ReactEvent) =
      $.modState(_ => State(Some("")))

    def onChange(e: ReactEventI) =
      $.modState(s => State(Some(e.target.value)))

  }

}
