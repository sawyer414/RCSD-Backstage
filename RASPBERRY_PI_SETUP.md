# PS4 Controller Robot Control System - Raspberry Pi 5 Network Setup

## Overview

This system allows you to control a robot using a PS4 controller connected to your PC/laptop. The controller input is broadcast over a network to a Raspberry Pi 5 running the motor controller client, which drives the robot's motors independently.

## Architecture

```
PC/Laptop (PS4 Controller) 
    ↓ (PS4 Input)
[Main Application - RobotNetworkServer]
    ↓ (TCP Network Broadcast Port 5555)
Raspberry Pi 5 [RaspberryPiMotorClient]
    ↓ (GPIO/PWM Control)
Robot Motors (Left & Right)
```

## Features

- **Independent Motor Control**: Each joystick on the PS4 controller controls one side of the robot independently
  - **Left Joystick Y-axis** → Controls **Left Motor**
  - **Right Joystick Y-axis** → Controls **Right Motor**
  
- **Network Communication**: Broadcasts commands over TCP to all connected Raspberry Pi clients
- **Multi-Client Support**: Connect multiple robots to one controller
- **Cross-Platform**: Works on Windows, Linux, and macOS

## Prerequisites

### On Your PC/Laptop
- Java 11 or higher
- PS4 Controller (connected via USB or wireless adapter)
- Network connection to Raspberry Pi 5

### On Raspberry Pi 5
- Raspberry Pi OS (latest)
- Java 11 or higher
  ```bash
  sudo apt-get install openjdk-11-jdk
  ```
- Pi4J library (for GPIO/PWM control) - **OPTIONAL** for testing without actual motors
- Network connection to your PC/laptop

## Controller Mapping

| Input | Function |
|-------|----------|
| **Left Joystick Y-Axis** | Left Motor Speed (-1.0 = full reverse, 0 = stop, 1.0 = full forward) |
| **Right Joystick Y-Axis** | Right Motor Speed (-1.0 = full reverse, 0 = stop, 1.0 = full forward) |
| **Cross (X)** | Jump/Special Action 1 |
| **Circle (O)** | Action 2 |
| **Square** | Action 3 |
| **Triangle** | Action 4 |
| **L1** | Boost |
| **R1** | Strafe |
| **Options** | Stop Robot |
| **PS Button** | Not assigned (can be customized) |
| **Left/Right Triggers** | Acceleration/Deceleration (configurable) |

## Setup Instructions

### Step 1: Build the Application on Your PC

```bash
cd RCSD-Backstage/Backstage
./gradlew build
```

The JAR file will be created at: `build/libs/Backstage-1.0-SNAPSHOT.jar`

### Step 2: Run the Network Server on Your PC

```bash
# Using default port 5555
java -jar build/libs/Backstage-1.0-SNAPSHOT.jar

# Using custom port (e.g., 9000)
java -jar build/libs/Backstage-1.0-SNAPSHOT.jar 9000
```

Expected output:
```
INFO - PS4 Controller Robot Control System
INFO - Mode: Network-based (Raspberry Pi 5 Compatible)
INFO - Network robot created successfully on port 5555
INFO - Waiting for Raspberry Pi connections on port 5555
INFO - Controller started - waiting for PS4 controller input
```

**Note**: Make sure your PS4 controller is connected before running the server.

### Step 3: Set Up Raspberry Pi 5

#### Option A: Using Mock Motor Controller (Testing without actual motors)

```bash
# Copy the Backstage JAR to Raspberry Pi
scp Backstage/build/libs/Backstage-1.0-SNAPSHOT.jar pi@<RASPBERRY_PI_IP>:~/

# SSH into Raspberry Pi
ssh pi@<RASPBERRY_PI_IP>

# Run the motor client (replace PC_IP with your laptop's IP address)
java -cp Backstage-1.0-SNAPSHOT.jar org.example.RaspberryPiMotorClient <PC_IP> 5555
```

#### Option B: Using Pi4J for Real Motor Control

1. **Install Pi4J**:
```bash
curl -s https://pi4j.com/install | sudo bash
```

2. **Create a custom MotorController implementation**:

Create a file `Pi4JMotorController.java`:

```java
package org.example;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.pwm.Pwm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pi4JMotorController implements RaspberryPiMotorClient.MotorController {
    private static final Logger logger = LoggerFactory.getLogger(Pi4JMotorController.class);
    
    // GPIO Pin numbers (adjust based on your wiring)
    private static final int LEFT_MOTOR_FORWARD_PIN = 17;
    private static final int LEFT_MOTOR_BACKWARD_PIN = 27;
    private static final int LEFT_MOTOR_PWM_PIN = 12;
    
    private static final int RIGHT_MOTOR_FORWARD_PIN = 22;
    private static final int RIGHT_MOTOR_BACKWARD_PIN = 23;
    private static final int RIGHT_MOTOR_PWM_PIN = 13;
    
    private Context pi4j;
    private DigitalOutput leftForward, leftBackward, rightForward, rightBackward;
    private Pwm leftPwm, rightPwm;
    
    public Pi4JMotorController() {
        try {
            pi4j = Pi4J.newAutoContext();
            setupMotors();
        } catch (Exception e) {
            logger.error("Failed to initialize Pi4J", e);
        }
    }
    
    private void setupMotors() {
        // Configure GPIO pins for direction control
        // Configure PWM pins for speed control
        // (Implementation depends on your specific motor driver)
        logger.info("Motors initialized");
    }
    
    @Override
    public void setMotorSpeed(int motorId, float speed) {
        if (motorId == LEFT_MOTOR) {
            setLeftMotor(speed);
        } else if (motorId == RIGHT_MOTOR) {
            setRightMotor(speed);
        }
    }
    
    private void setLeftMotor(float speed) {
        // Implement actual motor control using Pi4J
        logger.info("Left Motor: {}", String.format("%.2f", speed));
    }
    
    private void setRightMotor(float speed) {
        // Implement actual motor control using Pi4J
        logger.info("Right Motor: {}", String.format("%.2f", speed));
    }
}
```

Then run with:
```bash
java -cp Backstage-1.0-SNAPSHOT.jar org.example.RaspberryPiMotorClient <PC_IP> 5555
```

## Network Configuration

### Finding Your PC's IP Address

**Windows**:
```cmd
ipconfig
```
Look for "IPv4 Address" (usually starts with 192.168.x.x)

**Linux/Mac**:
```bash
ifconfig
# or
hostname -I
```

### Testing Network Connection

From Raspberry Pi:
```bash
ping <YOUR_PC_IP>
```

### Firewall Configuration

**Windows Firewall** - Allow Java application through firewall:
1. Open Windows Defender Firewall
2. Allow Java (or specific port 5555) through firewall

**Linux** (if applicable):
```bash
sudo ufw allow 5555/tcp
```

## Troubleshooting

### PS4 Controller Not Detected
- Ensure controller is connected via USB or wireless adapter
- Try pressing the PS button to wake the controller
- Restart the application

### Cannot Connect to Server
1. Verify network connectivity: `ping <SERVER_IP>`
2. Check firewall settings on both machines
3. Verify correct IP address and port number
4. Check if server is running and accessible:
   ```bash
   telnet <SERVER_IP> 5555
   ```

### Raspberry Pi Disconnects Frequently
- Check network stability and WiFi signal strength
- Ensure both devices are on the same network
- Check system logs: `journalctl -u backstage -f`

### Motors Not Moving
- Verify motor wiring connections
- Check GPIO pin configuration matches your hardware
- Ensure adequate power supply to motors
- Test individual motor pins manually

## Advanced Configuration

### Adjusting Sensitivity

Edit `ControlConfig.java`:
```java
public static final float SENSITIVITY = 1.0f;  // Increase for more responsive, decrease for smoother control
public static final float DEAD_ZONE = 0.15f;   // Increase to reduce stick drift
```

### Custom Motor Mapping

In `RobotController.java`:
```java
// Customize axis mapping
case PS4Controller.AXIS_LEFT_STICK_Y:
    handleLeftMotor(-value);  // Modify the value here
    break;
```

## Performance Tips

1. **Reduce Network Latency**:
   - Use wired connection on Raspberry Pi if possible
   - Keep devices close to WiFi router

2. **Optimize Motor Response**:
   - Adjust `ACCELERATION_RATE` in `ControlConfig.java`
   - Tune `DEAD_ZONE` to eliminate stick drift

3. **Monitor Performance**:
   - Enable debug logging in `ControlConfig.java`
   - Check network latency: `ping -c 10 <SERVER_IP>`

## Motor Wiring Guide (Example L298N Motor Driver)

```
Raspberry Pi GPIO → L298N Motor Driver → Robot Motors

GPIO 17 (Left Forward)  → IN1
GPIO 27 (Left Backward) → IN2
GPIO 12 (Left PWM)      → ENA

GPIO 22 (Right Forward)  → IN3
GPIO 23 (Right Backward) → IN4
GPIO 13 (Right PWM)      → ENB

GND → GND
5V  → +5V
```

## Support & Documentation

- Pi4J Documentation: https://pi4j.com/
- PS4 Controller Java API: Use JInput library
- Raspberry Pi Documentation: https://www.raspberrypi.com/documentation/

## License

This project is provided as-is for educational and personal robotics use.

