package rww.ui.foaf

import java.net.{URI => jURI}

import japgolly.scalajs.react.ScalazReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import rww.ui.MainRouter
import rww.ui.rdf.NPGPath

import scala.util.Try
import scalacss.ScalaCssReact._
import scalacss.StyleA
import scalaz.effect.IO

/**
 * Generalised Editable Text field.
 */
trait EditableField {
  type T
  val ST = ReactS.Fix[Option[String]]

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
  def apply(npg: NPGPath, editMode: Boolean, title: String, style: StyleA = fstyle.name) = {
    //todo: get all the various pieces of name out
    component(Props(npg, editMode, title, style: StyleA))
  }

  case class Props(display: NPGPath, editMode: Boolean, title: String, style: StyleA = fstyle.name) {
    val placeholder = "Enter " + title
  }

  val component = ReactComponentB[Props]("EditableTextBox")
    .initialState[Option[String]](None)
    .renderS(($, P, S) => {
    val nameOpt = objToTag($.props.display)
    if (!P.editMode || (S.isEmpty && nameOpt.isDefined))
      <.div(P.style, ^.title := P.title, ^.onClick ~~> $._runState(enterEdit(P,S)))(nameOpt)
    else <.form(^.onSubmit ~~> $._runState(handleSubmit(P, S)))(
      <.input(P.style, ^.tpe := "text", ^.placeholder := P.placeholder,
        ^.value := S,
        ^.onChange ~~> $._runState(onChange)
      )
    )
  }).build

  def onChange(e: ReactEventI) = ST.set(Some(e.target.value))

  def enterEdit(props: Props, currentState: Option[String])(e: ReactEventI) = {
    if (props.editMode) {
      objToState(props.display) match {
        case s@Some(_) => ST.set(s)
        case None => ST.nop
      }
    } else ST.nop
  }

  def handleSubmit(props: Props, stateopt: Option[String])(e: ReactEventI) =
    ST.retM(e.preventDefaultIO)  addCallback IO {
      val f = for {
        text <- stateopt
        value <- validate(text).toOption
        arc <- props.display.path.headOption
      } yield {
        val removeTriple = arc.arrow.rel
        val newTriple = Triple(removeTriple.subject, removeTriple.predicate, value)
        MainRouter.ws.vsimplePatch(props.display.target.name, newTriple, removeTriple)
      }
      ()
    }

  /* calculate the html that should be displayed */
  def objToTag(nameNpg: NPGPath): Option[ReactTag]

  /* calculate the state from the object */
  def objToState(nameNpg: NPGPath): Option[String]

  /* validate the user entry */
  def validate(string: String): Try[T]

}

object URLField extends EditableField {
  import rww.Rdf.ops._
  type T = rww.Rdf#URI

  //this can be moved to the class
  //genericise these functions

  def objToTag(nameNpg: NPGPath): Option[ReactTag] = {
    nameNpg.target.obj.pointer match {
      case URI(name) => {
        Try(new jURI(name)).map(u => <.a(^.href := u.toString, ^.target := "_blank")(u.getHost)).toOption
      }
      case _ => None
    }
  }

  def objToState(nameNpg: NPGPath): Option[String] = {
    nameNpg.pg.pointer match {
      case URI(u) => Some(u)
      case Literal(lit, _, _) => Some(lit)
      case _ => None
    }
  }

  def validate(string: String): Try[T] = Try {
    val juri = new jURI(string)
    URI(string)
  }

}

object TextField extends EditableField {
  import rww.Rdf.ops._
  type T = rww.Rdf#Literal

  //this can be moved to the class
  //genericise these functions

  def objToTag(nameNpg: NPGPath): Option[ReactTag] = {
    nameNpg.target.obj.pointer match {
      case Literal(name,_,_) => Some(<.span(name))
      case _ => None
    }
  }

  def objToState(nameNpg: NPGPath): Option[String] = {
    nameNpg.pg.pointer match {
      case Literal(lit, _, _) => Some(lit)
      case _ => None
    }
  }

  def validate(string: String): Try[T] = Try(Literal(string))

}