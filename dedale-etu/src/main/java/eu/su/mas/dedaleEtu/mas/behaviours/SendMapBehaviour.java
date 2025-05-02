package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class SendMapBehaviour extends SimpleBehaviour {

    private static final long serialVersionUID = 1L;
    private MapRepresentation map;
    private List<String> otherAgents;
    private boolean finished = false;

    public SendMapBehaviour(Agent agent, MapRepresentation sharedMap, List<String> otherAgents) {
        super(agent);
        this.map = sharedMap;
        this.otherAgents = otherAgents;
    }

    @Override
    public void action() {
        String myName = myAgent.getLocalName();

        for (String other : otherAgents) {
            if (myName.compareTo(other) < 0) {
                sendPingAndShareMap(other);
            } else {
                checkForPingAndRespond();
            }
        }
        finished = true;
    }

    private void sendPingAndShareMap(String receiver) {
        // Envoie du PING
        ACLMessage ping = new ACLMessage(ACLMessage.INFORM);
        ping.setProtocol("PING");
        ping.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        myAgent.send(ping);

        // Attente du PONG
        MessageTemplate pongTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol("PONG"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        ACLMessage pong = this.myAgent.blockingReceive(pongTemplate);
        if (pong != null) {
            // Envoie de la carte
            ACLMessage share = new ACLMessage(ACLMessage.INFORM);
            share.setProtocol("SHARE-TOPO");
            share.addReceiver(new AID(receiver, AID.ISLOCALNAME));
            try {
                share.setContentObject(map.getSerializableGraph());
                myAgent.send(share);
                System.out.println(myAgent.getLocalName() + " a partagé sa carte avec " + receiver);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkForPingAndRespond() {
        MessageTemplate pingTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol("PING"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        ACLMessage ping = myAgent.receive(pingTemplate);
        if (ping != null) {
            // Répondre avec un PONG
            ACLMessage pong = new ACLMessage(ACLMessage.INFORM);
            pong.setProtocol("PONG");
            pong.addReceiver(ping.getSender());
            myAgent.send(pong);

            // Attente de la carte
            MessageTemplate mapTemplate = MessageTemplate.and(
                    MessageTemplate.MatchProtocol("SHARE-TOPO"),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM)
            );

            ACLMessage mapMsg = this.myAgent.blockingReceive(mapTemplate);
            if (mapMsg != null) {
                try {
                    @SuppressWarnings("unchecked")
                    SerializableSimpleGraph<String, MapAttribute> receivedGraph =
                            (SerializableSimpleGraph<String, MapAttribute>) mapMsg.getContentObject();
                    this.map.mergeMap(receivedGraph);
                    System.out.println(myAgent.getLocalName() + " a reçu et fusionné la carte.");
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public boolean done() {
        return finished;
    }
}
