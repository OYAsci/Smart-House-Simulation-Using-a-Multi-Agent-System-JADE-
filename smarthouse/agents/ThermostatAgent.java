package smarthouse.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class ThermostatAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private double targetTemperature = 21.0;
    private double currentTemperature = 22.0;
    private String mode = "OFF";
    
    protected void setup() {
        System.out.println("🌡️  Thermostat started - Target: " + targetTemperature + "°C");
        
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                
                if (msg != null) {
                    String content = msg.getContent();
                    
                    if (content.startsWith("TEMPERATURE:")) {
                        currentTemperature = Double.parseDouble(content.split(":")[1]);
                        adjustClimate();
                    } else if (content.startsWith("PREHEAT:")) {
                        String[] parts = content.split(":");
                        System.out.println("🌡️  Pre-heating " + parts[1] + " to " + parts[2] + "°C");
                    }
                } else {
                    block();
                }
            }
        });
    }
    
    private void adjustClimate() {
        String previousMode = mode;
        
        if (currentTemperature > targetTemperature + 0.5) {
            mode = "COOLING";
        } else if (currentTemperature < targetTemperature - 0.5) {
            mode = "HEATING";
        } else {
            mode = "OFF";
        }
        
        if (!mode.equals(previousMode)) {
            System.out.println("❄️🔥 Thermostat: " + String.format("%.1f", currentTemperature) + "°C → " + mode);
        }
        
        // ALWAYS send to GUI (even if mode didn't change)
        ACLMessage guiMsg = new ACLMessage(ACLMessage.INFORM);
        guiMsg.addReceiver(new AID("guibridge", AID.ISLOCALNAME));
        guiMsg.setContent("THERMOSTAT:" + currentTemperature + ":" + mode);
        send(guiMsg);
    }
    
    protected void takeDown() {
        System.out.println("🌡️  Thermostat shutdown");
    }
}