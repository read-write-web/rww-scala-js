package rww.ui.foaf

import japgolly.scalajs.react.vdom.all._
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import org.scalajs.dom._
import japgolly.scalajs.react.{ ReactComponentB, _ }
import org.w3.banana._
import org.w3.banana.plantain.Plantain.ops._
import org.w3.banana.plantain.Plantain
import rww.ui.Util

case class PersonState(personPG: Option[PointedGraph[Plantain]],
                       edit: Boolean = false,
                       editText: String = "Edit")
                       
class PersonBackend($: BackendScope[PersonProps, PersonState]) {
  
  def handleSubmit(e: ReactEventI) = {
    e.preventDefault()
    println(js.JSON.stringify($._state))
    saveGraph($._state)
  }
  
  def saveGraph(graph: PersonState) =
    ???
    
  def onTextChange(matcher: Plantain#URI)(f: String => Plantain#Node)(e: ReactEventI) = {
    $.modState(s => {
      val newPg = Util.getModifiedCopy(s.personPG, matcher, f(e.target.value))
      PersonState(Option(newPg).get, s.edit, s.editText) // TODO: Consider edge case where newPG is None
    })
    val a = Util.getFirstLiteral($.state.personPG.get, FOAF.name, "").toString
    println(js.JSON.stringify(a))
  }
  
  def onClickEditSave() = {
    println(js.JSON.stringify($._state))
    $.modState(s => PersonState(Option($._props), !s.edit, if (!s.edit) "Save" else "Edit"))
  }
}

object Person {

  def apply(props: PersonProps) = Person(props)

  private val Person = ReactComponentB[PersonProps]("Person")
    .initialState(PersonState(None))
    .backend(new PersonBackend(_))
    .render((P: PersonProps, S: PersonState, B: PersonBackend) => div(className := "clearfix center")(
      div(className := "edit-profile", onClick --> B.onClickEditSave) {
        S.editText
      },
      Pix(PixProps(Util.getFirstUri(P, FOAF.depiction, "static/avatar-man.png"))),
      PersonBasicInfo(P, S, B)))
    .build
    
  

  /*
   Structure from react-foaf (https://github.com/read-write-web/react-foaf)
   
                  if (!this.state.modeEdit) {
                    var content = <Person
                    personPG={currentTab.personPG}
                    currentUserPG={this.state.personPG}
                    modeEdit={this.state.modeEdit}
                    submitEdition={this._submitEdition}
                    onContactSelected={this._loadOrMaximizeUserProfileFromUrl}
                    onAddContact={this._addContact}
                    onRemoveContact={this._removeContact}
                    handleClickChangeModeEdit={this._handleClickChangeModeEdit}/>
                } else {
                    var content = <Person
                    currentUserPG={this.state.personPG}
                    personPG={this.state.personPGDeepCopy}
                    modeEdit={this.state.modeEdit}
                    submitEdition={this._submitEdition}
                    onContactSelected={this._loadOrMaximizeUserProfileFromUrl}
                    handleClickChangeModeEdit={this._handleClickChangeModeEdit}/>
                }
  #############Person (top entity) ############
   <div id="profile" className="clearfix center">
                            <div className="edit-profile" onClick={this._handleClickEditButton}>{this.state.editText}</div>
                            <Pix src={this._getUserImg()}/>
                            <PersonBasicInfo
                                personPG={this.props.personPG}
                                modeEdit={this.props.modeEdit}
                                submitEdition={this._submitEdition}/>
                            <PersonNotifications personPG={this.props.personPG}/>
                            <PersonMessage personPG={this.props.personPG} />
                            <PersonMoreInfo
                                personPG={this.props.personPG}
                                modeEdit={this.props.modeEdit}
                                submitEdition={this._submitEdition}/>
                            <PersonWebId personPG={this.props.personPG}/>
                            <PersonContacts
                                personPG={this.props.personPG}
                                currentUserPG={this.props.currentUserPG}
                                onContactSelected={this.props.onContactSelected}
                                onAddContact={this.props.onAddContact}
                                onRemoveContact={this.props.onRemoveContact}
                            />
                        </div>
                        
    ##########Pix############################
                <div className="picture">
                    {this.transferPropsTo(<img/>)}
                </div>
    

    ###########PersonBasicInfo#####################
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
    
    ##########PersonNotifications###################
    <div className="notifications">
                <div className="newMessages float-left">{notifications.nbNewMessages}</div>
                <div className="recentInteractions float-left">{notifications.nbRecentInteraction}</div>
                <div className="updates float-left">{notifications.nbUpdates}</div>
            </div>
    
    ############PersonMessage#########################
            <div className="moreInfo">
                <div className="lastInteraction">Last message from {this._getUsername()}: <span>{message.lastMessageDate}</span></div>
                <div className="message">{message.lastMessage}</div>
                <div className="nextStep"><a href="#">Write back</a></div>
            </div>
    
    ############PersonMoreInfo########################
   if (!this.props.modeEdit) {
            viewTree =
                <div id="details">
                    <div className="title center-text title-case">DETAILS</div>
                    <ul className="clearfix span3">
                        <li className="float-left">
                            <div className="email">
                                <div className="title-case">Email</div>
                                <div className="content email-content">{moreInfo["foaf:mbox"]}</div>
                            </div>
                            <div className="phone">
                                <div className="title-case">Phone</div>
                                <div className="content email-content">{moreInfo["foaf:phone"]}</div>
                            </div>
                        </li>
                        <li className="float-left">
                            <PersonAddress
                            modeEdit={this.props.modeEdit}
                            personPG={this.props.personPG}/>
                        </li>
                        <li className="float-left">
                            <div className="website">
                                <div className="title-case">Website</div>
                                <div className="content website-content">
                                    <a href="https://stample.co" target="_blank">{moreInfo["foaf:homepage"]}</a>
                                </div>
                            </div>
                        </li>
                    </ul>
                </div>
        }
        else {
            viewTree =
                <div id="details">
                    <div className="title center-text title-case">DETAILS</div>

                    <ul className="clearfix span3">
                        <li className="float-left">
                            <div className="email">
                                <div className="title-case">Email</div>
                                <div className="content email-content">
                                    <form onSubmit={this._handleSubmit}>
                                        <input type="text"
                                        placeholder="Enter email"
                                        valueLink={this.linkToPgLiteral(personPG, 'foaf:mbox')} />
                                    </form>
                                </div>
                            </div>
                            <div className="phone">
                                <div className="title-case">Phone</div>
                                <div className="content email-content">
                                    <form onSubmit={this._handleSubmit}>
                                        <input type="text"
                                        placeholder="Enter phone"
                                        valueLink={this.linkToPgLiteral(personPG, 'foaf:phone')} />
                                    </form>
                                </div>
                            </div>
                        </li>
                        <li className="float-left">
                            <PersonAddress
                            modeEdit={this.props.modeEdit}
                            personPG={this.props.personPG}
                            submitEdition={this.props.submitEdition}/>
                        </li>
                        <li className="float-left">
                            <div className="website">
                                <div className="title-case">Website</div>
                                <div className="content website-content">
                                    <form onSubmit={this._handleSubmit}>
                                        <input type="text"
                                        placeholder="Enter homepage"
                                        valueLink={this.linkToPgLiteral(personPG, 'foaf:homepage')} />
                                    </form>
                                </div>
                            </div>
                        </li>
                    </ul>
                </div>
        }
   
    ############PersonWebId###########################
           <div id="webid" className="clearfix">
                <a href={webId.webId}>
                    <Pix src={appImages.webIdIcon} alt="Web ID logo" className="float-left"/>
                </a>
                <div id="webid-address" class="float-left"><span className="title-case">Web ID </span> {webId.webId}</div>
            </div>
    
    ############PersonContacts######################## 
    should look into later

   */
}