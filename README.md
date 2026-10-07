# Smart House Simulation with JADE

A Java smart-house simulation built with the JADE multi-agent framework. Agents represent a resident, guests, motion and temperature sensors, lighting and climate controllers, and a simulation clock. They exchange messages to update the graphical interface, adjust lighting according to the resident's activity, monitor temperature, and report unauthorized strangers. The system also observes the resident's behavior and compares Markov-based movement predictions with hourly location patterns.

## Requirements

- A Java Development Kit (JDK), with `java` and `javac` available in your terminal. Java 21 is used in the current setup.
- A desktop environment for the graphical interface.
- The JADE library at `jade/jade.jar`, included in the repository.

Eclipse is optional.

## Clone and run

```sh
git clone https://github.com/OYAsci/Smart-House-Simulation-Using-a-Multi-Agent-System-JADE-.git
cd Smart-House-Simulation-Using-a-Multi-Agent-System-JADE
```

Run the following commands from the repository root, which contains the `smarthouse` and `jade` folders.

### Windows (PowerShell)

Run the existing compiled classes:

```powershell
java -cp ".;jade\jade.jar" smarthouse.SmartHouseLauncher
```

To rebuild from source, especially after making changes:

```powershell
New-Item -ItemType Directory -Force bin | Out-Null
javac -encoding UTF-8 -cp "jade\jade.jar" -d bin .\smarthouse\*.java .\smarthouse\agents\*.java
```

After successful compilation, run:

```powershell
java -cp "bin;jade\jade.jar" smarthouse.SmartHouseLauncher
```

### Linux or macOS

Run the existing compiled classes:

```sh
java -cp ".:jade/jade.jar" smarthouse.SmartHouseLauncher
```

Or rebuild and run:

```sh
mkdir -p bin
javac -encoding UTF-8 -cp "jade/jade.jar" -d bin smarthouse/*.java smarthouse/agents/*.java
java -cp "bin:jade/jade.jar" smarthouse.SmartHouseLauncher
```

The `.java` files contain editable source code; `.class` files contain executable Java bytecode. Keep the accompanying `$1.class`, `$2.class`, and other nested-class files when using the existing compiled version.

## Prediction methods

| Method | Implementation | Purpose |
| --- | --- | --- |
| Markov-based prediction | `SmartControllerAgent` | Counts transitions between rooms during learning. Predicts the most frequent next room using transitions associated with the next hour, with overall transitions as a fallback. |
| Hourly frequency analysis | `PatternAnalyzerAgent` | Counts resident location reports by hour and selects the most frequently reported room for each hour. |
| Prediction selection | `SmartControllerAgent` | Prints both predictions and prefers the Markov result when available; otherwise uses the hourly frequency result. |

Transition probabilities are calculated as the number of observations of a particular room transition divided by all recorded transitions from its starting room.

**Resident and guest behavior:** resident movements come from a programmed daily schedule with random variations, and the prediction methods learn from the resulting reports. Guests announce their presence and move when they receive `GOTO` commands. The supplied implementation does not learn or predict guest movements; known guests and unauthorized strangers are distinguished using their configured type.

## How it works

1. `TimeManagerAgent` advances a 30-day simulation. Days 1–7 are marked as learning, and days 8–30 enable prediction. Ordinary hours use a one-second delay; selected active hours use five seconds in the supplied code.
2. `ResidentAgent` updates its room and activity and broadcasts a state report whenever either changes. The pattern analyzer, smart controller, motion sensors, and GUI bridge receive these reports.
3. The analyzer summarizes hourly location reports, while the smart controller collects transition counts during learning. After learning, the controller compares the two prediction methods on time updates.
4. The smart controller sends lighting intensities to the light controller and GUI bridge: sleeping uses 0, screen use 30, showering 80, and other activities 100. Rooms other than the resident's current room receive 0.
5. The temperature sensor generates small random temperature changes. The thermostat selects heating below 20.5°C, cooling above 21.5°C, and off within that range. The GUI bridge forwards state updates to the interface.
6. Guest arrivals can produce security alerts for unauthorized strangers, and the controller can send a bedtime reminder to the resident.
7. Agent communication : 

| Sender | Receiver | Example message |
|---|---|---|
| `ResidentAgent` | `MotionSensorAgent` | `RESIDENT_STATE` |
| `MotionSensorAgent` | `LightControllerAgent` | `MOTION_DETECTED:<location>` |
| `TemperatureSensorAgent` | `ThermostatAgent` | `TEMPERATURE:<value>` |
| `TemperatureSensorAgent` | `GUIBridgeAgent` | `TEMPERATURE:<value>` |
| `ThermostatAgent` | `GUIBridgeAgent` | `THERMOSTAT:<temp>:<mode>` |
| `GuestAgent` | `SmartControllerAgent` | `PERSON_DETECTED:<id>:<type>:<location>:<authorized>` |
| `PatternAnalyzerAgent` | `SmartControllerAgent` | Learned resident patterns |

## Current limitations

- The supplied smart controller records transitions using a lagging `previousLocation` value. This should be corrected to use `residentLocation` before updating the state for accurate consecutive-room learning.
- Hourly patterns count state-change reports, rather than continuous occupancy. Displayed Markov confidence uses overall counts even when an hourly transition table supplies the prediction.
- Bedtime is fixed at 23:00. Activity-pattern counts are not populated, so shower prediction is not fully connected.
- Predictive climate adjustment and water preheating are logged intentions; the smart controller does not issue corresponding thermostat commands.

If Java cannot find the launcher, check that you are in the repository root and that the classpath matches your compiled output. If Java cannot find JADE classes, check that `jade/jade.jar` exists.
