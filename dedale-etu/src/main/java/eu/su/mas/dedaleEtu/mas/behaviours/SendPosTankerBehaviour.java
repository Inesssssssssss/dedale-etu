package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

public class SendPosTankerBehaviour extends SimpleBehaviour {
	
	private boolean finished = false;
	private MapRepresentation myMap;
	private List<String> list_agent = new ArrayList<>();
	
	public SendPosTankerBehaviour(Agent agent, MapRepresentation myMap) {
		super(agent);
		this.myMap = myMap;
	}

	@Override
	public void action() {
		// Reception du ping
		MessageTemplate pingTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol("PING"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        ACLMessage ping = this.myAgent.receive(pingTemplate);
        if (ping != null) {
        	String agentName = ping.getSender().getLocalName();
        	if (!list_agent.contains(agentName)) {
	        	System.out.println("envoi de la position à "+agentName);
	        	ACLMessage share = new ACLMessage(ACLMessage.INFORM);
	            share.setProtocol("TANKER-POS");
	            share.addReceiver(new AID(agentName, AID.ISLOCALNAME));
	            //share.setContent(((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId());
	            SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
	            try {
					share.setContentObject(sg);
					share.setConversationId(((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}         
	            myAgent.send(share);
	            list_agent.add(agentName);
        	}
        }
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}

}
