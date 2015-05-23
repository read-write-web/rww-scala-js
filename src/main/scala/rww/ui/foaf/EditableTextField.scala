package rww.ui.foaf

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import rww.ui.MainRouter
import rww.ui.rdf.NPGPath

import scalacss.ScalaCssReact._
import scalacss.StyleA

/**
 * Generalised Editable Text field.
 */
object EditableTextField {

  import rww.Rdf.ops._
  import rww.ui.foaf.{FoafStyles => fstyle}

  /**
   *
   * @param npg the NamedPointedGraph with path
   * @param editMode true if the component is in edit mode
   * @param title the title of the component ( shown when for mouse hover )
   * @param style the style to give the component
   * @return the props
   */
  def apply(npg: NPGPath, editMode: Boolean,title: String, style: StyleA=fstyle.name) = {
    //todo: get all the various pieces of name out
    component(Props(npg,editMode,title, style: StyleA))
  }

  case class State(edit: Option[String] = None)
  case class Props(text: NPGPath, editMode: Boolean, title: String, style: StyleA=fstyle.name) {
    val placeholder = "Enter "+title
  }
  
  val component = ReactComponentB[Props]("EditableTextBox")
    .initialState(State())
    .backend(new NameBackend(_))
    .render((P, S, B) => {
      val nameNpg = P.text
      val nameOpt: Option[String] = nameNpg.target.obj.pointer match {
        case Literal(name, _, _) => Some(name)
        case _ => None
      }
     if (!P.editMode || (S.edit.isEmpty && nameOpt.isDefined))
      <.div(P.style, ^.title := "name", ^.onClick ==> B.enterEdit) (nameOpt)
     else <.form( ^.onSubmit ==> B.handleSubmit )(
      <.input(P.style, ^.tpe := "text", ^.placeholder := P.placeholder,
        ^.value := S.edit,
        ^.onChange ==> B.onChange
      )
    )
  }).build



  class NameBackend($: BackendScope[Props, State]) {
    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      $.state.edit.map { newName =>
        $.props.text.path.headOption.map{ arc =>
          val removeTriple = arc.arrow.rel
          val newTriple = Triple(removeTriple.subject, removeTriple.predicate, Literal(newName))
          MainRouter.ws.vsimplePatch($.props.text.target.name, newTriple, removeTriple) map { _ =>
            //todo: as this should return a future this will need to be changed
            $.setState(State())
          }
        } getOrElse {
          println("warning: path did not have an origin")
        }
        // send a message to x in order to update the graph.
        // we need a named graph so we know where we can send the update
      }
    }

    def enterEdit(e: ReactEvent) = {
      if ($.props.editMode) $.setState(State(Some("")))
    }
    def onChange(e: ReactEventI) =
      $.modState(s => State(Some(e.target.value)))

  }

}
