package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.SimpleBehaviour;

public class ExplorerBehaviour extends SimpleBehaviour{
	
	private MapRepresentation myMap;
	private List<String> agentNames;
	private List<Couple<String,Couple<Observation,String>>> list_tre=new ArrayList<>(); 
	private String nextTre;
	private boolean finished=false;
	
	private String pre_node;
	
	private Set<String> blockedNodes = new HashSet<>();
	
	private Integer cptBlock = 0;
	private Integer cptRedo = 0;
	
	public ExplorerBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames, List<Couple<String,Couple<Observation,String>>> list_tre) {
		super(myagent);
		this.myMap = myMap;
		this.agentNames = agentNames;
		this.list_tre = list_tre;
		this.nextTre = list_tre.get(0).getLeft();
		
		//this.MEB = new MeetingExploreBehaviour((AbstractDedaleAgent) this.myAgent, this.myMap, this.agentNames);
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
			this.cptRedo = 0;
		}
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		System.out.println("Explorer - "+this.nextTre);
		
		List<String> path = this.myMap.getShortestPath(myPosition.getLocationId(),this.nextTre);
		String nextNodeId = path.get(0);
		
		List<String> positions = new ArrayList<>();
		//System.out.println("liste tresor : "+ this.list_tre_type);
		for (Couple<String,Couple<Observation,String>> c : this.list_tre) {
			positions.add(c.getLeft());
		}
		
		// Si on a tteint le noeud on voit si on essaye d'aider à ouvrir puis on continue
		if (myPosition.getLocationId()==this.nextTre||nextNodeId==this.nextTre) {
			List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
			Couple<Location,List<Couple<Observation,String>>> lObservations= lobs.get(0);
			for (Couple<Observation,String> c : lObservations.getRight()) {
				if(c.getLeft()==Observation.GOLD) {
					if(((AbstractDedaleAgent) this.myAgent).openLock(Observation.GOLD)||(lObservations.getRight().get(0).getLeft()!=Observation.GOLD)) {
						for(Couple<String,Couple<Observation,String>> locList: this.list_tre) {
							if(locList.getLeft().equals(lObservations.getLeft().getLocationId())) {
								this.list_tre.remove(locList);
							}
						}
					}
				}
				if(c.getLeft()==Observation.DIAMOND) {
					if(((AbstractDedaleAgent) this.myAgent).openLock(Observation.DIAMOND)||(lObservations.getRight().get(0).getLeft()!=Observation.DIAMOND)) {
						for(Couple<String,Couple<Observation,String>> locList: this.list_tre) {
							if(locList.getLeft().equals(lObservations.getLeft().getLocationId())) {
								this.list_tre.remove(locList);
							}
						}
					}
				}
			}
		
			if(this.list_tre.size()<=0) {
				this.finished=true;
				return;
			}else {
				int i = 0;
				String newTre = this.list_tre.get(i).getLeft();
				if(this.nextTre==newTre) {
					i++;
				}
				this.nextTre = this.list_tre.get(i).getLeft();
			}
		}
			path = this.myMap.getShortestPath(myPosition.getLocationId(),this.nextTre);
			if (path!=null && !path.isEmpty()) {
				nextNodeId = path.get(0);
			}
			for (String block : blockedNodes) {
				if (path.contains(block)) {
				    
				    List<String> alternativePath=null;
					alternativePath = this.myMap.getAlternativeShortPath(myPosition.getLocationId(), block, this.nextTre);
				    
				    if (alternativePath != null && !alternativePath.isEmpty()) {
				        nextNodeId = alternativePath.get(0);
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
			System.out.println("Explorer - nextnode"+nextNodeId+" path : "+path);
			if (nextNodeId != null) {
				System.out.println("Explore - moving to "+nextNodeId);
				((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId));
			}
			
			this.pre_node = nextNodeId;
			this.cptRedo++;
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}

}
