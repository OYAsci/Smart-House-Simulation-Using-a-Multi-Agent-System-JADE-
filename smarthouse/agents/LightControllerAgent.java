package smarthouse.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.HashMap;
import java.util.Map;

public class LightControllerAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private Map<String, Integer> lightIntensity = new HashMap<>();
    
    @Override
    protected void setup() {
        System.out.println("💡 Light Controller started");
        
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                
                if (msg != null) {
                    String content = msg.getContent();
                    
                    if (content.startsWith("SET_INTENSITY:")) {
                        String[] parts = content.split(":");
                        String room = parts[1];
                        int intensity = Integer.parseInt(parts[2]);
                        lightIntensity.put(room, intensity);
                    } else if (content.startsWith("MOTION_DETECTED:")) {
                        String room = content.split(":")[1];
                        System.out.println("💡 Motion in " + room);
                    }
                } else {
                    block();
                }
            }
        });
    }
    
    @Override
    protected void takeDown() {
        System.out.println("💡 Light Controller shutdown");
    }
}