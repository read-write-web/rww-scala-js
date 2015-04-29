package rww.ui.foaf

import java.io.StringReader
import java.net.{URI => jURI}

import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{ReactComponentB, _}
import org.scalajs.dom
import org.scalajs.dom.document
import org.w3.banana._
import org.w3.banana.binder.ToPG
import org.w3.banana.plantain.Plantain
import rww.cache.WebStore

import scala.scalajs.js
import scala.scalajs.js.Date
import scala.util.{Failure, Success}
import scalacss.Defaults._
import scalacss.ScalaCssReact._


object Person extends js.JSApp {
  type Rdf = Plantain
  implicit val ops = Plantain.ops

  val foaf = FOAFPrefix[Rdf]

  implicit def ToURIToPG[Rdf <: RDF, T](implicit ops: RDFOps[Rdf]) = new ToPG[Rdf, jURI] {
    def toPG(t: jURI): PointedGraph[Rdf] = PointedGraph(t.asInstanceOf[Rdf#URI])
  }

  import org.w3.banana.plantain.Plantain.ops._

  case class PersonProps(personPG: PointedGraph[Rdf],
                         edit: Boolean = false,
                         editText: String = "Edit")

  class Backend(t: BackendScope[Unit, PersonProps]) {

  }

  val component = ReactComponentB[PersonProps]("Person")
    .initialState(None)
    .render((P, S, B) => {
    val x = Person(P.personPG)
    if (P.edit) p("in edit mode")
    else {
      div(className := "basic")(
        x.name.headOption.map(name=>div(FoafStyles.name,  title:=name.toString)(name.toString)) getOrElse div(),
        { val n = x.givenName.headOption.getOrElse("(unknown)");
          div(className:="surname title-case", title:=n)(n)},
        x.workPlaceHomePage.headOption.map{hp => div(className:="company", title:=hp.toString)(hp.toString)} getOrElse div(),
        img(src := x.depiction.headOption.map(_.toString).getOrElse("avatar-man.png")
        )
      )
    }
  }).build

  val el = document getElementById "eg1"

  @js.annotation.JSExport
  override def main(): Unit = {
    FoafStyles.addToDocument()
    example3()
  }

  val bbl = URI("http://bblfish.net/people/henry/card#me")
  val bblDocUri = bbl.fragmentLess

  //start with a locally built graph, find picture
  def example() = {
    val graph = (
      bbl -- foaf.depiction ->- "hello"
        -- foaf.depiction ->- URI("http://farm1.static.flickr.com/164/373663745_e2066a4950.jpg")
        -- foaf.depiction ->- URI("http://bblfish.net/pix/bfish.large.jpg")
      ).graph
    React.render(component(PersonProps(PointedGraph[Rdf](bbl, graph))), el)
  }

  //parse graph from string then show picture
  def example2() = {
   // do a request on the internet to get the file for the above url
    val foaf = "http://xmlns.com/foaf/0.1/"
    val xsd = "http://www.w3.org/2001/XMLSchema#"
    val base = bblDocUri.toString
    val bblDoc =
      s"""<$base#me> <${foaf}depiction> <http://farm1.static.flickr.com/164/373663745_e2066a4950.jpg> .
        |<$base#me> <${foaf}depiction> <http://bblfish.net/pix/bfish.large.jpg> .
        |<$base#me> <${foaf}name> "Henry" .
        |<$base#me> <${foaf}age> "42"^^<${xsd}int> .
        |<$base#me> <${foaf}near> "England"@en .
        |""".stripMargin

    println(bblDoc)


    //parse document above with Readers to get graph below

    val f = for {
      g <- Plantain.ntriplesReader.read(new StringReader(bblDoc),bblDocUri.toString)
    // one should then later also add the graph to a store
    //      _ <- appendToGraph(rww.rdf.jsstore, bblDocUri, g) //add to store
    //      graph <- getGraph(rww.rdf.jsstore,bblDocUri ) //get from store
    } yield {
      React.render(component(PersonProps(PointedGraph[Rdf](bbl, g))), el)
    }
    f.get
  }



  // for AJAX calls http://lihaoyi.github.io/hands-on-scala-js/#dom.extensions
  import org.scalajs.dom.ext._

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

  //and for CORS see http://www.html5rocks.com/en/tutorials/cors/

  def example3() = {
    //the run-now execution context should be fine as the two methods below work with callbacks
    //that presumably uses javascripts task queue (
    //todo: to be verified

    println(s"getting ${bblDocUri}")
    Ajax.get(bblDocUri.toString, headers = Map("Accept" -> "application/n-triples")).onComplete {
      case Success(xhr) => {
        val start = new Date()
        println("starting to parse " + start.toLocaleTimeString())
        for {
          g <- Plantain.ntriplesReader.read(new StringReader(xhr.responseText), bblDocUri.toString)
        // one should then later also add the graph to a store
        //      _ <- appendToGraph(rww.rdf.jsstore, bblDocUri, g) //add to store
        //      graph <- getGraph(rww.rdf.jsstore,bblDocUri ) //get from store
        } yield {
          val end = new Date()
          println("ending parse. Time taken (in ms) " + (end.getTime() - start.getTime()))
          React.render(component(PersonProps(PointedGraph[Rdf](bbl, g))), el)
        }
      }

      case Failure(f) => println("error: " + f)
    }
  }

  def example4() = {
    val ws = new WebStore()
    ws.get(URI("http://bblfish.net/people/fake/me")).map(pg => {
      println(pg)
      React.render(component(PersonProps(pg)), el)
    }).onComplete(x=>dom.console.log(x.asInstanceOf[js.Any]))
  }

//    import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
//    for {
//      i <- rdfstoreOps.loadRemote(jsstore, bblDocUri)
//      g <- JSStore.store.getGraph(JSStore.jsstore, bblDocUri)
//    } yield {
//      React.render(component(PointedGraph[Rdf](bbl, g)), el)
//    }

  case class Person(pg: PointedGraph[Rdf]) extends AnyVal {
    def name = (pg/foaf.name) map (_.pointer) collect {
      case Literal(lexicalForm,xsd.string,lang) => lexicalForm
    }
    def nick = (pg/foaf.nick) map (_.pointer) collect {
      case Literal(lexicalForm,xsd.string,lang) => lexicalForm
    }
    def givenName = (pg/foaf.givenname)++(pg/foaf.givenName) map (_.pointer) collect {
      case Literal(lexicalForm,xsd.string,lang) => lexicalForm
    }
    def familyName = (pg/foaf.familyName)++(pg/foaf.family_name) map (_.pointer) collect {
      case Literal(lexicalForm,xsd.string,lang) => lexicalForm
    }
    def firstName =  (pg/foaf.firstName) map (_.pointer) collect {
      case Literal(lexicalForm,xsd.string,lang) => lexicalForm
    }
    def workPlaceHomePage = (pg/foaf.workplaceHomepage) map (_.pointer) collect {
      case URI(u) => u
    }
    def depiction = (pg/foaf.img)++(pg/foaf.depiction) map (_.pointer) collect {
      case URI(u) => u
    }
    def logo = (pg/foaf.logo) map (_.pointer) collect {
      case URI(u) => u
    }

  }
}


object FoafStyles extends StyleSheet.Inline {

  import dsl._

  val picture = styleC {
    val top = styleS(
      width(300 px),
      height(300 px),
      borderRadius(50 pc),
      overflow.hidden,
      float.right
    )
    val img = styleS(
      width(100 pc),
      height.auto
    )
    top.named('outer) :*: img.named('image)
  }

  val name = style(
    height(50 px),
    lineHeight(50 px),
    fontSize(50 px),
    marginTop(-15 px),
    whiteSpace.nowrap,
    //textOverflow.ellipsis,
    overflow.hidden
  )


}