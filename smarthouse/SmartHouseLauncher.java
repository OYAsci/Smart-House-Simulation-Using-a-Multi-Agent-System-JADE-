package smarthouse;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;

public class SmartHouseLauncher {
    
    public static void main(String[] args) {
        try {
            System.out.println("\n" + "=".repeat(70));
            System.out.println(" SMART HOUSE MULTI-AGENT SYSTEM");
            System.out.println("=".repeat(70));
            System.out.println(" 30-day simulation with learning & prediction");
            System.out.println(" Variable time: 1-10 seconds per hour");
            System.out.println(" Learning: Days 1-7");
            System.out.println(" Prediction: Days 8-30");
            System.out.println("=".repeat(70) + "\n");
            
            Runtime runtime = Runtime.instance();
            
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "true");
            
            AgentContainer mainContainer = runtime.createMainContainer(profile);
            
            System.out.println(" Starting agents...\n");
            
            // 1. GUI Bridge
            mainContainer.createNewAgent("guibridge", "smarthouse.agents.GUIBridgeAgent", null).start();
            Thread.sleep(500);
            
            // 2. Time Manager (CRITICAL - controls time!)
            mainContainer.createNewAgent("timemanager", "smarthouse.agents.TimeManagerAgent", null).start();
            
            // 3. Pattern Analyzer
            mainContainer.createNewAgent("patternanalyzer", "smarthouse.agents.PatternAnalyzerAgent", null).start();
            
            // 4. Smart Controller
            mainContainer.createNewAgent("smartcontroller", "smarthouse.agents.SmartControllerAgent", null).start();
            
            // 5. Resident
            mainContainer.createNewAgent("resident", "smarthouse.agents.ResidentAgent", null).start();
            
            // 6. Temperature Sensor
            mainContainer.createNewAgent("tempsensor", "smarthouse.agents.TemperatureSensorAgent", null).start();
            
            // 7. Thermostat
            mainContainer.createNewAgent("thermostat", "smarthouse.agents.ThermostatAgent", null).start();
            
            // 8. Motion Sensors
            String[] rooms = {"Living Room", "Kitchen", "Bedroom", "Bathroom"};
            for (int i = 0; i < rooms.length; i++) {
                mainContainer.createNewAgent(
                    "motionsensor" + (i + 1),
                    "smarthouse.agents.MotionSensorAgent",
                    new Object[]{rooms[i]}
                ).start();
            }
            
            // 9. Light Controller
            mainContainer.createNewAgent("lightcontroller", "smarthouse.agents.LightControllerAgent", null).start();
            
            System.out.println("\n" + "=".repeat(70));
            System.out.println("✅ ALL AGENTS STARTED");
            System.out.println("=".repeat(70));
            System.out.println("\n🎬 SIMULATION STARTING...\n");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}