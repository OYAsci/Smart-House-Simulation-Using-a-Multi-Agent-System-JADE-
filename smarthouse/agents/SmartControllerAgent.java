package smarthouse.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import java.util.*;

public class SmartControllerAgent extends Agent {
    private static final long serialVersionUID = 1L;
    
    private int currentHour = 0;
    private int currentDay = 1;
    private boolean isLearningPhase = true;
    private String residentLocation = "Unknown";
    private String residentActivity = "UNKNOWN";
    private String previousLocation = "Unknown";
    
    private Map<Integer, String> hourlyLocationPattern = new HashMap<Integer, String>();
    private Map<String, Map<String, Integer>> locationTransitions = new HashMap<String, Map<String, Integer>>();
    private Map<Integer, Map<String, Map<String, Integer>>> hourlyLocationTransitions = new HashMap<Integer, Map<String, Map<String, Integer>>>();
    
    private List<Integer> showerHours = new ArrayList<Integer>();
    private int typicalBedtime = 23;
    
    private String[] allRooms = {"Living Room", "Kitchen", "Bedroom", "Bathroom"};
    
    protected void setup() {
        System.out.println("🏠 Smart Controller started with Markov Chain prediction");
        System.out.println("🏠 Will show BOTH prediction methods for comparison");
        
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                
                if (msg != null) {
                    String content = msg.getContent();
                    
                    if (content.startsWith("TIME_UPDATE:")) {
                        handleTimeUpdate(content);
                    } else if (content.startsWith("RESIDENT_STATE:")) {
                        handleResidentState(content);
                    } else if (content.startsWith("PATTERN_LEARNED:")) {
                        handleLearnedPattern(content);
                    } else if (content.startsWith("PERSON_DETECTED:")) {
                        handlePersonDetection(content);
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
        isLearningPhase = Boolean.parseBoolean(parts[4]);
        
        if (!isLearningPhase) {
            executePredictiveControl();
        }
    }
    
    private void handleResidentState(String content) {
        String[] parts = content.split(":");
        String newLocation = parts[2];
        int hour = Integer.parseInt(parts[4]);
        
        if (isLearningPhase) {
            learnLocationTransition(previousLocation, newLocation, hour);
        }
        
        previousLocation = residentLocation;
        residentLocation = newLocation;
        residentActivity = parts[3];
        
        controlAllLights();
    }
    
    private void learnLocationTransition(String from, String to, int hour) {
        if (from.equals("Unknown") || to.equals("Unknown")) return;
        //if either location is unknown we ignore the transition
        locationTransitions.putIfAbsent(from, new HashMap<String, Integer>());
        Map<String, Integer> transitions = locationTransitions.get(from);
        transitions.put(to, transitions.getOrDefault(to, 0) + 1);
        
        hourlyLocationTransitions.putIfAbsent(Integer.valueOf(hour), new HashMap<String, Map<String, Integer>>());
        Map<String, Map<String, Integer>> hourTransitions = hourlyLocationTransitions.get(Integer.valueOf(hour));
        hourTransitions.putIfAbsent(from, new HashMap<String, Integer>());
        Map<String, Integer> fromTransitions = hourTransitions.get(from);
        fromTransitions.put(to, fromTransitions.getOrDefault(to, 0) + 1);
    }
    
    private String predictNextLocationMarkov(String currentLoc, int currentHr) {
        int nextHour = (currentHr + 1) % 24;
        
        if (hourlyLocationTransitions.containsKey(Integer.valueOf(nextHour))) {
            Map<String, Map<String, Integer>> hourTransitions = hourlyLocationTransitions.get(Integer.valueOf(nextHour));
            if (hourTransitions.containsKey(currentLoc)) {
                Map<String, Integer> transitions = hourTransitions.get(currentLoc);
                return getMostLikelyState(transitions);
            }
        }
        
        if (locationTransitions.containsKey(currentLoc)) {
            Map<String, Integer> transitions = locationTransitions.get(currentLoc);
            return getMostLikelyState(transitions);
        }
        
        return null;
    }
    
    private String getMostLikelyState(Map<String, Integer> transitions) {
        if (transitions.isEmpty()) return null;
        
        return transitions.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    private double getTransitionProbability(String from, String to) {
        if (!locationTransitions.containsKey(from)) return 0.0;
        
        Map<String, Integer> transitions = locationTransitions.get(from);
        int total = transitions.values().stream().mapToInt(Integer::intValue).sum();
        int count = transitions.getOrDefault(to, 0);
        
        return total > 0 ? (double) count / total : 0.0;
    }
    
    private void handleLearnedPattern(String content) {
        String[] parts = content.split(":");
        String patternType = parts[1];
        
        if (patternType.equals("LOCATION")) {
            int hour = Integer.parseInt(parts[2]);
            String location = parts[3];
            hourlyLocationPattern.put(Integer.valueOf(hour), location);
            
        } else if (patternType.equals("ACTIVITY")) {
            int hour = Integer.parseInt(parts[2]);
            String activity = parts[3];
            
            if (activity.equals("SHOWERING") && !showerHours.contains(Integer.valueOf(hour))) {
                showerHours.add(Integer.valueOf(hour));
                System.out.println("🏠 Learned: Shower at " + hour + ":00");
            }
        } else if (patternType.equals("BEDTIME")) {
            typicalBedtime = Integer.parseInt(parts[2]);
            System.out.println("🏠 Learned: Bedtime at " + typicalBedtime + ":00");
        }
    }
    
    private void handlePersonDetection(String content) {
        String[] parts = content.split(":");
        String personID = parts[1];
        String personType = parts[2];
        String location = parts[3];
        boolean authorized = Boolean.parseBoolean(parts[4]);
        
        if (personType.equals("STRANGER") && !authorized) {
            System.out.println("🏠 ⚠️  SECURITY ALERT: Stranger detected!");
            System.out.println("🏠 ⚠️  ID: " + personID + " in " + location);
        }
    }
    
    private void executePredictiveControl() {
        int nextHour = (currentHour + 1) % 24;
        
        // === GET BOTH PREDICTIONS ===
        String markovPrediction = predictNextLocationMarkov(residentLocation, currentHour);
        String frequencyPrediction = hourlyLocationPattern.get(Integer.valueOf(nextHour));
        
        // === SHOW COMPARISON ===
        System.out.println("\n" + "─".repeat(70));
        System.out.println("🔮 PREDICTION COMPARISON - Hour " + currentHour + ":00 → " + nextHour + ":00");
        System.out.println("📍 Current Location: " + residentLocation);
        System.out.println("─".repeat(70));
        
        // Markov prediction
        if (markovPrediction != null) {
            double confidence = getTransitionProbability(residentLocation, markovPrediction);
            System.out.println("🧠 MARKOV CHAIN:");
            System.out.println("   Prediction: " + markovPrediction);
            System.out.println("   Confidence: " + String.format("%.1f%%", confidence * 100));
            System.out.println("   Reasoning: Based on transitions from '" + residentLocation + "'");
            
            // Show transition details
            if (locationTransitions.containsKey(residentLocation)) {
                Map<String, Integer> trans = locationTransitions.get(residentLocation);
                int total = trans.values().stream().mapToInt(Integer::intValue).sum();
                System.out.println("   Transitions from " + residentLocation + ":");
                trans.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(e -> {
                        double prob = (double) e.getValue() / total;
                        System.out.println("      → " + e.getKey() + ": " + 
                            String.format("%.1f%%", prob * 100) + " (" + e.getValue() + "/" + total + ")");
                    });
            }
        } else {
            System.out.println(" MARKOV CHAIN:");
            System.out.println("   Prediction: NONE (insufficient data)");
        }
        
        System.out.println();
        
        // Frequency prediction
        if (frequencyPrediction != null) {
            System.out.println(" FREQUENCY-BASED:");
            System.out.println("   Prediction: " + frequencyPrediction);
            System.out.println("   Reasoning: Most common location at " + nextHour + ":00");
        } else {
            System.out.println(" FREQUENCY-BASED:");
            System.out.println("   Prediction: NONE (no pattern for this hour)");
        }
        
        System.out.println();
        
        // Final decision
        String finalPrediction = chooseBestPrediction(markovPrediction, frequencyPrediction);
        
        if (finalPrediction != null) {
            String agreement = "";
            if (markovPrediction != null && markovPrediction.equals(frequencyPrediction)) {
                agreement = " BOTH AGREE";
            } else if (markovPrediction != null && markovPrediction.equals(finalPrediction)) {
                agreement = " Using MARKOV (context-aware)";
            } else {
                agreement = " Using FREQUENCY (time-based)";
            }
            
            System.out.println("🎯 FINAL DECISION: " + finalPrediction + " - " + agreement);
            
            if (!finalPrediction.equals(residentLocation)) {
                System.out.println("🌡️  → Pre-adjusting climate for " + finalPrediction);
            }
        } else {
            System.out.println("🎯 FINAL DECISION: No prediction available");
        }
        
        System.out.println("─".repeat(70) + "\n");
        
        // Shower prediction
        if (showerHours.contains(Integer.valueOf(nextHour))) {
            System.out.println("🏠 🚿 PREDICT: Shower expected at " + nextHour + ":00 - Pre-heating water");
        }
        
        // Bedtime notification
        if (currentHour == typicalBedtime - 1 && !residentActivity.equals("SLEEPING")) {
            System.out.println("🏠 📢 Bedtime approaching - Sending notification");
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("resident", AID.ISLOCALNAME));
            msg.setContent("BEDTIME_ALERT");
            send(msg);
        }
    }
    
    private String chooseBestPrediction(String markov, String frequency) {
        if (markov != null && markov.equals(frequency)) {
            return markov;
        }
        if (markov != null) {
            return markov;
        }
        return frequency;
    }
    
    private void controlAllLights() {
        int currentRoomIntensity = calculateLightIntensity(residentActivity);
        
        for (String room : allRooms) {
            int intensity;
            
            if (room.equals(residentLocation)) {
                intensity = currentRoomIntensity;
            } else {
                intensity = 0;
            }
            
            ACLMessage lightMsg = new ACLMessage(ACLMessage.INFORM);
            lightMsg.addReceiver(new AID("lightcontroller", AID.ISLOCALNAME));
            lightMsg.setContent("SET_INTENSITY:" + room + ":" + intensity);
            send(lightMsg);
            
            ACLMessage guiMsg = new ACLMessage(ACLMessage.INFORM);
            guiMsg.addReceiver(new AID("guibridge", AID.ISLOCALNAME));
            guiMsg.setContent("SET_INTENSITY:" + room + ":" + intensity);
            send(guiMsg);
        }
    }
    
    private int calculateLightIntensity(String activity) {
        if (activity.equals("SLEEPING")) {
            return 0;
        } else if (activity.equals("USING_SCREEN")) {
            return 30;
        } else if (activity.equals("SHOWERING")) {
            return 80;
        } else {
            return 100;
        }
    }
    
    protected void takeDown() {
        System.out.println("\n🏠 Smart Controller shutdown");
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📊 FINAL MARKOV CHAIN - Location Transition Matrix");
        System.out.println("=".repeat(70));
        
        for (Map.Entry<String, Map<String, Integer>> entry : locationTransitions.entrySet()) {
            String from = entry.getKey();
            System.out.println("\nFrom " + from + ":");
            
            Map<String, Integer> transitions = entry.getValue();
            int total = transitions.values().stream().mapToInt(Integer::intValue).sum();
            
            transitions.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(trans -> {
                    double prob = (double) trans.getValue() / total;
                    System.out.println("  → " + trans.getKey() + ": " + 
                        String.format("%.2f", prob) + " (" + trans.getValue() + "/" + total + " transitions)");
                });
        }
        
        System.out.println("\n" + "=".repeat(70) + "\n");
    }
}