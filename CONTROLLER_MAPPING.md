# PS4 Controller Mapping Reference

## Visual Controller Layout

```
                    ▲ Triangle (Yellow)
                    │
        ◀ Square ────┼──── Circle ▶ (Red)
        (Blue)       │     (Green)
                    ▼
                  Cross (Blue)


           L1 (LB)          R1 (RB)
            ▲                ▲
            │                │
      ┌─────┴─────┐    ┌─────┴─────┐
      │  L2 (LT)  │    │  R2 (RT)  │
      └───────────┘    └───────────┘
   (Analog Trigger)  (Analog Trigger)


   ┌─────────────────────────────────────┐
   │  OPTIONS      PS Button      SHARE   │
   │   (Start)                    (Back)  │
   └─────────────────────────────────────┘


   ┌──────────────────┐    ┌──────────────────┐
   │ LEFT STICK       │    │ RIGHT STICK      │
   │   (L3 Click)     │    │   (R3 Click)     │
   │                  │    │                  │
   │ ◄─ X-Axis ──►   │    │ ◄─ X-Axis ──►   │
   │    Y-Axis       │    │    Y-Axis       │
   │     │           │    │     │           │
   │     ▼           │    │     ▼           │
   └──────────────────┘    └──────────────────┘
```

## Motor Control Mapping (CURRENT IMPLEMENTATION)

### Primary Controls

| Joystick | Axis | Direction | Motor | Speed | Result |
|----------|------|-----------|-------|-------|--------|
| **Left** | Y | Forward | Left | +1.0 | Left motor forward |
| **Left** | Y | Backward | Left | -1.0 | Left motor backward |
| **Left** | Y | Centered | Left | 0.0 | Left motor stop |
| **Right** | Y | Forward | Right | +1.0 | Right motor forward |
| **Right** | Y | Backward | Right | -1.0 | Right motor backward |
| **Right** | Y | Centered | Right | 0.0 | Right motor stop |

### Combined Movement Patterns

```
Left Up    +1.0    Right Up    +1.0
    ▲              ▲
    │              │
    └──────┬───────┘
           │
      MOVE FORWARD
           
           
Left Down  -1.0    Right Down  -1.0
    │              │
    │              │
    └──────┬───────┘
           │
      MOVE BACKWARD


Left Up    +1.0    Right Down  -1.0
    ▲              │
    │              │
    └──────┬───────┘
           │
      SPIN RIGHT (Clockwise)


Left Down  -1.0    Right Up    +1.0
    │              ▲
    │              │
    └──────┬───────┘
           │
      SPIN LEFT (Counter-clockwise)


Left Up    +1.5    Right Up    +0.5
    ▲              ▲
    │              │
    │              │
    └──────┬───────┘
           │
      CURVE RIGHT (Wider right turn)


Left Up    +0.5    Right Up    +1.5
    ▲              ▲
    │              │
    │              │
    └──────┬───────┘
           │
      CURVE LEFT (Wider left turn)
```

## Joystick Input Values

### Left Joystick Y-Axis

```
Pushed Forward
    ↑
    │
   +1.0  ← Maximum Forward
    │
   +0.5  ← Half Speed Forward
    │
    0    ← Dead Zone (-0.15 to +0.15)
    │
   -0.5  ← Half Speed Backward
    │
   -1.0  ← Maximum Backward
    │
    ↓
Pulled Backward
```

### Right Joystick Y-Axis
Same as Left Joystick Y-Axis

## Button Controls (Secondary Functions)

| Button | Function | Status |
|--------|----------|--------|
| **Cross (X)** | Jump/Action 1 | Ready to implement |
| **Circle (O)** | Action 2 | Ready to implement |
| **Square** | Action 3 | Ready to implement |
| **Triangle** | Action 4 | Ready to implement |
| **L1** | Boost | Ready to implement |
| **R1** | Strafe | Ready to implement |
| **L2** | Left Trigger (analog) | Available |
| **R2** | Right Trigger (analog) | Available |
| **L3** | Left Stick Click | Available |
| **R3** | Right Stick Click | Available |
| **Share** | Not assigned | Available |
| **Options** | Stop Robot | Implemented |
| **PS Button** | Not assigned | Available |
| **Touchpad** | Not assigned | Available |

## Dead Zone Visualization

```
Input Range with Dead Zone (±0.15)

Joystick Position    →    Motor Output

    -1.0                    -1.0
     │                      │
    -0.8                   -0.8
     │                      │
    -0.5                   -0.5
     │                      │
    -0.15                    │
     │         Dead Zone      │
     0 ─────────────────────  0
     │         Dead Zone      │
    +0.15                    │
     │                      │
    +0.5                   +0.5
     │                      │
    +0.8                   +0.8
     │                      │
    +1.0                   +1.0
```

## Network Command Examples

### Moving Forward at Full Speed
Both joysticks forward at maximum:
```json
{"type":"motor","left":1.0,"right":1.0}
```

### Moving Backward at Half Speed
Both joysticks back at half:
```json
{"type":"motor","left":-0.5,"right":-0.5}
```

### Spinning Right
Left forward, right backward:
```json
{"type":"motor","left":1.0,"right":-1.0}
```

### Spinning Left
Left backward, right forward:
```json
{"type":"motor","left":-1.0,"right":1.0}
```

### Curving Right
Left faster than right:
```json
{"type":"motor","left":1.0,"right":0.5}
```

### Curving Left
Right faster than left:
```json
{"type":"motor","left":0.5,"right":1.0}
```

### Only Left Motor Moving
Right motor stationary:
```json
{"type":"motor","left":0.7,"right":0.0}
```

### Emergency Stop
Both motors stop immediately:
```json
{"type":"motor","left":0.0,"right":0.0}
```

## Common Driving Patterns

### Pattern 1: Simple Forward/Backward
```
Action: Push left joystick up and down
Result: Robot moves forward and backward in straight line
```

### Pattern 2: Tank Turn (Spot Turn)
```
Action: Push left joystick up, right joystick down
Result: Robot spins in place clockwise
```

### Pattern 3: Arcade Turn
```
Action: Push both joysticks up, move left joystick left
Result: Robot moves forward while turning left
(Requires X-axis customization)
```

### Pattern 4: Independent Drive
```
Action: Push left joystick 75%, right joystick 25%
Result: Robot curves to the right while moving forward
```

### Pattern 5: Crabbing (Strafe)
```
Action: Push left joystick up, right joystick down (or vice versa)
Result: Robot moves diagonally or slides sideways
```

## Sensitivity Adjustments

### Effect of SENSITIVITY Multiplier

```
SENSITIVITY = 0.5  → Half response (gentler control)
Raw: 1.0 → Output: 0.5

SENSITIVITY = 1.0  → Normal response (current default)
Raw: 1.0 → Output: 1.0

SENSITIVITY = 2.0  → Double response (twitchy control)
Raw: 1.0 → Output: 2.0 (clamped to 1.0)
```

## Customization Examples

### Example 1: Reverse Motor Direction
In `RobotController.handleLeftMotor()`:
```java
private void handleLeftMotor(float leftValue) {
    currentLeftVelocity = -leftValue;  // Reverse direction
    robot.move(currentLeftVelocity, currentRightVelocity);
}
```

### Example 2: Reduce Sensitivity
In `ControlConfig.java`:
```java
public static final float SENSITIVITY = 0.7f;  // Reduced responsiveness
```

### Example 3: Increase Dead Zone
In `ControlConfig.java`:
```java
public static final float DEAD_ZONE = 0.25f;  // Larger stick threshold
```

### Example 4: Custom Stick Mapping
In `RobotController.onAxisMotion()`:
```java
case PS4Controller.AXIS_LEFT_STICK_Y:
    // Apply custom curve (exponential for better control)
    float curved = leftValue * leftValue * Math.signum(leftValue);
    handleLeftMotor(curved);
    break;
```

## Troubleshooting Input Issues

### Joystick Not Responding
1. Check dead zone is not too large (should be 0.10-0.20)
2. Verify joystick Y-axis is being read (check logs)
3. Test joystick movement with telnet/netcat to see raw values

### Joystick Drifts When Centered
1. Increase dead zone in ControlConfig.java
2. Calibrate joystick in OS settings
3. Try moving joystick slowly to neutral

### Motors Move in Wrong Direction
1. Reverse motor polarity in physical wiring
2. Or reverse in MotorController: `speed = -speed;`
3. Or reverse in RobotController input handling

### Motors Don't Stop When Released
1. Check dead zone is working (logs should show 0.0)
2. Verify network message includes 0.0 for that motor
3. Test with `{"type":"motor","left":0.0,"right":0.0}`

## Advanced Features Ready for Implementation

### 1. Trigger-Based Acceleration
```java
case PS4Controller.AXIS_L2_TRIGGER:
    float accelMultiplier = (value + 1.0f) / 2.0f;  // -1 to 1 → 0 to 1
    handleLeftMotor(currentLeftVelocity * accelMultiplier);
    break;
```

### 2. Button-Triggered Actions
```java
case PS4Controller.BUTTON_CROSS:
    robot.performAction("jump");  // or other actions
    break;
```

### 3. Exponential Response Curve
```java
// For finer control at low speeds
float exponential = joystickValue * joystickValue * Math.signum(joystickValue);
handleMotor(exponential);
```

### 4. Velocity Ramping
```java
// For smooth acceleration
currentVelocity += (targetVelocity - currentVelocity) * 0.1;  // Smooth transition
```

---

## Quick Reference Table

| Want This | Move This | Expected Behavior |
|-----------|-----------|-------------------|
| Forward | Both up | Straight forward |
| Backward | Both down | Straight backward |
| Spin clockwise | L-up, R-down | Rotate right |
| Spin counter-clockwise | L-down, R-up | Rotate left |
| Curve right | L-up more than R-up | Arc right |
| Curve left | R-up more than L-up | Arc left |
| Crab right | L-up, R-down + offset | Slide right |
| Stop | Both centered | All motors stop |

---

**Last Updated**: March 4, 2026
**Version**: 1.0

