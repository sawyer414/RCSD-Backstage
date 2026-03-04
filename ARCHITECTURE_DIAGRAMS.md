# System Architecture & Wiring Diagrams

## System Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          YOUR PC / LAPTOP                           │
│                                                                     │
│  ┌──────────────────┐       ┌──────────────────┐                  │
│  │  PS4 Controller  │──────→│  JInput Library  │                  │
│  │  (USB/Wireless)  │       │  (PS4Controller) │                  │
│  └──────────────────┘       └────────┬─────────┘                  │
│                                      │                            │
│                    ┌─────────────────▼─────────────────┐          │
│                    │  RobotController                 │          │
│                    │  (Maps Joystick to Motors)       │          │
│                    │  - Left Y → Left Motor           │          │
│                    │  - Right Y → Right Motor         │          │
│                    └─────────────────┬─────────────────┘          │
│                                      │                            │
│                    ┌─────────────────▼─────────────────┐          │
│                    │  NetworkRobotImpl                 │          │
│                    │  (Converts to JSON)              │          │
│                    └─────────────────┬─────────────────┘          │
│                                      │                            │
│                    ┌─────────────────▼─────────────────┐          │
│                    │  RobotNetworkServer               │          │
│                    │  (Broadcasts to Raspberry Pi)     │          │
│                    │  TCP Port: 5555                  │          │
│                    └─────────────────┬─────────────────┘          │
│                                      │                            │
└──────────────────────────────────────┼────────────────────────────┘
                                       │
                      ┌────────────────┴────────────────┐
                      │   Network (WiFi/Ethernet)      │
                      │   TCP/IP - Port 5555           │
                      │   JSON Messages                │
                      └────────────────┬────────────────┘
                                       │
┌──────────────────────────────────────┼────────────────────────────┐
│              RASPBERRY PI 5                                       │
│                                      │                            │
│                    ┌─────────────────▼─────────────────┐          │
│                    │  RaspberryPiMotorClient          │          │
│                    │  (Receives JSON Commands)        │          │
│                    │  (Controls Motors)               │          │
│                    └─────────────────┬─────────────────┘          │
│                                      │                            │
│                    ┌─────────────────▼─────────────────┐          │
│                    │  MotorController Interface        │          │
│                    │  (Custom Implementation)         │          │
│                    │  - setMotorSpeed(motorId, speed)│          │
│                    └─────────────────┬─────────────────┘          │
│                                      │                            │
│           ┌──────────────────────────┼──────────────────────────┐ │
│           │                          │                          │ │
│    ┌──────▼──────┐         ┌─────────▼────────┐      ┌──────────▼──┐
│    │   Motor     │         │   Motor Driver   │      │  GPIO/PWM   │
│    │  Left       │◄────────│   (L298N etc)    │◄─────│  Interface  │
│    │             │         │                  │      │  (Pi4J)     │
│    └─────────────┘         │                  │      └─────────────┘
│                            │                  │
│    ┌─────────────┐         │                  │
│    │   Motor     │◄────────│                  │
│    │  Right      │         │                  │
│    └─────────────┘         └──────────────────┘
│           │                          │
│           │        ┌─────────────────┤
│           │        │                 │
│           └─┬──────┴─────┬───────────┘
│             │           │
│        ┌────▼─┐  ┌──────▼──┐
│        │ Left │  │  Right  │
│        │Wheel │  │  Wheel  │
│        └──────┘  └─────────┘
│
│              ROBOT
└────────────────────────────────────────────────────────────────────┘
```

## Signal Flow Diagram

```
USER ACTION: Push Left Joystick Forward
        ↓
   Raw Value: -0.95 (negative because Y-axis is inverted)
        ↓
   PS4Controller detects event
        ↓
   onAxisMotion(AXIS_LEFT_STICK_Y, -0.95)
        ↓
   RobotController.onAxisMotion()
        ↓
   Apply Dead Zone: |0.95| > 0.15 ✓
        ↓
   Apply Sensitivity: 0.95 × 1.0 = 0.95
        ↓
   Invert: -(−0.95) = 0.95 → handleLeftMotor(0.95)
        ↓
   currentLeftVelocity = 0.95
        ↓
   robot.move(0.95, currentRightVelocity)
        ↓
   NetworkRobotImpl.move()
        ↓
   Create JSON: {"type":"motor","left":0.95,"right":0.0}
        ↓
   RobotNetworkServer.broadcastMotorCommand()
        ↓
   Send via TCP to all connected clients
        ↓
   Raspberry Pi receives JSON
        ↓
   RaspberryPiMotorClient parses message
        ↓
   motorController.setMotorSpeed(LEFT_MOTOR, 0.95)
        ↓
   GPIO pin configured for motor PWM
        ↓
   L298N motor driver receives signal
        ↓
   Left motor spins forward at 95% speed
        ↓
RESULT: Robot's left wheel accelerates forward ✓
```

## Motor Control Flow

```
┌────────────────────────────────────────────────────────────────┐
│                     RobotController                            │
│                  (Receives Joystick)                           │
└──────────────────┬─────────────────────────────────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
        ▼                     ▼
┌───────────────┐      ┌───────────────┐
│ handleLeftMotor     │ handleRightMotor
│  (leftValue)       │  (rightValue)
└───────┬──────┘     └────────┬───────┘
        │                    │
        │  currentLeft       │ currentRight
        │  Velocity = ...    │ Velocity = ...
        │                    │
        └─────────┬──────────┘
                  │
                  ▼
        ┌──────────────────────┐
        │  robot.move()        │
        │  (left, right)       │
        └──────┬───────────────┘
               │
        ┌──────▼────────────┐
        │ NetworkRobotImpl   │
        │ .move()           │
        └──────┬────────────┘
               │
        ┌──────▼──────────────────────┐
        │ Create Motor Command JSON   │
        │ {                           │
        │   "type": "motor",          │
        │   "left": 0.95,            │
        │   "right": 0.50             │
        │ }                           │
        └──────┬──────────────────────┘
               │
        ┌──────▼─────────────────────┐
        │ networkServer.broadcast()   │
        │ (Send to all clients)       │
        └──────┬─────────────────────┘
               │
        ┌──────▼────────────────────────┐
        │ TCP Socket to RPi on Port 5555│
        └─────────────────────────────┘
```

## Raspberry Pi Motor Driver Wiring (L298N Example)

```
                    L298N Motor Driver Board
                    ┌─────────────────────┐
                    │  ┌─────────────┐    │
                    │  │             │    │
     ENA ◀──────────│─┤ ENA      GND├────┤─────→ GND (RPi)
     IN1 ◀──────────│─┤ IN1      GND├────┤
     IN2 ◀──────────│─┤ IN2      OUT1├──→ LEFT MOTOR +
                    │  │      OUT2├──→ LEFT MOTOR -
     +5V ◀──────────│─┤ +5V         │    │
     GND ◀──────────│─┤ GND     ENB├────┤─────→ ENB (RPi GPIO 13)
                    │  │             │    │
                    │  │         IN3├────┤─────→ IN3 (RPi GPIO 22)
                    │  │         IN4├────┤─────→ IN4 (RPi GPIO 23)
                    │  │        OUT3├──→ RIGHT MOTOR +
                    │  │        OUT4├──→ RIGHT MOTOR -
                    │  └─────────────┘    │
                    └─────────────────────┘


RPi GPIO Connection:
┌──────────────────────────────────────┐
│  Raspberry Pi 5 GPIO Header          │
│                                      │
│  GPIO 17 ◄──────────── IN1 (L Fwd)  │
│  GPIO 27 ◄──────────── IN2 (L Rev)  │
│  GPIO 12 ◄──────────── ENA (L PWM)  │
│                                      │
│  GPIO 22 ◄──────────── IN3 (R Fwd)  │
│  GPIO 23 ◄──────────── IN4 (R Rev)  │
│  GPIO 13 ◄──────────── ENB (R PWM)  │
│                                      │
│  GND ◄────────────────── GND         │
│  5V ◄─────────────────── +5V         │
│                                      │
└──────────────────────────────────────┘


Motor Output Connection:
┌──────────────────────────┐
│  Out1 (+)  Out2 (-)      │
│    │         │           │
│    └────┬────┘           │
│         │                │
│      ┌──▼──┐             │
│      │ Left │   (Spins when ENA is PWM'ed  │
│      │Motor │    and IN1/IN2 control dir) │
│      └──────┘             │
│                          │
│  Out3 (+)  Out4 (-)      │
│    │         │           │
│    └────┬────┘           │
│         │                │
│      ┌──▼───┐            │
│      │Right │  (Same as left)      │
│      │Motor │            │
│      └──────┘            │
└──────────────────────────┘
```

## GPIO Pin Control Logic

```
For Left Motor:

GPIO 17 (IN1) │ GPIO 27 (IN2) │ Direction      │ GPIO 12 (ENA)
──────────────┼───────────────┼────────────────┼─────────────
   HIGH       │     LOW       │ Forward        │ PWM duty%
   LOW        │     HIGH      │ Backward       │ PWM duty%
   LOW        │     LOW       │ Stop           │ 0
   HIGH       │     HIGH      │ Stop/Brake     │ 0


For Right Motor: Same logic with GPIO 22/23 (IN3/IN4) and GPIO 13 (ENB)

Speed Control via PWM (ENA/ENB):
  0%   = 0%     speed (stopped)
  25%  = 25%    speed (slow)
  50%  = 50%    speed (medium)
  75%  = 75%    speed (fast)
  100% = 100%   speed (maximum)
```

## Message Flow Example

```
USER INTERACTION:
────────────────

PC: Push Left Joystick Forward
    │
    ├─ Raw Value: -0.95
    ├─ After Processing: 0.95
    │
PC: Push Right Joystick Back
    │
    ├─ Raw Value: 0.85
    ├─ After Processing: -0.85
    │

NETWORK TRANSMISSION:
──────────────────────
PC Server: {"type":"motor","left":0.95,"right":-0.85}
        │
        └─→ TCP Connection to RPi on Port 5555
            │
RPi Client: Receives JSON message
        │
        ├─ Parses left: 0.95
        ├─ Parses right: -0.85
        │

MOTOR EXECUTION:
───────────────
Left Motor:
    Velocity: 0.95
    └─→ IN1=HIGH, IN2=LOW, ENA=95% PWM
        └─→ Motor spins FORWARD at 95% speed

Right Motor:
    Velocity: -0.85
    └─→ IN3=LOW, IN4=HIGH, ENB=85% PWM
        └─→ Motor spins BACKWARD at 85% speed

ROBOT BEHAVIOR:
───────────────
    Left wheel spins forward (95%)
    Right wheel spins backward (85%)
    ↓
    Robot curves/spins LEFT in place
```

## Error Handling Flow

```
RobotNetworkServer
        │
        ├─ Client connects
        │   └─→ Add to client list
        │       └─→ Send welcome JSON
        │
        ├─ Broadcast motor command
        │   └─→ For each client:
        │       ├─ Send JSON
        │       ├─ If IOException → Remove client
        │       └─ Log disconnection
        │
        └─ Client disconnects
            └─→ Remove from list
                └─→ Log disconnection


RaspberryPiMotorClient
        │
        ├─ Attempt connection
        │   └─→ If fails, retry after 5s
        │
        ├─ Receive messages
        │   ├─ If valid JSON → Execute command
        │   ├─ If invalid JSON → Log warning, continue
        │   └─ If disconnected → Reconnect
        │
        └─ Handle IoException
            └─→ Disconnect & reconnect loop
```

## Timing Diagram

```
Time (ms)    PC Action                RPi Action         Motor State
────────────────────────────────────────────────────────────────────
0            Joystick X1: 0.5         [Waiting]          Stationary
16           Poll ✓                   [Waiting]
32           Send motor cmd           [Waiting]
35                                    Receive ✓          Accelerating
40                                    setMotorSpeed()    Moving
50           Joystick X2: 0.8         [Moving]           Moving
66           Poll ✓                   [Moving]
82           Send motor cmd           [Moving]
85                                    Receive ✓          Accelerating
90                                    setMotorSpeed()    Faster
100          Joystick Released: 0     [Moving]           Moving
116          Poll ✓                   [Moving]
132          Send motor cmd           [Moving]
135                                   Receive ✓          Decelerating
150                                   setMotorSpeed(0)   Stopped

Total latency: ~35-50ms typical
```

---

**Note**: GPIO pin numbers and motor driver type should be customized for your specific hardware setup.

Last Updated: March 4, 2026

