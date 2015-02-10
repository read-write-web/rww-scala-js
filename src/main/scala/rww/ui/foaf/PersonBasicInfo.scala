package rww.ui.foaf

import japgolly.scalajs.react.vdom.all._
import org.scalajs.dom._
import japgolly.scalajs.react.{ ReactComponentB, _ }
import org.w3.banana.plantain.Plantain.ops._
import rww.ui.Util

object PersonBasicInfo {

  def apply(props: PersonProps) = PersonBasicInfo(props)
    
  private val PersonBasicInfo = ReactComponentB[PersonProps]("PersonBasicInfo")
    .initialState(PersonState(None))
    .render((P, C, S) => div(className := "basic") {
      val name = Util.getFirstLiteral(P, FOAF.name, "(name missing)").toString
      val givenname = Util.getFirstLiteral(P, FOAF.givenname, "(givenname missing)").toString
      val company = Util.getFirstUri(P, FOAF.workplaceHomepage, "workplaceHomepage missing")
      
      if (!S.edit) {
          div(className := "name title-case")(name) ::
          div(className := "surname title-case")(givenname) ::
          div(className := "company")(company) ::
          Nil
      } else {
          div(className := "name title-case")(form()(input(tpe := "text", placeholder := "Enter name", value := name))) ::
          div(className := "surname title-case")(form()(input(tpe := "text", placeholder := "Enter givenname", value := givenname))) ::
          div(className := "company")(form()(input(tpe := "text", placeholder := "Enter company website", value := company))) ::
          Nil
      }
    }).build
    
    
    
    

  /*
                var viewTree;
        if (!this.props.modeEdit) {
            viewTree =
            <div className="basic">
                <div className="name title-case" title={info["foaf:name"]}>{info["foaf:name"]}</div>
                <div className="surname title-case" title={info["foaf:givenname"]}>{info["foaf:givenname"]}</div>
                <div className="company"  title={info["foaf:workplaceHomepage"]}>{info["foaf:workplaceHomepage"]}</div>
            </div>
        }
        else {
            viewTree =
            <div className="basic">
                <div className="name title-case">
                    <form onSubmit={this._handleSubmit}>
                        <input type="text" placeholder="Enter name"  valueLink={this.linkToPgLiteral(personPG, 'foaf:name')} />
                    </form>
                </div>
                <div className="surname title-case">
                    <form onSubmit={this._handleSubmit}>
                        <input type="text" placeholder="Enter givenname" valueLink={this.linkToPgLiteral(personPG, 'foaf:givenname')} />
                    </form>
                </div>
                <div className="company">
                    <form onSubmit={this._handleSubmit}>
                        <input type="text" placeholder="Enter company website" valueLink={this.linkToPgLiteral(personPG, 'foaf:workplaceHomepage')} />
                    </form>
                </div>
            </div>
        }
        
        */
}