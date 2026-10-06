package smarthouse.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

/**
 * Time Manager Agent - Controls simulation time with variable speed
 * Normal hours: 1 second = 1 hour
 * Activity change hours: 10 seconds = 1 hour
 */
public class TimeManagerAgent extends Agent {
    private static final long serialVersionUID = 1L;
    
    private int currentHour = 0;
    private int currentDay = 1;
    private int totalHours = 0;
    private static final int SIMULATION_DAYS = 30;
    private static final int LEARNING_DAYS = 7;
    
    private static final long NORMAL_SPEED = 1000; // 1 second
    private static final long SLOW_SPEED = 5000; // 10 seconds
    private static final int[] ACTIVE_HOURS = {6, 7, 8, 9, 18, 19, 20, 21, 22, 23};
    
    @Override
    protected void setup() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("⏰ TIME MANAGER STARTED");
        System.out.println("=".repeat(70));
        System.out.println("⏰ Normal hours: 1 second = 1 hour");
        System.out.println("⏰ Active hours (6-9 AM, 6-11 PM): 10 seconds = 1 hour");
        System.out.println("⏰ Simulation: " + SIMULATION_DAYS + " days");
        System.out.println("⏰ Learning Phase: Days 1-" + LEARNING_DAYS);
        System.out.println("⏰ Prediction Phase: Days " + (LEARNING_DAYS + 1) + "-" + SIMULATION_DAYS);
        System.out.println("=".repeat(70) + "\n");
        
        addBehaviour(new Behaviour() {
            private long nextTickTime = System.currentTimeMillis();
            
            @Override
            public void action() {
                long currentTime = System.currentTimeMillis();
                
                if (currentTime >= nextTickTime) {
                    advanceTime();
                    long delay = isActiveHour(currentHour) ? SLOW_SPEED : NORMAL_SPEED;
                    nextTickTime = currentTime + delay;
                } else {
                    block(100);
                }
            }
            
            @Override
            public boolean done() {
                return currentDay > SIMULATION_DAYS;
            }
        });
    }
    
    private boolean isActiveHour(int hour) {
        for (int h : ACTIVE_HOURS) {
            if (hour == h) return true;
        }
        return false;
    }
    
    private void advanceTime() {
        totalHours++;
        currentHour = (currentHour + 1) % 24;
        
        if (currentHour == 0 && totalHours > 0) {
            currentDay++;
            System.out.println("\n" + "=".repeat(70));
            System.out.println("📅 DAY " + currentDay + " STARTED");
            if (currentDay == LEARNING_DAYS + 1) {
                System.out.println("🧠 LEARNING COMPLETE - PREDICTION MODE ACTIVE");
            }
            System.out.println("=".repeat(70) + "\n");
        }
        
        String marker = isActiveHour(currentHour) ? "🔍 [SLOW]" : "⏱️ ";
        System.out.println(marker + " Day " + currentDay + ", " + String.format("%02d:00", currentHour));
        
        broadcastTime();
        
        if (currentDay > SIMULATION_DAYS) {
            System.out.println("\n✅ SIMULATION COMPLETE\n");
            doDelete();
        }
    }
    
    private void broadcastTime() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("resident", AID.ISLOCALNAME));
        msg.addReceiver(new AID("patternanalyzer", AID.ISLOCALNAME));
        msg.addReceiver(new AID("smartcontroller", AID.ISLOCALNAME));
        msg.addReceiver(new AID("guibridge", AID.ISLOCALNAME));
        
        boolean isLearning = currentDay <= LEARNING_DAYS;
        msg.setContent("TIME_UPDATE:" + currentHour + ":" + currentDay + ":" + totalHours + ":" + isLearning);
        send(msg);
    }
    
    @Override
    protected void takeDown() {
        System.out.println("⏰ Time Manager shutdown");
    }
}