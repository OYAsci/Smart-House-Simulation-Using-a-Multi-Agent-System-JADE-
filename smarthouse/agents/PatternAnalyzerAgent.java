package smarthouse.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import java.util.*;

public class PatternAnalyzerAgent extends Agent {
    private static final long serialVersionUID = 1L;
    
    private List<String> movementHistory = new ArrayList<>();
    private Map<String, Integer> locationFrequency = new HashMap<>();
    private Map<Integer, Map<String, Integer>> hourlyLocationCount = new HashMap<>();
    private Map<Integer, Map<String, Integer>> hourlyActivityCount = new HashMap<>();
    private List<Integer> showerHours = new ArrayList<>();
    private int currentDay = 1;
    private boolean patternsLearned = false;
    
    @Override
    protected void setup() {
        System.out.println("🧠 Pattern Analyzer started - Learning mode");
        
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                
                if (msg != null) {
                    String content = msg.getContent();
                    
                    if (content.startsWith("RESIDENT_STATE:")) {
                        parseResidentState(content);
                    } else if (content.startsWith("TIME_UPDATE:")) {
                        handleTimeUpdate(content);
                    }
                } else {
                    block();
                }
            }
        });
        
        addBehaviour(new TickerBehaviour(this, 60000) {
            @Override
            protected void onTick() {
                if (currentDay == 7 && !patternsLearned) {
                    analyzeAndSendPatterns();
                }
            }
        });
    }
    
    private void handleTimeUpdate(String content) {
        String[] parts = content.split(":");
        currentDay = Integer.parseInt(parts[2]);
    }
    
    private void parseResidentState(String content) {
        // Format: RESIDENT_STATE:ID:location:activity:hour:day
        String[] parts = content.split(":");
        if (parts.length >= 6) {
            String location = parts[2];
            String activity = parts[3];
            int hour = Integer.parseInt(parts[4]);
            
            movementHistory.add(content);
            locationFrequency.put(location, locationFrequency.getOrDefault(location, 0) + 1);
            
            // Track hourly patterns
            hourlyLocationCount.putIfAbsent(hour, new HashMap<>());
            hourlyLocationCount.get(hour).put(location, 
                hourlyLocationCount.get(hour).getOrDefault(location, 0) + 1);
                        
            if (activity.equals("SHOWERING") && !showerHours.contains(hour)) {
                showerHours.add(hour);
            }
            
            System.out.println(" Recorded: " + location + " (" + activity + ") at " + 
                String.format("%02d:00", hour) + " | Total: " + movementHistory.size());
        }
    }
    
    private void analyzeAndSendPatterns() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🧠 LEARNING COMPLETE - Sending patterns to Smart Controller");
        System.out.println("=".repeat(70));
        
        // Send hourly location patterns
        for (Map.Entry<Integer, Map<String, Integer>> entry : hourlyLocationCount.entrySet()) {
            int hour = entry.getKey();
            String mostCommonLocation = entry.getValue().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Living Room");
            
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("smartcontroller", AID.ISLOCALNAME));
            msg.setContent("PATTERN_LEARNED:LOCATION:" + hour + ":" + mostCommonLocation);
            send(msg);
        }
        
        // Send hourly activity patterns
        for (Map.Entry<Integer, Map<String, Integer>> entry : hourlyActivityCount.entrySet()) {
            int hour = entry.getKey();
            String mostCommonActivity = entry.getValue().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("RELAXING");
            
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("smartcontroller", AID.ISLOCALNAME));
            msg.setContent("PATTERN_LEARNED:ACTIVITY:" + hour + ":" + mostCommonActivity);
            send(msg);
        }
        
        // Send shower hours
        for (int hour : showerHours) {
            System.out.println("🧠 Pattern: Shower at " + hour + ":00");
        }
        
        // Send bedtime (hour when SLEEPING starts)
        int bedtime = 23;
        ACLMessage bedtimeMsg = new ACLMessage(ACLMessage.INFORM);
        bedtimeMsg.addReceiver(new AID("smartcontroller", AID.ISLOCALNAME));
        bedtimeMsg.setContent("PATTERN_LEARNED:BEDTIME:" + bedtime);
        send(bedtimeMsg);
        
        System.out.println("🧠 Patterns sent to Smart Controller");
        System.out.println("=".repeat(70) + "\n");
        
        patternsLearned = true;
    }
    
    @Override
    protected void takeDown() {
        System.out.println("🧠 Pattern Analyzer shutdown - " + movementHistory.size() + " records");
    }
}