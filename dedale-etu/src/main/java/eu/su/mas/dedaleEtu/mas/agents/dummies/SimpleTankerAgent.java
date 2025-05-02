package eu.su.mas.dedaleEtu.mas.agents.dummies;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



import dataStructures.tuple.Couple;
import debug.Debug;
import eu.su.mas.dedale.env.EntityCharacteristics;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.ReceiveTreasureTankerBehaviour;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;
import eu.su.mas.dedale.mas.agent.knowledge.AgentObservableElement;
import eu.su.mas.dedaleEtu.mas.behaviours.CollectBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MeetingShareBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendPosTankerBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


/**
 * Dummy Tanker agent. It does nothing more than printing what it observes every 10s and receiving the treasures from other agents. 
 * <p>
 * Note that this last behaviour is hidden, every tanker agent automatically possess it.
 * 
 * @author hc
 *
 */
public class SimpleTankerAgent extends AbstractDedaleAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1784844593772918359L;
	
	private MapRepresentation myMap;
	



	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		
		//get the parameters added to the agent at creation (if any)
		final Object[] args = getArguments();
		
		List<String> list_agentNames=new ArrayList<String>();
		
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				list_agentNames.add((String)args[i]);
				i++;
			}
		}
		List<Behaviour> lb=new ArrayList<Behaviour>();
		lb.add(new RandomTankerBehaviour(this,this.myMap, list_agentNames));
		
		
		addBehaviour(new StartMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){
		super.takeDown();
	}

	protected void beforeMove(){
		super.beforeMove();
		//System.out.println("I migrate");
	}

	protected void afterMove(){
		super.afterMove();
		//System.out.println("I migrated");
	}
	
	public AgentObservableElement getAOE() {
		Field aoe;
		try {
			aoe = this.getClass().getSuperclass().getDeclaredField("aoe");
			aoe.setAccessible(true);
			try {
				return (AgentObservableElement) aoe.get(this);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	}
		return null;
}

/**************************************
 * 
 * 
 * 				BEHAVIOUR
 * 
 * 
 **************************************/

class RandomTankerBehaviour extends SimpleBehaviour{
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *  
	 */
	private static final long serialVersionUID = 9088209402507795289L;
	public static final String PROTOCOL_TANKER="ProtocolTanker";
	
	private MapRepresentation myMap;
	private MeetingShareBehaviour MSB;
	private SendPosTankerBehaviour SPT;
	private List<String> list_agentNames;

	public RandomTankerBehaviour (final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames) {
		super(myagent);
		this.myMap = myMap;
		this.list_agentNames=agentNames;
	}

	@Override
	public void action() {
		//on ajoute le noeud courant pour le donner aux autre agents
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
		}
		//this.MSB = new MeetingShareBehaviour(this.myAgent,this.myMap, this.list_agentNames);
		this.SPT = new SendPosTankerBehaviour(this.myAgent, this.myMap);
		
		//this.myAgent.addBehaviour(MSB);
		this.myAgent.addBehaviour(SPT);
		
		List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

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
		
		//Example to retrieve the current position
		//Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			//List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			//System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);
			
			MessageTemplate template= 
					MessageTemplate.and(
							MessageTemplate.MatchProtocol(PROTOCOL_TANKER),
							MessageTemplate.MatchPerformative(ACLMessage.REQUEST)		
							);

			//I'm waiting for a message from a collector
			ACLMessage msgReceived=this.myAgent.receive(template);
			//Couple<Observation,Integer> c=null;
			
			if (msgReceived!=null){
				
				ACLMessage resp= new ACLMessage(ACLMessage.AGREE);
				resp.setProtocol(ReceiveTreasureTankerBehaviour.PROTOCOL_TANKER);
				resp.setSender(this.myAgent.getAID());
				resp.addReceiver(msgReceived.getSender());
				
				//Debug.warning("Tanker agent - Message received: "+msg.toString());
				List<Couple<Observation, Integer>>c=null;
				try {
					c=(List<Couple<Observation, Integer>>) msgReceived.getContentObject();
				} catch (UnreadableException e) {
					Debug.error("Tanker receiving non Deserializable value");
					e.printStackTrace();
				}
				//Integer i=this.ec.getBackPackCapacity(c.getLeft())-this.aoe.getBackPackUsedSpace((c.getLeft()));
				System.out.println("j'ai recu le message"+c);
				
				((AbstractDedaleAgent) this.myAgent).sendMessage(resp);
				for(Couple<Observation,Integer> o : c){
					SimpleTankerAgent agentT = (SimpleTankerAgent) this.myAgent;
					agentT.getAOE().add2TreasureValue(o.getLeft(), o.getRight());
					}
				}
			}
		/*
		else{
			block();
			}*/
		}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
}
}

