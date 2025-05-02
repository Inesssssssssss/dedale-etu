package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class MeetingBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = -2058134672078521998L;
	private boolean finished = false;
	private MapRepresentation myMap;
	
	public MeetingBehaviour (final Agent myagent, MapRepresentation myMap) {
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
				try {
					this.myAgent.doWait(500);
				} catch (Exception e) {
					e.printStackTrace();
				}
			
			this.myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);
			String nextNodeId=null;
			Iterator<Couple<Location, List<Couple<Observation, String>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				Location accessibleNode=iter.next().getLeft();
				boolean isNewNode=this.myMap.addNewNode(accessibleNode.getLocationId());
				//the node may exist, but not necessarily the edge
				if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
					this.myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
					if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
				}
			}
			if (!this.myMap.hasOpenNode()){
				//Explo finished
				while(!finished) {
					Location lastPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

					if (lastPosition!=null){
						//List of observable from the agent's current position
						List<Couple<Location,List<Couple<Observation,String>>>> lastlobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
						for (Couple<Location,List<Couple<Observation,String>>> lastobs : lastlobs) {
							for (Couple<Observation,String> lasttype : lastobs.getRight()) {
								switch(lasttype.getLeft()) {
								case AGENTNAME :
									String agentName = lasttype.getRight();
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
									finished=true;
								default:
									break;
								}
							}
						}
					}
				}
				System.out.println(this.myAgent.getLocalName()+" - Exploration successfully done, behaviour removed.");
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNodeId==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					nextNodeId=this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					//System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode);
				}
				MessageTemplate msgTemplate=MessageTemplate.and(
						MessageTemplate.MatchProtocol("SHARE-TOPO"),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
				if (msgReceived!=null) {
					SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
					try {
						sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					this.myMap.mergeMap(sgreceived);
				}
				((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId));
		}
	}
}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}

}
