package smarthouse.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import java.util.Random;

public class ResidentAgent extends Agent {
    private static final long serialVersionUID = 1L;
    
    private String currentLocation = "Bedroom";
    private String currentActivity = "SLEEPING";
    private int currentHour = 0;
    private int currentDay = 1;
    private Random random = new Random();
    private String residentID = "RESIDENT_001";
    
    @Override
    protected void setup() {
        System.out.println("👤 Resident Agent started - ID: " + residentID);
        
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                
                if (msg != null) {
                    String content = msg.getContent();
                    
                    if (content.startsWith("TIME_UPDATE:")) {
                        handleTimeUpdate(content);
                    } else if (content.startsWith("GOTO:")) {
                        handleManualCommand(content);
                    } else if (content.startsWith("BEDTIME_ALERT")) {
                        System.out.println("👤 📢 BEDTIME ALERT: Time to sleep soon!");
                    }
                } else {
                    block();
                }
            }
        });
    }
    
    private void handleTimeUpdate(String content) {
        String[] parts = content.split(":");
        currentHour = Integer.parseInt(parts[1]);
        currentDay = Integer.parseInt(parts[2]);
        
        updateActivityAndLocation();
    }
    
    private void updateActivityAndLocation() {
        String previousLocation = currentLocation;
        String previousActivity = currentActivity;
        
        if (currentHour >= 0 && currentHour < 7) {
            currentLocation = "Bedroom";
            currentActivity = "SLEEPING";
        } else if (currentHour == 7) {
            currentLocation = "Bedroom";
            currentActivity = "AWAKE";
        } else if (currentHour == 8) {
            currentLocation = "Bathroom";
            currentActivity = "SHOWERING";
        } else if (currentHour >= 9 && currentHour < 12) {
            currentLocation = random.nextDouble() < 0.7 ? "Kitchen" : "Living Room";
            currentActivity = "RELAXING";
        } else if (currentHour >= 12 && currentHour < 13) {
            currentLocation = "Kitchen";
            currentActivity = "RELAXING";
        } else if (currentHour >= 13 && currentHour < 18) {
            currentLocation = "Living Room";
            currentActivity = random.nextDouble() < 0.6 ? "WORKING" : "USING_SCREEN";
        } else if (currentHour >= 18 && currentHour < 19) {
            currentLocation = "Kitchen";
            currentActivity = "RELAXING";
        } else if (currentHour >= 19 && currentHour < 22) {
            currentLocation = "Living Room";
            currentActivity = random.nextDouble() < 0.7 ? "USING_SCREEN" : "RELAXING";
        } else if (currentHour == 22) {
            if (random.nextDouble() < 0.5) {
                currentLocation = "Bathroom";
                currentActivity = "SHOWERING";
            } else {
                currentLocation = "Living Room";
                currentActivity = "RELAXING";
            }
        } else {
            currentLocation = "Bedroom";
            currentActivity = "USING_SCREEN";
        }
        
        if (!currentLocation.equals(previousLocation) || !currentActivity.equals(previousActivity)) {
            System.out.println(String.format("👤 [Day %d, %02d:00] %s → %s in %s", 
                currentDay, currentHour, currentActivity, residentID, currentLocation));
            notifySystem();
        }
    }
    
    private void notifySystem() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("patternanalyzer", AID.ISLOCALNAME));
        msg.addReceiver(new AID("smartcontroller", AID.ISLOCALNAME));
        msg.addReceiver(new AID("motionsensor1", AID.ISLOCALNAME));
        msg.addReceiver(new AID("motionsensor2", AID.ISLOCALNAME));
        msg.addReceiver(new AID("motionsensor3", AID.ISLOCALNAME));
        msg.addReceiver(new AID("motionsensor4", AID.ISLOCALNAME));
        msg.addReceiver(new AID("guibridge", AID.ISLOCALNAME));
        
        msg.setContent(String.format("RESIDENT_STATE:%s:%s:%s:%d:%d", 
            residentID, currentLocation, currentActivity, currentHour, currentDay));
        send(msg);
    }
    
    private void handleManualCommand(String content) {
        String[] parts = content.split(":");
        if (parts.length >= 2) {
            currentLocation = parts[1];
            if (parts.length >= 3) {
                currentActivity = parts[2];
            }
            System.out.println("👤 Manual: " + currentActivity + " in " + currentLocation);
            notifySystem();
        }
    }
    
    @Override
    protected void takeDown() {
        System.out.println("👤 Resident leaving");
    }
}