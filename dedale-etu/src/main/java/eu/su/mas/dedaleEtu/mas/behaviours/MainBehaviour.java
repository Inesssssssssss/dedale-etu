package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.SimpleBehaviour;

public class MainBehaviour extends SimpleBehaviour{
	private boolean finished = false;
	private MapRepresentation myMap;
	private List<String> agentNames;
	private MeetingExploreBehaviour MEB;
	private CollectNearBehaviour CNB;
	private CollectBehaviour CB;
	private int MEB_starter = 0;
	private int EB_starter = 0;
	private ExplorerBehaviour EB;
	
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
			this.CB = new CollectBehaviour(this.myAgent, this.agentNames);
			this.myAgent.addBehaviour(MEB);
			this.myAgent.addBehaviour(CB);
			
			this.CNB = new CollectNearBehaviour((AbstractDedaleAgent) this.myAgent,null, this.myMap, null);
			this.MEB_starter++;
		}else {
			String tankerName = this.MEB.getNameTanker();
			if (tankerName!=null) {
				//System.out.println("Main : tankerName set : "+tankerName+" agent : "+this.myAgent.getLocalName());
				this.CB.setTankerName(tankerName);
			}
		}
		if (this.MEB.done()) {
			
			if (this.CNB.get_pos_tanker()==null) {
				if(this.myAgent.getLocalName().equals("E1")) {
					System.out.println("Agent explorer a fini MEB");
					if(this.EB_starter==0) {
						this.EB_starter++;
						this.EB = new ExplorerBehaviour((AbstractDedaleAgent) this.myAgent,this.MEB.get_map(),this.agentNames,this.MEB.get_treasure());
						this.myAgent.addBehaviour(EB);
					}
						
					
				}else {
					System.out.println("MEB done for "+this.myAgent.getLocalName());
					List<Couple<String,Couple<Observation,String>>> list_tre = this.MEB.get_treasure();
					String pos_tanker = this.MEB.get_tanker();
					MapRepresentation map = this.MEB.get_map();
					this.myAgent.removeBehaviour(MEB);
					this.CNB.set_list_tre(list_tre);
					this.CNB.set_pos_tanker(pos_tanker);
					this.CNB.set_map(map);
					System.out.println("Main : liste des tresors : "+list_tre);
					
					
					this.myAgent.addBehaviour(CNB);
				}
		}
			
			
			
			
		}if(this.CNB.done()) {
			this.myAgent.removeBehaviour(CB);
			this.myAgent.removeBehaviour(CNB);
			finished=true;
			this.myAgent.doDelete();
		}
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}

}
