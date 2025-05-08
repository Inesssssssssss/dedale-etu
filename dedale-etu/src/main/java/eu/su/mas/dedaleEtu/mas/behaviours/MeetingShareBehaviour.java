package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class MeetingShareBehaviour extends SimpleBehaviour{
	
	private static final long serialVersionUID = -2058134672078521998L;
	private boolean finished = false;
	private MapRepresentation myMap;
	private List<String> agentNames;
	private List<String> agentSentTo;
	private List<Couple<String,Couple<Observation,String>>> list_tre;
	private String tankerPos;
	private String tankerName;
	
	public MeetingShareBehaviour (final Agent myagent, MapRepresentation myMap, List<String> agentNames,List<Couple<String,Couple<Observation,String>>> list_tre, String tankerPos, String tankerName) {
		super(myagent);
		this.myMap=myMap;
		this.agentNames = agentNames;
		this.agentSentTo = new ArrayList<String>();
		this.list_tre = list_tre;
		this.tankerPos = tankerPos;
		this.tankerName = tankerName;
		}

	@Override
	public void action() {
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
		}
		
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			
			ACLMessage ping = new ACLMessage(ACLMessage.INFORM);
			ping.setProtocol("PING");
			ping.setSender(this.myAgent.getAID());
			
			for (String agentName : this.agentNames){
				ping.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			}
			((AbstractDedaleAgent)this.myAgent).sendMessage(ping);
			
			MessageTemplate msgTemplate=MessageTemplate.and(
					MessageTemplate.MatchProtocol("PING"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
			if (msgReceived!=null) {
				
				String agentName = msgReceived.getSender().getLocalName();
				//System.out.println(this.myAgent.getLocalName()+" recu ping de "+agentName);
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SHARE-TOPO");
				msg.setSender(this.myAgent.getAID());
				msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
					
				SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
				try {					
					msg.setContentObject(sg);
				} catch (IOException e) {
					e.printStackTrace();
				}
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				if (!this.agentSentTo.contains(agentName)) {
					this.agentSentTo.add(agentName);
				}
				if (this.agentSentTo.size() >= (int)(this.agentNames.size() / 2)) {
					this.myMap.resetGraph();
				}
				
				ACLMessage msg_tre = new ACLMessage(ACLMessage.INFORM);
				msg_tre.setProtocol("SHARE-TREASURE");
				msg_tre.setSender(this.myAgent.getAID());
				msg_tre.addReceiver(new AID(agentName,AID.ISLOCALNAME));
				
				ArrayList<Object> msgList = new ArrayList<>();
				msgList.add(this.list_tre);
				msgList.add(this.tankerPos);
				msgList.add(this.tankerName);
				
				try {
					//msg_tre.setContentObject((Serializable) this.list_tre);
					msg_tre.setContentObject(msgList);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg_tre);
				//System.out.println("tresor envoye");
				}
			}
	}
	
	public void setTankerPos(String tanker_pos) {
		this.tankerPos=tanker_pos;
	}
	
	public void setTankerName(String tanker_name) {
		this.tankerName=tanker_name;
	}

	@Override
	public boolean done() {
		return finished;
	}

}