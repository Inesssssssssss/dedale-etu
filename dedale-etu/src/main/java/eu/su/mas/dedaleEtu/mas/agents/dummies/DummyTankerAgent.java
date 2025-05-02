package eu.su.mas.dedaleEtu.mas.agents.dummies;


import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;



import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.ReceiveTreasureTankerBehaviour;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;
import eu.su.mas.dedale.mas.agent.knowledge.AgentObservableElement;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Serializable;


/**
 * Dummy Tanker agent. It does nothing more than printing what it observes every 10s and receiving the treasures from other agents. 
 * <p>
 * Note that this last behaviour is hidden, every tanker agent automatically possess it.
 * 
 * @author hc
 *
 */
public class DummyTankerAgent extends AbstractDedaleAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1784844593772918359L;



	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();

		List<Behaviour> lb=new ArrayList<Behaviour>();
		lb.add(new RandomTankerBehaviour(this));
		
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

}


/**************************************
 * 
 * 
 * 				BEHAVIOUR
 * 
 * 
 **************************************/

class RandomTankerBehaviour extends TickerBehaviour{
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *  
	 */
	private static final long serialVersionUID = 9088209402507795289L;
	public static final String PROTOCOL_TANKER="ProtocolTanker";
	private AgentObservableElement aoe;
	

	public RandomTankerBehaviour (final AbstractDedaleAgent myagent) {
		super(myagent, 10000);
		this.aoe=new AgentObservableElement(this.myAgent.getName());
	}

	@Override
	public void onTick() {
		//Example to retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);
			MessageTemplate msgTemplate=MessageTemplate.and(
					MessageTemplate.MatchProtocol("ProtocolTanker"),
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
			ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
			if (msgReceived!=null) {
				ACLMessage msg= new ACLMessage(ACLMessage.AGREE);
				msg.setProtocol(ReceiveTreasureTankerBehaviour.PROTOCOL_TANKER);
				msg.setSender(this.myAgent.getAID());
				msg.addReceiver(msgReceived.getSender());
				((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
				try {
					java.io.Serializable o = msgReceived.getContentObject();
					if (o instanceof List) {
						@SuppressWarnings("unchecked")
						List<Couple<Observation, Integer>> l_obs = (List<Couple<Observation, Integer>>) o;
						System.out.println("J'ai recu un msg : "+ o);
						for(Couple<Observation, Integer> obs :l_obs) {
							
							switch(obs.getLeft()) {
							case DIAMOND:
								this.aoe.add2TreasureValue(Observation.DIAMOND, obs.getRight());
								System.out.println("Tanker backpack "+this.aoe.getBackPackUsedSpace(Observation.DIAMOND));
							case GOLD:
								System.out.println("i got diamonds "+ obs.getRight());
								System.out.println("Tanker backpack "+this.aoe.getBackPackUsedSpace(Observation.GOLD));
								this.aoe.add2TreasureValue(Observation.GOLD, obs.getRight());
								//this.aoe.setCurrentGoldValue(10);
								
								//((AbstractDedaleAgent) this.myAgent).pick();
							default:
								break;
							}
						}
					}
					
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		
		}
		

	}

}