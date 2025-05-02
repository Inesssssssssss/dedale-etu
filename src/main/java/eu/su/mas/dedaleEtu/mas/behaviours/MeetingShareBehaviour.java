package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
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
	
	public MeetingShareBehaviour (final Agent myagent, MapRepresentation myMap) {
		super(myagent);
		this.myMap=myMap;
	}

	@Override
	public void action() {
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
		}
		
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			for (Couple<Location,List<Couple<Observation,String>>> obs : lobs) {
				for (Couple<Observation,String> type : obs.getRight()) {
					switch(type.getLeft()) {
					case AGENTNAME :
						String agentName = type.getRight();
						System.out.println("Bonjour");
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
					
					default:
						break;
							}
						}
					}
			}
		
		
	}

	@Override
	public boolean done() {
		return finished;
	}

}
