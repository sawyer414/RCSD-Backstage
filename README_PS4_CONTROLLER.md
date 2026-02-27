# PS4 Controller Robot Control System

A Java application for controlling robots using a PlayStation 4 (PS4) wireless controller.

## Features

- **PS4 Controller Support**: Full input support for PS4 controllers via JInput
- **Real-time Control**: Low-latency polling at ~60 Hz
- **Analog Input**: Support for analog sticks, triggers, and buttons
- **Robot Interface**: Extensible robot control interface for various robot platforms
- **Logging**: Comprehensive SLF4J logging for debugging

## Prerequisites

- **Java**: JDK 11 or higher
- **PS4 Controller**: Connected via USB or wireless adapter
- **Gradle**: Included with the project (use `gradlew`)

## Project Structure

```
src/
├── main/java/org/example/
│   ├── Main.java                 # Entry point
│   ├── PS4Controller.java        # PS4 controller input handling
│   ├── ControllerListener.java   # Controller event interface
│   ├── Robot.java                # Robot control interface
│   ├── RobotImpl.java             # Default robot implementation
│   └── RobotController.java      # Bridges controller to robot
```

## Building the Project

```bash
cd Backstage
./gradlew build
```

## Running the Application

```bash
cd Backstage
./gradlew run
```

Or run the JAR directly:
```bash
java -jar build/libs/Backstage-1.0-SNAPSHOT.jar
```

## PS4 Controller Input Mapping

### Buttons
| Button | Action |
|--------|--------|
| **X (Cross)** | Custom action (jump) |
| **O (Circle)** | Custom action 1 |
| **□ (Square)** | Custom action 2 |
| **△ (Triangle)** | Custom action 3 |
| **L1** | Boost |
| **R1** | Strafe |
| **Options** | Stop robot |

### Analog Inputs
| Input | Function |
|-------|----------|
| **Left Stick X** | Rotation (left/right) |
| **Left Stick Y** | Forward/Backward movement |
| **Right Stick X** | Strafing |
| **Right Stick Y** | Reserved for future use |
| **L2 Trigger** | Custom action |
| **R2 Trigger** | Custom action |

## Architecture

### PS4Controller
Handles low-level PS4 controller polling and event generation using JInput library.

```java
PS4Controller controller = new PS4Controller(listener);
controller.start();  // Begin polling
controller.stop();   // Stop polling
```

### ControllerListener
Interface for receiving controller events:
- `onButtonPressed(int buttonId)`
- `onButtonReleased(int buttonId)`
- `onAxisMotion(int axisId, float value)`
- `onControllerDisconnected()`

### Robot Interface
Extensible interface for robot control:
- `move(double leftVelocity, double rightVelocity)` - Tank drive control
- `rotate(double angularVelocity)` - In-place rotation
- `stop()` - Stop all motion
- `performAction(String action)` - Special actions

### RobotController
Bridges PS4 controller input to robot commands:
- Maps stick movements to differential drive commands
- Maps buttons to special actions
- Handles analog trigger inputs
- Includes dead zone filtering (15%) and sensitivity control

## Customization

### Custom Robot Implementation
Extend the `Robot` interface or modify `RobotImpl.java`:

```java
public class CustomRobot implements Robot {
    @Override
    public void move(double leftVelocity, double rightVelocity) {
        // Send commands to your robot
        // Example: SendMotorCommand(LEFT_MOTOR, leftVelocity);
        //          SendMotorCommand(RIGHT_MOTOR, rightVelocity);
    }
    
    @Override
    public void stop() {
        // Stop your robot
    }
    
    // Implement other methods...
}
```

### Custom Button Mapping
Modify `RobotController.onButtonPressed()` to map buttons to custom actions:

```java
case PS4Controller.BUTTON_CROSS:
    robot.performAction("your_custom_action");
    break;
```

### Adjust Sensitivity
Edit `RobotController.java`:
```java
private static final float SENSITIVITY = 1.0f;      // Increase for more responsive
private static final float DEAD_ZONE = 0.15f;       // Adjust dead zone threshold
```

## Troubleshooting

### Controller Not Detected
- Ensure PS4 controller is connected via USB or wireless adapter
- Try reconnecting the controller
- Check Windows Device Manager to verify controller is recognized
- For wireless, ensure the wireless adapter is securely connected

### No Movement
- Verify the robot implementation (`RobotImpl`) is communicating with your hardware
- Check logs for controller events
- Ensure controller sticks are not stuck in the dead zone
- Try pressing buttons to confirm controller is responding

### Compilation Issues
- Ensure JDK 11+ is installed
- Run `./gradlew clean build`
- Clear gradle cache if needed: `./gradlew clean --refresh-dependencies`

## Dependencies

- **JInput 2.0.10**: Game controller input library
- **SLF4J 2.0.9**: Logging framework

## Quick Start Example

1. **Connect your PS4 controller** via USB or wireless adapter

2. **Build the project**:
   ```bash
   cd Backstage
   ./gradlew build
   ```

3. **Run the application**:
   ```bash
   ./gradlew run
   ```

4. **Control your robot**:
   - Left stick: Move forward/backward and rotate
   - Right stick: Strafe side to side
   - Buttons: Trigger special actions
   - Options button: Stop the robot

## Implementation Notes

- The `RobotImpl` class contains TODO comments for connecting to actual robot hardware
- Extend the system with serial communication, network connections, or hardware APIs as needed
- Consider adding rate limiting and safety checks for your specific robot platform
- Controller events are logged at DEBUG level - enable debug logging to see all inputs

## License

This project is for RCSD backstage automation.

