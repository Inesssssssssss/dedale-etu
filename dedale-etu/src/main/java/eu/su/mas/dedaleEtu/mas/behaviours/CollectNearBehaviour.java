package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
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
	private List<Couple<String,Couple<Observation,String>>> list_tre_type;   //liste ou se trouve les tresors
	private String pos_tanker;
	private MapRepresentation myMap;
	
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
		
		String nextNodeId=null;
		// Si mon sac est plein je vais au silo
		for ( Couple<Observation,Integer> o : ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace()) {
			if (o.getLeft() == agent_type) {
				if (o.getRight() <= 0) {
					System.out.println("Mon sac est rempli");
					List<String> path = this.myMap.getShortestPath(myPosition.getLocationId(),pos_tanker);
					nextNodeId = path.get(0);
				}
			}
		}
		List<String> positions = new ArrayList<>();
		for (Couple<String,Couple<Observation,String>> c : this.list_tre_type) {
			positions.add(c.getLeft());
		}
		if (nextNodeId == null) {
			List<String> path = this.myMap.getShortestPathToClosestTreasure(myPosition.getLocationId(), positions);
			nextNodeId = path.get(0);
		}
		((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId));
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

	@Override
	public boolean done() {
		return finished;
	}

}
