package smarthouse.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import smarthouse.SmartHouseGUI;

public class GUIBridgeAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private SmartHouseGUI gui;
    private boolean guiReady = false;
    
    protected void setup() {
        System.out.println("🖥️  GUI Bridge started");
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gui = SmartHouseGUI.getInstance();
                guiReady = true;
            }
        });
        
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                
                if (msg != null) {
                    if (!guiReady) {
                        return;
                    }
                    
                    String content = msg.getContent();
                    
                    try {
                        if (content.startsWith("RESIDENT_STATE:")) {
                            String[] parts = content.split(":");
                            String location = parts[2];
                            String activity = parts[3];
                            gui.updateResidentLocation(location);
                            gui.updateResidentActivity(activity);
                            
                        } else if (content.startsWith("TIME_UPDATE:")) {
                            String[] parts = content.split(":");
                            int hour = Integer.parseInt(parts[1]);
                            int day = Integer.parseInt(parts[2]);
                            gui.updateTime(hour);
                            gui.updateDay(day);
                            
                        } else if (content.startsWith("SET_INTENSITY:")) {
                            String[] parts = content.split(":");
                            String location = parts[1];
                            int intensity = Integer.parseInt(parts[2]);
                            gui.updateLightIntensity(location, intensity);
                            
                        } else if (content.startsWith("THERMOSTAT:")) {
                            String[] parts = content.split(":");
                            double temp = Double.parseDouble(parts[1]);
                            String mode = parts[2];
                            gui.updateTemperature(temp, mode);
                            
                        } else if (content.startsWith("TEMPERATURE:")) {
                            String[] parts = content.split(":");
                            double temp = Double.parseDouble(parts[1]);
                            gui.updateTemperature(temp, "AUTO");
                        }
                    } catch (Exception e) {
                        // Silently ignore errors
                    }
                } else {
                    block();
                }
            }
        });
    }
    
    protected void takeDown() {
        System.out.println("🖥️  GUI Bridge shutdown");
    }
}