# PS4 Controller Robot Control - Quick Reference Card

## 🚀 Running the Application

```bash
# Build
cd Backstage
./gradlew build

# Run
./gradlew run

# Or run JAR directly
java -jar build/libs/Backstage-1.0-SNAPSHOT.jar
```

## 🎮 Controller Inputs

### Movement
- **Left Stick Up/Down** → Robot moves forward/backward
- **Left Stick Left/Right** → Robot rotates in place
- **Right Stick Left/Right** → Robot strafes left/right

### Actions
- **X Button** → Jump/Custom Action
- **O Button** → Action 1
- **Square Button** → Action 2
- **Triangle Button** → Action 3
- **L1 Button** → Boost
- **R1 Button** → Strafe mode
- **L2/R2 Triggers** → Custom actions
- **Options Button** → Stop Robot

## 📁 File Map

| File | Purpose |
|------|---------|
| `Main.java` | Application entry point |
| `PS4Controller.java` | Low-level controller polling |
| `ControllerListener.java` | Event interface |
| `RobotController.java` | Input → Robot mapper |
| `Robot.java` | Robot interface |
| `RobotImpl.java` | Basic robot implementation |
| `ControlConfig.java` | Configuration settings |
| `SerialRobotExample.java` | Hardware integration template |

## ⚙️ Configuration (ControlConfig.java)

```java
// Sensitivity and Dead Zone
DEAD_ZONE = 0.15f;        // 15% stick drift tolerance
SENSITIVITY = 1.0f;       // Input multiplier

// Poll Rate
POLL_RATE_MS = 16;        // ~60 Hz refresh rate

// Speed Limits
MAX_FORWARD_SPEED = 1.0f; // 100%
MAX_ROTATION_SPEED = 1.0f;
MAX_STRAFE_SPEED = 1.0f;
```

## 🔌 Hardware Integration

### Option 1: Serial Port (Arduino/Embedded)
```java
// In Main.java, replace:
Robot robot = new RobotImpl();

// With:
Robot robot = new SerialRobotExample("COM3", 9600);
```

### Option 2: GPIO (Raspberry Pi)
1. Create `RPiRobot` implementing `Robot` interface
2. Use Pi4j library for GPIO control
3. Implement motor control methods

### Option 3: Network (ROS/Networked Robot)
1. Create `NetworkRobot` implementing `Robot` interface
2. Send commands via TCP/UDP sockets
3. Handle sensor feedback

## 🐛 Debugging

Enable debug logging by modifying `ControlConfig.java`:
```java
public static final boolean DEBUG_LOGGING = true;
```

Then check console output for all controller inputs.

## 📊 Movement Control Logic

```
Left Stick Input → RobotController.handleMovement()
  ↓
Apply Dead Zone (15% by default)
  ↓
Apply Sensitivity (1.0x by default)
  ↓
Send to Robot.move(leftVelocity, rightVelocity)
  ↓
RobotImpl sends to hardware (serial/GPIO/network)
```

## 🛠️ Common Customizations

### Change Button Action
```java
// RobotController.onButtonPressed()
case PS4Controller.BUTTON_CROSS:
    robot.performAction("your_action");
    break;
```

### Change Stick Response
```java
// RobotController.handleMovement()
// Add acceleration curves, scaling, etc.
```

### Add Trigger Control
```java
// RobotController.handleLeftTrigger()
// Implement trigger-based actions
```

## ✅ Checklist

- [ ] PS4 controller connected (USB or wireless adapter)
- [ ] `./gradlew build` completes successfully
- [ ] Application starts with `./gradlew run`
- [ ] Controller inputs appear in console logs
- [ ] Robot hardware connected and powered
- [ ] Serial port configured (if using SerialRobotExample)
- [ ] Motor commands tested independently
- [ ] Sensitivity and dead zone tuned
- [ ] Safety features implemented (if needed)
- [ ] Autonomous/telemetry features added (if needed)

## 🎯 Architecture Layers

```
User Input (PS4 Controller)
        ↓
PS4Controller (Polling, event generation)
        ↓
RobotController (Input mapping, filtering)
        ↓
Robot Interface (Abstraction layer)
        ↓
Robot Implementation (Hardware communication)
        ↓
Physical Hardware (Motors, sensors)
```

## 📚 Key Constants

```java
// Button IDs (PS4Controller)
BUTTON_CROSS = 0
BUTTON_CIRCLE = 1
BUTTON_SQUARE = 2
BUTTON_TRIANGLE = 3
BUTTON_L1 = 4
BUTTON_R1 = 5
BUTTON_OPTIONS = 9

// Axis IDs (PS4Controller)
AXIS_LEFT_STICK_X = 0
AXIS_LEFT_STICK_Y = 1
AXIS_RIGHT_STICK_X = 2
AXIS_RIGHT_STICK_Y = 3
AXIS_L2_TRIGGER = 4
AXIS_R2_TRIGGER = 5
```

## 🚨 If Something Goes Wrong

1. **Controller not detected**
   - Check USB/wireless connection
   - Restart application
   - Verify in Device Manager

2. **Build fails**
   ```bash
   ./gradlew clean build --refresh-dependencies
   ```

3. **No motor movement**
   - Verify RobotImpl connects to hardware
   - Check serial port in SerialRobotExample
   - Test with debug logging enabled

4. **Laggy input**
   - Reduce POLL_RATE_MS in ControlConfig
   - Check system resources
   - Try USB connection instead of wireless

---

**Ready to go!** Run `./gradlew run` and start controlling your robot! 🤖

