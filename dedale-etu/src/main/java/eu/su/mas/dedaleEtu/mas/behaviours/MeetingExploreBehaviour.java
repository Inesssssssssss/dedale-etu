package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private CollectBehaviour CB;
	private MapRepresentation sharedMap;
	private SendMapBehaviour SMB;
	
	private String pre_node;
	private String problem_node=null;
	private String pos_tanker=null;
	private List<Couple<String,Couple<Observation,String>>> list_tre = new ArrayList<>();   //liste ou se trouve les tresors
	
	private List<String> list_agentNames;
	
	// Ajout d'une file pour stocker les 3 derniers déplacements
	private Queue<String> lastPositions = new LinkedList<>();
	private Set<String> blockedNodes = new HashSet<>(); // Liste des nœuds à éviter temporairement

	


/**
 * 
 * @param myagent reference to the agent we are adding this behaviour to
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public MeetingExploreBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames) {
		super(myagent);
		this.myMap=myMap;
		this.sharedMap=null;
		this.list_agentNames=agentNames;
	}

	@Override
	public void action() {
		//this.sharedMap = new MapRepresentation();
		//sharedMap ne fonctionne pas -> ouvre une nouvelle map qui n'a pas de liens entre les noeuds
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
			this.sharedMap = new MapRepresentation();
			this.MSB = new MeetingShareBehaviour(this.myAgent,this.sharedMap, this.list_agentNames, this.list_tre);
			this.CB = new CollectBehaviour(this.myAgent,this.sharedMap, this.list_agentNames);
			//this.SMB = new SendMapBehaviour(this.myAgent,this.sharedMap, this.list_agentNames);
			this.myAgent.addBehaviour(CB);
			
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
			//System.out.println("iterator : "+lobs);
			while(iter.hasNext()){
				Couple<Location, List<Couple<Observation, String>>> nodeData = iter.next();
			    
			    Location accessibleNode = nodeData.getLeft();
			    List<Couple<Observation,String>> lObservations = nodeData.getRight();
			    
			    String posTre = accessibleNode.getLocationId();
				if ( lObservations != null && lObservations.size() > 0 ) {
					Couple<Observation,String> treasure = lObservations.get(0);
					if ((treasure.getLeft() == Observation.DIAMOND || treasure.getLeft() == Observation.GOLD) && !list_tre.contains(new Couple<>(posTre, treasure))){
						list_tre.add(new Couple<>(posTre, treasure));
					}
				}
				boolean isNewNode=this.myMap.addNewNode(accessibleNode.getLocationId());
				boolean isNewNodeShared=this.sharedMap.addNewNode(accessibleNode.getLocationId());
				//the node may exist, but not necessarily the edge
				if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
					this.myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
					this.sharedMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
					if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
				}
				
			}

			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode()){
				//Explo finished
				finished = true;
				System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNodeId==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					List<String> path=this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId());//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					//System.out.println("chemin : "+this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()));
					
					if (path == null || path.isEmpty()) {
	                    System.out.println("Aucun chemin trouvé vers un nœud ouvert");
	                    finished = true;
	                }
					
					nextNodeId = path.get(0);
					if (pre_node == myPosition.getLocationId() && !finished) {
						blockedNodes.add(nextNodeId);
					}
					for (String block : blockedNodes) {
						if (path.contains(block)) {
						//if (pre_node == myPosition.getLocationId()) {
							//problem_node = nextNodeId;
							System.out.println(this.myAgent.getName()+" est bloqué");
						    
						    List<String> alternativePath = this.myMap.getAlternativePath(myPosition.getLocationId(), block);
						    
						    if (alternativePath != null && !alternativePath.isEmpty()) {
						        nextNodeId = alternativePath.get(0);
						    }else {
	                            System.out.println("L'agent a fini d'explorer les noeuds accessibles");
	                            finished = true;
		                     }
						    }
							/*
							String pre_nextNode = nextNodeId;
							int i = 0;
							while(pre_nextNode==nextNodeId) {
								System.out.println("meme node : "+ this.myMap.getShortestPathToSecClosestOpenNode(myPosition.getLocationId(),i));
								nextNodeId=this.myMap.getShortestPathToSecClosestOpenNode(myPosition.getLocationId(),i).get(0);
								i++;
							}*/
						
					}
					//System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode);
				}else {
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				
				//5) At each time step, the agent check if he received a graph from a teammate. 	
				// If it was written properly, this sharing action should be in a dedicated behaviour set.
				// on regarde si on a recu un ping
				/*
				MessageTemplate msgTemplate=MessageTemplate.and(
						MessageTemplate.MatchProtocol("PING"),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
				if (msgReceived!=null) {
					
					ACLMessage pong = new ACLMessage(ACLMessage.INFORM);
					pong.setProtocol("PONG");
					pong.setSender(this.myAgent.getAID());
					pong.addReceiver(new AID(msgReceived.getSender().getName(),AID.ISLOCALNAME));
						
					((AbstractDedaleAgent)this.myAgent).sendMessage(pong);*/
					
					MessageTemplate msgG=MessageTemplate.and(
							MessageTemplate.MatchProtocol("SHARE-TOPO"),
							MessageTemplate.MatchPerformative(ACLMessage.INFORM));
					
					//System.out.println("J'attend la map");
					ACLMessage msgReceivedG=this.myAgent.blockingReceive(msgG,50);
					
					if (msgReceivedG != null) {
						//System.out.println("J'ai recu la map");
						SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
						try {
							sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceivedG.getContentObject();
						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						this.myMap.mergeMap(sgreceived);
						//this.sharedMap.resetGraph();
					}
					
					MessageTemplate msgTre=MessageTemplate.and(
							MessageTemplate.MatchProtocol("SHARE-TREASURE"),
							MessageTemplate.MatchPerformative(ACLMessage.INFORM));
					
					//System.out.println("J'attend la map");
					ACLMessage msgReceivedTre=this.myAgent.blockingReceive(msgTre,200);
					if (msgReceivedG != null) {
						List<Couple<String,Couple<Observation,String>>> treasure_received=null;
						try {
							treasure_received = (List<Couple<String,Couple<Observation,String>>>)msgReceivedTre.getContentObject();
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
						System.out.println("Tresor avant partage : "+this.list_tre);
						System.out.println("Tresor partagé : "+treasure_received);
						this.myAgent.doWait(500);
						
						this.list_tre = Stream.concat(list_tre.stream(),treasure_received.stream()).collect(Collectors.toList());
						this.list_tre = this.list_tre.stream().distinct().collect(Collectors.toList());
						System.out.println("Tresor apres partage : "+this.list_tre);
					
					}
					
				//}
					MessageTemplate posTemplate = MessageTemplate.and(
			                MessageTemplate.MatchProtocol("TANKER-POS"),
			                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
			        );
					
					ACLMessage pos_tank = this.myAgent.blockingReceive(posTemplate,50);
			        if (pos_tank != null) {
			        	try {
							System.out.println("map recu : "+pos_tank.getContentObject()+ " pos : "+ pos_tank.getConversationId());
							SerializableSimpleGraph<String, MapAttribute> sgreceived = (SerializableSimpleGraph<String, MapAttribute>)pos_tank.getContentObject();
							this.myMap.mergeMap(sgreceived);
			        	} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        	this.pos_tanker = pos_tank.getConversationId();
			        	
			        }
				
			
					//System.out.println("Prochain noeud : "+ nextNodeId);
					((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId));
					lastPositions.add(nextNodeId);
	                if (lastPositions.size() > 3) lastPositions.poll();

		
				pre_node = myPosition.getLocationId();
			}

		}
	}

	@Override
	public boolean done() {
		if (finished) {
			System.out.println("liste final"+list_tre);
		}
		return finished;
	}

}