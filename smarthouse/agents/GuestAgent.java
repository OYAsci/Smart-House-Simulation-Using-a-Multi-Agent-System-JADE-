package smarthouse.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class GuestAgent extends Agent {
    private static final long serialVersionUID = 1L;
    
    private String guestID;
    private String guestType;
    private String currentLocation = "Living Room";
    private boolean isAuthorized = false;
    
    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length >= 2) {
            guestID = (String) args[0];
            guestType = (String) args[1];
            isAuthorized = guestType.equals("KNOWN_GUEST");
        } else {
            guestID = "UNKNOWN_" + System.currentTimeMillis();
            guestType = "STRANGER";
        }
        
        System.out.println("🚪 Guest entered - ID: " + guestID + " | Type: " + guestType);
        announcePresence();
        
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String content = msg.getContent();
                    if (content.startsWith("GOTO:")) {
                        String[] parts = content.split(":");
                        currentLocation = parts[1];
                        System.out.println("🚪 Guest moved to " + currentLocation);
                    }
                } else {
                    block();
                }
            }
        });
    }
    
    private void announcePresence() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("smartcontroller", AID.ISLOCALNAME));
        msg.setContent(String.format("PERSON_DETECTED:%s:%s:%s:%b", 
            guestID, guestType, currentLocation, isAuthorized));
        send(msg);
    }
    
    @Override
    protected void takeDown() {
        System.out.println("🚪 Guest " + guestID + " left");
    }
}