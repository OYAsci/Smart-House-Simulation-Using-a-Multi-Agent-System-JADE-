package smarthouse.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class TemperatureSensorAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private double currentTemperature = 22.0;
    
    protected void setup() {
        System.out.println("🌡️  Temperature Sensor started");
        
        addBehaviour(new TickerBehaviour(this, 5000) {
            protected void onTick() {
                currentTemperature += (Math.random() - 0.5) * 0.3;
                
                // Send to thermostat
                ACLMessage tempMsg = new ACLMessage(ACLMessage.INFORM);
                tempMsg.addReceiver(new AID("thermostat", AID.ISLOCALNAME));
                tempMsg.setContent("TEMPERATURE:" + currentTemperature);
                send(tempMsg);
                
                // Send to GUI bridge
                ACLMessage guiMsg = new ACLMessage(ACLMessage.INFORM);
                guiMsg.addReceiver(new AID("guibridge", AID.ISLOCALNAME));
                guiMsg.setContent("TEMPERATURE:" + currentTemperature);
                send(guiMsg);
            }
        });
    }
    
    protected void takeDown() {
        System.out.println("🌡️  Temperature Sensor shutdown");
    }
}