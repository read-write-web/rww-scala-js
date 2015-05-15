package rww.ui.foaf

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all._
import rww.ui.MainRouter

import scalacss.ScalaCssReact._

/**
 * Created by hjs on 11/05/2015.
 */
object Name {
  import rww.Rdf.ops._
  import rww.ui.foaf.{FoafStyles => fstyle}

  case class NameState(edit: Option[String]=None)
  class NameBackend($: BackendScope[RelProp,NameState]) {
    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      $.state.edit.map { newName =>
        val t = $.props.arc.t
        val newTriple = Triple(t.subject,t.predicate,Literal(newName))
        println("remove: "+t)
        println("add: "+newTriple)
        MainRouter.ws.vsimplePatch($.props.npg.name,newTriple,t)
        // send a message to x in order to update the graph.
        // we need a named graph so we know where we can send the update

      }

    }
    def enterEdit(e: ReactEvent) =
      $.modState(_=>NameState(Some("")))

    def onChange(e: ReactEventI) =
      $.modState(s => NameState(Some(e.target.value)))

  }

  val component = ReactComponentB[RelProp]("PersonName")
    .initialState(NameState())
    .backend(new NameBackend(_))
    .render((P,S,B)=> {
    println("In ReactComponent PersonName")
    println("==>obj:"+P.npg.obj.toString)
    println("==>pointer:"+P.npg.obj.pointer)
    val nameOpt = P.npg.obj.pointer match {
      case Literal(name, _, _) => Some(name)
      case _ => None
    }
    println("NameOpt:"+nameOpt)
    println("S.edit:"+S.edit)
    if (S.edit.isEmpty && nameOpt.isDefined)
      div(fstyle.name, title := nameOpt.get, onClick ==> B.enterEdit)(nameOpt.get)
    else {
      form(onSubmit ==> B.handleSubmit)(
        input(fstyle.name, tpe := "text", placeholder := "Enter name",
          value := S.edit.get,
          onChange ==> B.onChange
        )
      )
    }
  }).build

  def apply(props: RelProp) = component(props)
}
