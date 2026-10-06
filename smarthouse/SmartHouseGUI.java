package smarthouse;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Smart House Visualization GUI - Updated for new system
 * Shows house layout with real-time status updates
 */
public class SmartHouseGUI extends JFrame {
    
    private static final long serialVersionUID = 1L;
	private Map<String, RoomPanel> rooms = new HashMap<>();
    private JLabel statusLabel;
    private JLabel temperatureLabel;
    private JLabel timeLabel;
    private JLabel dayLabel;
    private JLabel phaseLabel;
    
    public SmartHouseGUI() {
        setTitle(" Smart House Monitor - 30 Day Simulation");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Header panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(1200, 80));
        
        JLabel titleLabel = new JLabel(" SMART HOUSE MONITORING SYSTEM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        phaseLabel = new JLabel(" LEARNING PHASE - Days 1-7", SwingConstants.CENTER);
        phaseLabel.setFont(new Font("Arial", Font.BOLD, 18));
        phaseLabel.setForeground(Color.YELLOW);
        
        headerPanel.add(titleLabel);
        headerPanel.add(phaseLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main house layout - 2x2 grid for rooms
        JPanel housePanel = new JPanel();
        housePanel.setLayout(new GridLayout(2, 2, 15, 15));
        housePanel.setBackground(new Color(236, 240, 241));
        housePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create rooms
        rooms.put("Living Room", new RoomPanel("Living Room", new Color(52, 152, 219)));
        rooms.put("Kitchen", new RoomPanel("Kitchen", new Color(46, 204, 113)));
        rooms.put("Bedroom", new RoomPanel("Bedroom", new Color(155, 89, 182)));
        rooms.put("Bathroom", new RoomPanel("Bathroom", new Color(52, 73, 94)));
        
        housePanel.add(rooms.get("Living Room"));
        housePanel.add(rooms.get("Kitchen"));
        housePanel.add(rooms.get("Bedroom"));
        housePanel.add(rooms.get("Bathroom"));
        
        add(housePanel, BorderLayout.CENTER);
        
        // Status panel at bottom
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(4, 1, 5, 5));
        statusPanel.setBackground(new Color(44, 62, 80));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        statusPanel.setPreferredSize(new Dimension(1200, 140));
        
        dayLabel = new JLabel(" Day: 1", SwingConstants.CENTER);
        dayLabel.setForeground(Color.WHITE);
        dayLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        timeLabel = new JLabel(" Time: 00:00", SwingConstants.CENTER);
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        statusLabel = new JLabel(" Resident: Unknown", SwingConstants.CENTER);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        temperatureLabel = new JLabel("  Temperature: --°C | Thermostat: OFF", SwingConstants.CENTER);
        temperatureLabel.setForeground(Color.WHITE);
        temperatureLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        statusPanel.add(dayLabel);
        statusPanel.add(timeLabel);
        statusPanel.add(statusLabel);
        statusPanel.add(temperatureLabel);
        
        add(statusPanel, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    // Update resident location
    public void updateResidentLocation(String location) {
        SwingUtilities.invokeLater(() -> {
            // Clear all rooms
            for (RoomPanel room : rooms.values()) {
                room.setResidentPresent(false);
            }
            
            // Set resident in new location
            if (rooms.containsKey(location)) {
                rooms.get(location).setResidentPresent(true);
                statusLabel.setText(" Resident: " + location);
            }
        });
    }
    
    // Update resident activity
    public void updateResidentActivity(String activity) {
        SwingUtilities.invokeLater(() -> {
            for (RoomPanel room : rooms.values()) {
                room.setActivity(activity);
            }
        });
    }
    
    // Update light status
    public void updateLightStatus(String location, boolean isOn) {
        SwingUtilities.invokeLater(() -> {
            if (rooms.containsKey(location)) {
                rooms.get(location).setLightOn(isOn);
            }
        });
    }
    
    // Update light intensity
    public void updateLightIntensity(String location, int intensity) {
        SwingUtilities.invokeLater(() -> {
            if (rooms.containsKey(location)) {
                rooms.get(location).setLightIntensity(intensity);
            }
        });
    }
    
    // Update temperature
    public void updateTemperature(double temp, String mode) {
        SwingUtilities.invokeLater(() -> {
            temperatureLabel.setText(String.format("🌡️  Temperature: %.1f°C | Thermostat: %s", temp, mode));
        });
    }
    
    // Update time
    public void updateTime(int hour) {
        SwingUtilities.invokeLater(() -> {
            timeLabel.setText(String.format(" Time: %02d:00", hour));
        });
    }
    
    // Update day
    public void updateDay(int day) {
        SwingUtilities.invokeLater(() -> {
            dayLabel.setText(" Day: " + day);
            
            // Update phase label
            if (day <= 7) {
                phaseLabel.setText(" LEARNING PHASE - Days 1-7");
                phaseLabel.setForeground(Color.YELLOW);
            } else {
                phaseLabel.setText(" PREDICTION PHASE - Days 8-30");
                phaseLabel.setForeground(Color.GREEN);
            }
        });
    }
    
    // Inner class for room panels
    class RoomPanel extends JPanel {
        private static final long serialVersionUID = 1L;
		private Color baseColor;
        private boolean residentPresent = false;
        private boolean lightOn = false;
        private int lightIntensity = 0;
        private String activity = "";
        
        private JLabel nameLabel;
        private JLabel residentLabel;
        private JLabel lightLabel;
        private JLabel activityLabel;
        
        public RoomPanel(String name, Color color) {
            this.baseColor = color;
            
            setLayout(new BorderLayout());
            setBackground(color);
            setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3));
            
            // Room name at top
            nameLabel = new JLabel(name, SwingConstants.CENTER);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
            add(nameLabel, BorderLayout.NORTH);
            
            // Center panel with status
            JPanel centerPanel = new JPanel();
            centerPanel.setOpaque(false);
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            
            residentLabel = new JLabel("", SwingConstants.CENTER);
            residentLabel.setFont(new Font("Arial", Font.BOLD, 60));
            residentLabel.setForeground(Color.WHITE);
            residentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            activityLabel = new JLabel("", SwingConstants.CENTER);
            activityLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            activityLabel.setForeground(Color.WHITE);
            activityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            lightLabel = new JLabel(" OFF", SwingConstants.CENTER);
            lightLabel.setFont(new Font("Arial", Font.BOLD, 20));
            lightLabel.setForeground(Color.WHITE);
            lightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            centerPanel.add(Box.createVerticalGlue());
            centerPanel.add(residentLabel);
            centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            centerPanel.add(activityLabel);
            centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            centerPanel.add(lightLabel);
            centerPanel.add(Box.createVerticalGlue());
            
            add(centerPanel, BorderLayout.CENTER);
        }
        
        public void setResidentPresent(boolean present) {
            this.residentPresent = present;
            updateDisplay();
        }
        
        public void setActivity(String activity) {
            this.activity = activity;
            updateDisplay();
        }
        
        public void setLightOn(boolean on) {
            this.lightOn = on;
            updateDisplay();
        }
        
        public void setLightIntensity(int intensity) {
            this.lightIntensity = intensity;
            this.lightOn = intensity > 0;
            updateDisplay();
        }
        
        private void updateDisplay() {
            if (residentPresent) {
                residentLabel.setText("👤");
                activityLabel.setText(activity);
                setBackground(baseColor.brighter());
                setBorder(BorderFactory.createLineBorder(Color.YELLOW, 6));
            } else {
                residentLabel.setText("");
                activityLabel.setText("");
                setBackground(baseColor);
                setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3));
            }
            
            if (lightOn) {
                if (lightIntensity > 0) {
                    lightLabel.setText("Light ON (" + lightIntensity + "%)");
                } else {
                    lightLabel.setText("Light ON");
                }
                lightLabel.setForeground(Color.YELLOW);
            } else {
                lightLabel.setText("Light OFF");
                lightLabel.setForeground(Color.GRAY);
            }
        }
    }
    
    // Singleton instance
    private static SmartHouseGUI instance;
    
    public static SmartHouseGUI getInstance() {
        if (instance == null) {
            instance = new SmartHouseGUI();
        }
        return instance;
    }
}