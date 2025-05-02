package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agents.dedaleDummyAgents.DummyTankerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CollectBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;
	private boolean b = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	private MeetingShareBehaviour MSB;
	private MapRepresentation sharedMap;
	
	private List<String> list_agentNames;


/**
 * 
 * @param myagent reference to the agent we are adding this behaviour to
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public CollectBehaviour(final Agent myagent, MapRepresentation myMap, List<String> agentNames) {
		super(myagent);
		this.myMap=myMap;
		this.sharedMap=null;
		this.list_agentNames=agentNames;
		
		
	}

	@Override
	public void action() {
		//Example to retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null && myPosition.getLocationId()!=""){
			List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			//System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);

			//Little pause to allow you to follow what is going on
//			try {
//				System.out.println("Press enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
//				System.in.read();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
			int relative_pos = 0;
			//list of observations associated to the currentPosition
			for (int i = 0; i < lobs.size(); i++) {
				
				List<Couple<Observation,String>> lObservations= lobs.get(i).getRight();
				//example related to the use of the backpack for the treasure hunt
				Boolean b=false;
				for(Couple<Observation,String> o:lObservations){
					switch (o.getLeft()) {
					case DIAMOND:
						if (i == 0) {
							System.out.println(this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
							System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
							System.out.println(this.myAgent.getLocalName()+" - I try to open the safe: "+((AbstractDedaleAgent) this.myAgent).openLock(Observation.DIAMOND));
							System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
							System.out.println(this.myAgent.getLocalName()+" - The agent grabbed :"+((AbstractDedaleAgent) this.myAgent).pick());
							System.out.println(this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
							//this.b=true;
							break;
						}
					case GOLD:
						if (i == 0) {
							System.out.println(this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
							System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
							System.out.println(this.myAgent.getLocalName()+" - I try to open the safe: "+((AbstractDedaleAgent) this.myAgent).openLock(Observation.GOLD));
							System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
							System.out.println(this.myAgent.getLocalName()+" - The agent grabbed :"+((AbstractDedaleAgent) this.myAgent).pick());
							System.out.println(this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
							this.b=true;
							break;
						}
					case AGENTNAME:
						if (relative_pos != i) {
							relative_pos = i;
							//System.out.println("b : "+this.b);
							if (this.b) {
								String agentName = o.getRight();
								//System.out.println(agentName);
								if (myAgent.getAID(agentName).getName().equals("Tim@Ithaq")) {
									//System.out.println("Je vide mon sac");
									//((AbstractDedaleAgent)myAgent).emptyMyBackPack(agentName);
									System.out.println(this.myAgent.getLocalName()+" - The agent tries to transfer is load into the Silo (if reachable); succes ? : "+((AbstractDedaleAgent)this.myAgent).emptyMyBackPack(agentName));
									//System.out.println("Je vide mon sac");
									//((AbstractDedaleAgent)myAgent).emptyMyBackPack("Elsa");
								}
								this.b = false;
							}
							break;
							
							
						}
						
					default:
						break;
					}
				}

			//If the agent picked (part of) the treasure
			if (b){
				List<Couple<Location,List<Couple<Observation,String>>>> lobs2=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
				System.out.println("State of the observations after picking "+lobs2);
			}
		}
			
			//((AbstractDedaleAgent)myAgent).emptyMyBackPack("Elsa");

			//Trying to store everything in the tanker
			//System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());
			//System.out.println(this.myAgent.getLocalName()+" - The agent tries to transfer is load into the Silo (if reachable); succes ? : "+((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("Tank"));
			//System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());

			//Random move from the current position
			//Random r= new Random();
			//int moveId=1+r.nextInt(lobs.size()-1);//removing the current position from the list of target to accelerate the tests, but not necessary as to stay is an action

			//The move action (if any) should be the last action of your behaviour
			//((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
		}

	}

	@Override
	public boolean done() {
		return finished;
	}


}
