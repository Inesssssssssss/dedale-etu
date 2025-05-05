package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.SimpleBehaviour;

public class MainBehaviour extends SimpleBehaviour{
	private MapRepresentation myMap;
	private List<String> agentNames;
	private MeetingExploreBehaviour MEB;
	private CollectNearBehaviour CNB;
	private int MEB_starter = 0;
	
	public MainBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames) {
		super(myagent);
		this.myMap = myMap;
		this.agentNames = agentNames;
		
		//this.MEB = new MeetingExploreBehaviour((AbstractDedaleAgent) this.myAgent, this.myMap, this.agentNames);
	}

	@Override
	public void action() {
		if (this.MEB_starter==0) {
			this.MEB = new MeetingExploreBehaviour((AbstractDedaleAgent) this.myAgent, this.myMap, this.agentNames);
			this.myAgent.addBehaviour(MEB);
			
			this.CNB = new CollectNearBehaviour((AbstractDedaleAgent) this.myAgent,null, this.myMap, null);
			this.MEB_starter++;
		}
		if (this.MEB.done()) {
			System.out.println("MEB done for "+this.myAgent.getLocalName());
			List<Couple<String,Couple<Observation,String>>> list_tre = this.MEB.get_treasure();
			String pos_tanker = this.MEB.get_tanker();
			MapRepresentation map = this.MEB.get_map();
			this.myAgent.removeBehaviour(MEB);
			
			if (this.CNB.get_pos_tanker()==null) {
				this.CNB.set_list_tre(list_tre);
				this.CNB.set_pos_tanker(pos_tanker);
				this.CNB.set_map(map);
				
				
				this.myAgent.addBehaviour(CNB);
			}
		}else {
			//this.myAgent.addBehaviour(MEB);
		}
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
