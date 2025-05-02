package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;

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
        	System.out.println("envoi de la position à "+ping.getSender().getName());
        	ACLMessage share = new ACLMessage(ACLMessage.INFORM);
            share.setProtocol("TANKER-POS");
            share.addReceiver(new AID(ping.getSender().getLocalName(), AID.ISLOCALNAME));
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
        }
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}

}
