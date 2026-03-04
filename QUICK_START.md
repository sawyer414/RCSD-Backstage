# Quick Start Guide - PS4 Robot Control System

## 60-Second Setup

### 1. **PC Setup** (5 min)

```bash
# Build the application
cd Backstage
./gradlew build

# Run the server (make sure PS4 controller is connected)
java -jar build/libs/Backstage-1.0-SNAPSHOT.jar
```

You should see:
```
INFO - Waiting for Raspberry Pi connections on port 5555
INFO - Controller started - waiting for PS4 controller input
```

### 2. **Raspberry Pi Setup** (1 min)

```bash
# Find your PC's IP address and run:
java -cp Backstage-1.0-SNAPSHOT.jar org.example.RaspberryPiMotorClient YOUR_PC_IP 5555
```

Example:
```bash
java -cp Backstage-1.0-SNAPSHOT.jar org.example.RaspberryPiMotorClient 192.168.1.100 5555
```

### 3. **Test Movement**

- **Left joystick up** → Left motor moves forward
- **Right joystick up** → Right motor moves forward
- **Left joystick down** → Left motor moves backward
- **Right joystick down** → Right motor moves backward
- **Move joysticks independently** → Tank-drive style turning

## What Each Joystick Controls

```
        PS4 Controller
     
    Left Joystick          Right Joystick
    Controls LEFT          Controls RIGHT
    Motor Speed            Motor Speed
         
    Up = Forward           Up = Forward
    Down = Backward        Down = Backward
    (Y-axis only)          (Y-axis only)
```

## Typical Robot Movement

| Joystick Position | Robot Behavior |
|---|---|
| Both up | Move forward |
| Both down | Move backward |
| Left up, Right down | Spin right |
| Left down, Right up | Spin left |
| Left up (partial), Right 0 | Curve right |
| Left 0, Right up (partial) | Curve left |

## Troubleshooting in 3 Steps

### Issue: PS4 Controller Not Detected

```bash
# Check if controller is connected
# Try pressing PS button on controller
# Restart application
```

### Issue: Raspberry Pi Cannot Connect

```bash
# 1. Find your PC's IP address
ipconfig  # Windows
ifconfig  # Linux/Mac

# 2. Verify network connection
ping <YOUR_PC_IP>

# 3. Test port accessibility
telnet <YOUR_PC_IP> 5555

# 4. Check Windows Firewall - Allow Java through firewall
```

### Issue: Motors Not Responding

If using real motors (Pi4J):
- Check motor driver connections
- Verify GPIO pin numbers in your MotorController
- Test pins individually with GPIO tools

If using mock motors:
- Check console output for debug messages
- Verify joystick input is being registered

## File Structure

```
RCSD-Backstage/
├── Backstage/
│   ├── src/main/java/org/example/
│   │   ├── Main.java                      ← Start here on PC
│   │   ├── RobotController.java           ← Joystick mapping
│   │   ├── PS4Controller.java             ← Controller input
│   │   ├── RobotNetworkServer.java        ← Network server
│   │   ├── NetworkRobotImpl.java           ← Network robot
│   │   ├── RaspberryPiMotorClient.java    ← Start here on RPi
│   │   └── ControlConfig.java             ← Configuration
│   └── build.gradle.kts
├── RASPBERRY_PI_SETUP.md                  ← Detailed setup
├── NETWORK_PROTOCOL.md                    ← Protocol details
└── README.md
```

## Configuration Files

### ControlConfig.java - Adjust Sensitivity

```java
public static final float DEAD_ZONE = 0.15f;      // 15% stick threshold
public static final float SENSITIVITY = 1.0f;     // 100% responsiveness
public static final int POLL_RATE_MS = 16;        // ~60 Hz update rate
```

## Commands to Remember

### On PC

```bash
# Navigate to project
cd Backstage

# Build application
./gradlew build

# Run with default port (5555)
java -jar build/libs/Backstage-1.0-SNAPSHOT.jar

# Run with custom port (e.g., 9000)
java -jar build/libs/Backstage-1.0-SNAPSHOT.jar 9000
```

### On Raspberry Pi

```bash
# Connect to server with default port
java -cp Backstage-1.0-SNAPSHOT.jar org.example.RaspberryPiMotorClient 192.168.1.100 5555

# Connect with custom port
java -cp Backstage-1.0-SNAPSHOT.jar org.example.RaspberryPiMotorClient 192.168.1.100 9000
```

## Network Connection Checklist

- [ ] PC and Raspberry Pi on same network
- [ ] PS4 controller connected to PC (USB or wireless)
- [ ] Server running on PC on port 5555 (or custom port)
- [ ] Client running on Raspberry Pi pointing to PC IP
- [ ] Firewall allows Java application (Windows)
- [ ] Motors wired correctly to Raspberry Pi GPIO
- [ ] Motor power supply connected

## Motor Wiring Quick Reference

For L298N Motor Driver:

```
RPi GPIO 17  → L298N IN1  (Left Motor Forward)
RPi GPIO 27  → L298N IN2  (Left Motor Backward)
RPi GPIO 12  → L298N ENA  (Left Motor Speed - PWM)

RPi GPIO 22  → L298N IN3  (Right Motor Forward)
RPi GPIO 23  → L298N IN4  (Right Motor Backward)
RPi GPIO 13  → L298N ENB  (Right Motor Speed - PWM)

RPi GND      → L298N GND
```

*Note: Adjust GPIO pins in your MotorController implementation based on your actual wiring.*

## Performance Tips

1. **Reduce Latency**: Use wired Ethernet on Raspberry Pi
2. **Improve Responsiveness**: Reduce `DEAD_ZONE` in ControlConfig.java
3. **Smoother Control**: Adjust `SENSITIVITY` value (0.5-2.0)
4. **Better Stability**: Check WiFi signal strength on Raspberry Pi

## Next Steps

1. Read detailed setup guide: [RASPBERRY_PI_SETUP.md](./RASPBERRY_PI_SETUP.md)
2. Understand network protocol: [NETWORK_PROTOCOL.md](./NETWORK_PROTOCOL.md)
3. Customize MotorController for your specific hardware
4. Fine-tune sensitivity and dead zones
5. Add additional features (sensors, status reporting, etc.)

## Support

For issues or questions:
1. Check console output for error messages
2. Enable debug logging in ControlConfig.java
3. Review RASPBERRY_PI_SETUP.md troubleshooting section
4. Verify network connectivity with `ping` commands

---

**Happy Robot Controlling!** 🤖

