import jade.content.lang.Codec;


import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREResponder;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import ontology.messages.Agent;
import ontology.messages.Results;
import ontology.webservices.GetAgents;
import ontology.webservices.GetResults;
import ontology.webservices.GetOptions;
import ontology.webservices.SetProblem;
import ontology.webservices.WS_Ontology;


public class Agent_GUI_WS extends Agent_GUI {

	private static final long serialVersionUID = -5322630455326259706L;
	private jade.util.leap.LinkedList results = new jade.util.leap.LinkedList();
	private Codec codec;
	private Ontology wsOntology;
	private Ontology messagesOntology;

	@Override
	protected void allOptionsReceived() {
		sendProblem();
	}

	@Override
	protected void displayOptions(Agent agent) {
		refreshOptions(agent);
	}

	@Override
	protected void displayResult(ACLMessage inform) {
		System.out.println("Displaying the result ;)");
	}

	@Override
	protected String getAgentType() {
		return "WS GUI Agent";
	}

	@Override
	protected void mySetup() {
		
		wsOntology = ontology.webservices.WS_Ontology.getInstance();
		messagesOntology = ontology.messages.MessagesOntology.getInstance();
		codec = new SLCodec();
		getContentManager().registerOntology(wsOntology);
		getContentManager().registerOntology(messagesOntology);
		
		DFAgentDescription df = new DFAgentDescription();
		df.setName(this.getAID());
		
		//TODO: fix this
		try {
			df = DFService.search(this, df)[0];
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		ServiceDescription sd = new ServiceDescription();
		sd.addOntologies(ontology.webservices.WS_Ontology.ONTOLOGY_NAME);
		sd.addLanguages(codec.getName());
		sd.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
		sd.addProperties(new Property("wsig", "true"));
		sd.setType("WS_GUI_Agent");
		sd.setName("WS_GUI");
		
		df.addOntologies(wsOntology.getName());
		df.addServices(sd);
		
		try {
			DFService.modify(this, df);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		MessageTemplate mt = MessageTemplate.MatchOntology(WS_Ontology.ONTOLOGY_NAME);
		
		addBehaviour(new AchieveREResponder(this, mt) {
			
			@Override
			protected ACLMessage prepareResponse(ACLMessage request)
					throws NotUnderstoodException, RefuseException {
				return null;
			}
			
			@Override
			protected ACLMessage handleRequest(ACLMessage request) {
				
				try {
					System.err.print("Agent " + myAgent.getName() + ":");
					
					Action a = (Action)getContentManager().extractContent(request);
					
					if ( a.getAction() instanceof SetProblem) {
						SetProblem sp = (SetProblem)(a.getAction());
						
						jade.util.leap.Iterator it = sp.getFileNames().iterator();
						
						while (it.hasNext()) {
							String s = (String)it.next();
							addFileToProblem(s);
						}
						
						it = sp.getAgentDescriptions().iterator();
						
						while (it.hasNext()) {
							String[] params =  ((String)it.next()).split("[ ]+");
							addAgentToProblem(params);
							getAgentOptions(params[0]);
						}
						
						ACLMessage response = request.createReply();
						response.setPerformative(ACLMessage.INFORM);
						response.setContent("OK");
						
						return response;
						
					}
					else if (a.getAction() instanceof GetAgents) {
						
						String[] agents = getComputingAgents();
						
						jade.util.leap.ArrayList agentsList = new ArrayList();
						
						for (String s: agents) {
							agentsList.add(s);
						}
						
						ACLMessage response = request.createReply();
						response.setPerformative(ACLMessage.INFORM);
						Result r = new Result(a.getAction(), agentsList);
						
						getContentManager().fillContent(response, r);
						
						return response;
					}
					else if (a.getAction() instanceof GetResults) {
						
						ACLMessage response = request.createReply();
						response.setPerformative(ACLMessage.INFORM);
						Result r = new Result(a.getAction(), results);
						
						getContentManager().fillContent(response, r);
						
						return response;
					}
					else if (a.getAction() instanceof GetOptions) {
						
						ACLMessage response = request.createReply();
						
						GetOptions go = (GetOptions)a.getAction();
						String agentName = go.getAgentName();
						
						Agent ag = onlyGetAgentOptions(agentName);
						
						if (ag == null) {
							response.setPerformative(ACLMessage.FAILURE);
							return response;
						}
						
						List mOptions = ag.getOptions();
						ArrayList options = new ArrayList();
						
						Iterator it = mOptions.iterator();
						
						while (it.hasNext()) {
							ontology.messages.Option o = (ontology.messages.Option)it.next();
							String name = o.getName();
							String synopsis = o.getSynopsis();
							String description = o.getDescription();
							String value = o.getValue();
							
							options.add(new ontology.webservices.Option(
									name, synopsis, description, value));
						}
						
						response.setPerformative(ACLMessage.INFORM);
						Result r = new Result(a.getAction(), options);
						
						getContentManager().fillContent(response, r);
						
						return response;
						
					}
					
				} catch (UngroundedException e) {
					e.printStackTrace();
				} catch (CodecException e) {
					e.printStackTrace();
				} catch (OntologyException e) {
					e.printStackTrace();
				}
				
				return null;
			}
		});
		
	}

	private Agent onlyGetAgentOptions(String agent) {
		
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(new AID(agent, AID.ISLOCALNAME));
		
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		
		request.setLanguage(codec.getName());
		request.setOntology(messagesOntology.getName());
		
		ontology.messages.GetOptions get = new ontology.messages.GetOptions();
		Action a = new Action();
		a.setAction(get);
		a.setActor(this.getAID());
		
		try {
			// Let JADE convert from Java objects to string
			getContentManager().fillContent(request, a);
			
			ACLMessage inform = FIPAService.doFipaRequestClient(this, request);
			
			if (inform == null) {
				return null;
			}
			
			Result r = (Result)getContentManager().extractContent(inform);
			
			return (Agent)r.getItems().get(0);
			
		}
		catch (CodecException ce) {
			ce.printStackTrace();
		}
		catch (OntologyException oe) {
			oe.printStackTrace();
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		return null;
		
	}
	
	
	@Override
	protected void displayPartialResult(ACLMessage inform) {
		try {
			Results r = (Results)getContentManager().extractContent(inform);
			results.add(r);
		} catch (UngroundedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CodecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
