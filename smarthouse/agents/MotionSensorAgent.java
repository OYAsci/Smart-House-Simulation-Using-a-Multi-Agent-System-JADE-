package smarthouse.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class MotionSensorAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private String location;
    
    @Override
    protected void setup() {
        Object[] args = getArguments();
        location = (args != null && args.length > 0) ? (String) args[0] : "Living Room";
        
        System.out.println("👁️  Motion Sensor monitoring: " + location);
        
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                
                if (msg != null) {
                    String content = msg.getContent();
                    
                    if (content.startsWith("RESIDENT_STATE:")) {
                        String[] parts = content.split(":");
                        String residentLocation = parts[2];
                        
                        if (residentLocation.equals(location)) {
                            System.out.println("👁️  Motion detected in " + location);
                            
                            ACLMessage motionMsg = new ACLMessage(ACLMessage.INFORM);
                            motionMsg.addReceiver(new AID("lightcontroller", AID.ISLOCALNAME));
                            motionMsg.setContent("MOTION_DETECTED:" + location);
                            send(motionMsg);
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }
    
    @Override
    protected void takeDown() {
        System.out.println("👁️  Motion Sensor shutdown: " + location);
    }
}