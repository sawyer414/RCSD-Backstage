package org.example;

/**
 * Configuration settings for PS4 controller and robot control
 */
public class ControlConfig {
    // Controller polling settings
    public static final int POLL_RATE_MS = 16;           // ~60 Hz
    public static final float DEAD_ZONE = 0.15f;         // 15% dead zone
    public static final float SENSITIVITY = 1.0f;        // 1.0 = normal sensitivity

    // Movement settings
    public static final float MAX_FORWARD_SPEED = 1.0f;  // 100% speed
    public static final float MAX_ROTATION_SPEED = 1.0f; // 100% rotation speed
    public static final float MAX_STRAFE_SPEED = 1.0f;   // 100% strafe speed

    // Trigger sensitivity (0.0 to 1.0)
    public static final float TRIGGER_SENSITIVITY = 0.5f;

    // Enable debug logging
    public static final boolean DEBUG_LOGGING = false;

    // Motor/actuator settings (customize based on your robot)
    public static final int LEFT_MOTOR_PIN = 1;
    public static final int RIGHT_MOTOR_PIN = 2;
    public static final int SERVO_PIN = 3;

    // Acceleration/deceleration rates (0.0 to 1.0 per frame)
    public static final float ACCELERATION_RATE = 1.0f;  // Immediate acceleration
    public static final float DECELERATION_RATE = 0.9f;  // Gradual deceleration

    /**
     * Get the actual motor speed based on joystick input
     * @param joystickValue Raw joystick value (-1.0 to 1.0)
     * @return Clamped motor speed (-1.0 to 1.0)
     */
    public static float getMotorSpeed(float joystickValue) {
        // Apply dead zone
        if (Math.abs(joystickValue) < DEAD_ZONE) {
            return 0.0f;
        }

        // Apply sensitivity
        float adjusted = joystickValue * SENSITIVITY;

        // Clamp to valid range
        return Math.max(-1.0f, Math.min(1.0f, adjusted));
    }

    /**
     * Get rotation speed from trigger input
     * @param triggerValue Trigger value (0.0 to 1.0)
     * @return Rotation speed
     */
    public static float getTriggerSpeed(float triggerValue) {
        return triggerValue * TRIGGER_SENSITIVITY;
    }

    /**
     * Calculate motor speed for differential drive
     * @param forward Forward/backward speed (-1.0 to 1.0)
     * @param turn Rotation speed (-1.0 to 1.0)
     * @param motor Motor identifier (0 = left, 1 = right)
     * @return Motor speed value
     */
    public static float calculateDifferentialDrive(float forward, float turn, int motor) {
        if (motor == 0) {  // Left motor
            return forward + turn;
        } else {  // Right motor
            return forward - turn;
        }
    }
}

