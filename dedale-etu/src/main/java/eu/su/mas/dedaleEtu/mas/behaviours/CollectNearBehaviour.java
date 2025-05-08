package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.SimpleBehaviour;

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
	}

	@Override
	public void action() {

		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		Observation agent_type = ((AbstractDedaleAgent) this.myAgent).getMyTreasureType();

		
		//List of observable from the agent's current position
		List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		System.out.println("observation collect : "+lobs);
		// retirer tresor deja ramassé
		for (int i = 0; i < list_tre_type.size(); i++) {
			if (list_tre_type.get(i).getLeft().equals(lobs.get(0).getLeft().getLocationId())) {
				if(lobs.get(0).getRight().get(0).getLeft()!=Observation.GOLD && lobs.get(0).getRight().get(0).getLeft()!=Observation.DIAMOND) {
					list_tre_type.remove(i);
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
					System.out.println("Mon sac est rempli, position du tanker : "+this.pos_tanker);
					List<String> path = this.myMap.getShortestPath(myPosition.getLocationId(),this.pos_tanker);
					nextNodeId = path.get(0);
				}
			}
		}
		
		
		if (this.list_tre_type.size()<=0) {
			if (!drop) {
				System.out.println("pos tanker : "+this.pos_tanker);
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
		System.out.println("liste tresor : "+ this.list_tre_type);
		for (Couple<String,Couple<Observation,String>> c : this.list_tre_type) {
			positions.add(c.getLeft());
		}
		if (nextNodeId == null) {
			System.out.println("position de l'agent : "+myPosition.getLocationId()+" positions : "+ positions);
			List<String> path = this.myMap.getShortestPathToClosestTreasure(myPosition.getLocationId(), positions);
			System.out.println("path : "+path);
			if (path.contains(this.pos_tanker)) {
				path = this.myMap.getAlternativePathTreasure(myPosition.getLocationId(), this.pos_tanker, positions);
			}
			if (path!=null && !path.isEmpty()) {
				nextNodeId = path.get(0);
			}
		}
		if (nextNodeId != null) {
			((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId));
		}
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
