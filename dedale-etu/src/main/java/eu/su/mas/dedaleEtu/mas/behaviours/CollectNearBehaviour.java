package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

public class CollectNearBehaviour extends SimpleBehaviour{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2937149605266647143L;
	private boolean finished = false;
	private List<Couple<String,Couple<Observation,String>>> list_tre_type=new ArrayList<>();   //liste ou se trouve les tresors
	private String pos_tanker;
	private MapRepresentation myMap;
	private boolean drop = false;
	
	private String pre_node;
	
	private Set<String> blockedNodes = new HashSet<>();
	
	private Integer cptBlock = 0;
	private Integer cptRedo = 0;
	
	public CollectNearBehaviour(final AbstractDedaleAgent myagent,List<Couple<String,Couple<Observation,String>>> list_tre,MapRepresentation myMap, String pos_tanker) {
		super(myagent);
		//this.list_tre = list_tre;
		this.pos_tanker = pos_tanker;
		this.myMap = myMap;
		if (list_tre!=null) {
			for (Couple<String,Couple<Observation,String>> c : list_tre) {
				if (c.getRight().getLeft()==((AbstractDedaleAgent)myAgent).getMyTreasureType()) {
					this.list_tre_type.add(c);
				}
			}
		}
		this.blockedNodes.add(pos_tanker);
	}

	@Override
	public void action() {
		
		try {
			this.myAgent.doWait(400);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (this.cptRedo>10) {
			this.blockedNodes = new HashSet<>();
			if(this.pos_tanker!=null) {
				this.blockedNodes.add(this.pos_tanker);
			}
			this.cptRedo = 0;
		}

		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		Observation agent_type = ((AbstractDedaleAgent) this.myAgent).getMyTreasureType();

		
		//List of observable from the agent's current position
		List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		//System.out.println("observation collect : "+lobs);
		// retirer tresor deja ramassé
		for (int i = 0; i < list_tre_type.size(); i++) {
			if (list_tre_type.get(i).getLeft().equals(lobs.get(0).getLeft().getLocationId())) {
				if(lobs.get(0).getRight().get(0).getLeft()!=Observation.GOLD && lobs.get(0).getRight().get(0).getLeft()!=Observation.DIAMOND) {
					list_tre_type.remove(i);
				}
			}
		}
		
		for (Couple<Location,List<Couple<Observation,String>>> c : lobs) {
			for(Couple<Observation,String> cc : c.getRight()) {
				if (cc.getLeft()==Observation.AGENTNAME) {
					String agentName = cc.getRight();
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
				}
			}
		}
		
		

		
		try {
			this.myAgent.doWait(50);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String nextNodeId=null;
		
		// Si mon sac est plein je vais au silo
		for ( Couple<Observation,Integer> o : ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace()) {
			if (o.getLeft() == agent_type) {
				if (o.getRight() <= 0) {
					List<String> path = this.myMap.getShortestPath(myPosition.getLocationId(),this.pos_tanker);
					nextNodeId = path.get(0);
					System.out.println("Mon sac est rempli, position du tanker : "+this.pos_tanker+" nextNodeId : "+nextNodeId);
				}
			}
		}
		
		
		if (this.list_tre_type.size()<=0) {
			if (!drop) {
				//System.out.println("pos tanker : "+this.pos_tanker);
				List<String> path = this.myMap.getShortestPath(myPosition.getLocationId(),this.pos_tanker);
				nextNodeId = path.get(0);
				if (nextNodeId.equals(this.pos_tanker)) {
					drop = true;
				}
			}
			else {
				System.out.println("Tous les trésors ont été collectés");
				
				finished = true;
				return;
			}
		}
		List<String> positions = new ArrayList<>();
		//System.out.println("liste tresor : "+ this.list_tre_type);
		for (Couple<String,Couple<Observation,String>> c : this.list_tre_type) {
			positions.add(c.getLeft());
		}
		if (nextNodeId == null) {
			//System.out.println("position de l'agent : "+myPosition.getLocationId()+" positions : "+ positions);
			List<String> path = this.myMap.getShortestPathToClosestTreasure(myPosition.getLocationId(), positions);
			//System.out.println("path : "+path);
			/*if (path.contains(this.pos_tanker)) {
				path = this.myMap.getAlternativePathTreasure(myPosition.getLocationId(), this.pos_tanker, positions);
			}*/
			if (path!=null && !path.isEmpty()) {
				nextNodeId = path.get(0);
			}
			for (String block : blockedNodes) {
				if (path.contains(block)) {
				//if (pre_node == myPosition.getLocationId()) {
					//problem_node = nextNodeId;
					System.out.println(this.myAgent.getName()+" est bloqué, noeud bloqué : "+block+" pos tanker : "+this.pos_tanker);
				    
				    List<String> alternativePath=null;
					alternativePath = this.myMap.getAlternativePathTreasure(myPosition.getLocationId(), block, positions);
				    
				    if (alternativePath != null && !alternativePath.isEmpty()) {
				        nextNodeId = alternativePath.get(0);
				    }
				 }
				
			}
			
		}
		if (this.pre_node == nextNodeId) {
        	this.cptBlock++;
        	if(cptBlock>2) {
        		this.blockedNodes.add(nextNodeId);
        		this.cptBlock = 0;
        	}
        }
		if (nextNodeId != null) {
			((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId));
		}
		
		this.pre_node = nextNodeId;
		this.cptRedo++;
		
	}
	
	public void set_list_tre(List<Couple<String,Couple<Observation,String>>> list_tre) {
		this.list_tre_type = new ArrayList<>();
		for (Couple<String,Couple<Observation,String>> c : list_tre) {
			if (c.getRight().getLeft()==((AbstractDedaleAgent)myAgent).getMyTreasureType()) {
				this.list_tre_type.add(c);
			}
		}
	}
	
	public void set_pos_tanker(String pos_tank) {
		this.pos_tanker=pos_tank;
	}
	
	public String get_pos_tanker() {
		return this.pos_tanker;
	}
	
	public void set_map(MapRepresentation myMap) {
		this.myMap = myMap;
	}
	
	public List<String> is_tanker(String pos,List<String> path){
		List<String> new_path=new ArrayList<>();
		if (path.contains(this.pos_tanker)) {
			new_path = this.myMap.getAlternativePath(pos, this.pos_tanker);
		}
		return new_path;
	}


	@Override
	public boolean done() {
		return finished;
	}

}
