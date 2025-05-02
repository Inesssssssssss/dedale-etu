package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class MeetingExploreBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	private MeetingShareBehaviour MSB;
	private MapRepresentation sharedMap;


/**
 * 
 * @param myagent reference to the agent we are adding this behaviour to
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public MeetingExploreBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap) {
		super(myagent);
		this.myMap=myMap;
		this.sharedMap=null;
		
		
	}

	@Override
	public void action() {
		//this.sharedMap = new MapRepresentation();
		//sharedMap ne fonctionne pas -> ouvre une nouvelle map qui n'a pas de liens entre les noeuds
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
			this.sharedMap = new MapRepresentation();
			this.MSB = new MeetingShareBehaviour(this.myAgent,this.sharedMap);
			this.myAgent.addBehaviour(MSB);
			
		}

		//0) Retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(100);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);
			this.sharedMap.addNode(myPosition.getLocationId(), MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
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

			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode()){
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
									this.myAgent.removeBehaviour(this.MSB);
									finished=true;
								default:
									break;
								}
							}
						}
					}
				}
				//Explo finished
				System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNodeId==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					nextNodeId=this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					//System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode);
				}else {
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				
				//5) At each time step, the agent check if he received a graph from a teammate. 	
				// If it was written properly, this sharing action should be in a dedicated behaviour set.
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
		return finished;
	}

}
