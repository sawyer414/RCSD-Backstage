package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridges PS4 controller input to robot movement.
 *
 * On Linux (Raspberry Pi) it uses LinuxPS4Controller which reads directly from
 * /dev/input/js* — no JInput native libraries required.
 * On Windows/macOS it falls back to the JInput-based PS4Controller.
 */
public class RobotController implements ControllerListener {
    private static final Logger logger = LoggerFactory.getLogger(RobotController.class);

    private Robot robot;

    // One of these will be non-null depending on the platform
    private PS4Controller ps4Controller;
    private LinuxPS4Controller linuxController;

    // Current velocity states for independent motor control
    private float currentLeftVelocity = 0.0f;
    private float currentRightVelocity = 0.0f;

    // Dead zone threshold for analog sticks
    private static final float DEAD_ZONE = 0.15f;

    // Sensitivity multiplier for stick input
    private static final float SENSITIVITY = 1.0f;

    public RobotController(Robot robot) throws ControllerException {
        this.robot = robot;

        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("linux")) {
            // Use native Linux joystick API — works on ARM64 / Raspberry Pi
            String device = LinuxPS4Controller.findDevice();
            if (device == null) {
                throw new ControllerException(
                    "No joystick device found under /dev/input/js*. " +
                    "Make sure your PS4 controller is connected.");
            }
            logger.info("Linux detected — using native joystick device: {}", device);
            linuxController = new LinuxPS4Controller(device, this);
        } else {
            // Windows / macOS — use JInput
            logger.info("Non-Linux OS detected — using JInput PS4Controller");
            ps4Controller = new PS4Controller(this);
        }
    }

    /**
     * Start the controller and robot control
     */
    public void start() {
        if (linuxController != null) {
            linuxController.start();
        } else {
            ps4Controller.start();
        }
        logger.info("Robot controller started");
    }

    /**
     * Stop the controller and robot control
     */
    public void stop() {
        if (linuxController != null) {
            linuxController.stop();
        } else if (ps4Controller != null) {
            ps4Controller.stop();
        }
        robot.stop();
        logger.info("Robot controller stopped");
    }

    @Override
    public void onButtonPressed(int buttonId) {
        logger.debug("Button pressed: {}", buttonId);

        switch (buttonId) {
            case PS4Controller.BUTTON_CROSS:
                logger.info("Cross (X) pressed - Performing action");
                robot.performAction("jump");
                break;
            case PS4Controller.BUTTON_CIRCLE:
                logger.info("Circle (O) pressed - Special action 1");
                robot.performAction("action1");
                break;
            case PS4Controller.BUTTON_SQUARE:
                logger.info("Square pressed - Special action 2");
                robot.performAction("action2");
                break;
            case PS4Controller.BUTTON_TRIANGLE:
                logger.info("Triangle pressed - Special action 3");
                robot.performAction("action3");
                break;
            case PS4Controller.BUTTON_L1:
                logger.info("L1 pressed");
                robot.performAction("boost");
                break;
            case PS4Controller.BUTTON_R1:
                logger.info("R1 pressed");
                robot.performAction("strafe");
                break;
            case PS4Controller.BUTTON_OPTIONS:
                logger.info("Options pressed - Stopping robot");
                robot.stop();
                break;
        }
    }

    @Override
    public void onButtonReleased(int buttonId) {
        logger.debug("Button released: {}", buttonId);
    }

    @Override
    public void onAxisMotion(int axisId, float value) {
        // Apply dead zone
        if (Math.abs(value) < DEAD_ZONE) {
            value = 0;
        }

        // Apply sensitivity
        value *= SENSITIVITY;

        switch (axisId) {
            case PS4Controller.AXIS_LEFT_STICK_Y:
                // Left joystick controls left motor (Y-axis, inverted)
                handleLeftMotor(-value);
                break;
            case PS4Controller.AXIS_RIGHT_STICK_Y:
                // Right joystick controls right motor (Y-axis, inverted)
                handleRightMotor(-value);
                break;
            case PS4Controller.AXIS_LEFT_STICK_X:
                // Left stick X - can be used for additional control
                logger.debug("Left stick X: {}", String.format("%.2f", value));
                break;
            case PS4Controller.AXIS_RIGHT_STICK_X:
                // Right stick X - can be used for additional control
                logger.debug("Right stick X: {}", String.format("%.2f", value));
                break;
            case PS4Controller.AXIS_L2_TRIGGER:
                // Left trigger
                handleLeftTrigger(value);
                break;
            case PS4Controller.AXIS_R2_TRIGGER:
                // Right trigger
                handleRightTrigger(value);
                break;
        }
    }

    @Override
    public void onControllerDisconnected() {
        logger.warn("Controller disconnected!");
        robot.stop();
    }

    /**
     * Handle left motor control from left joystick Y-axis
     */
    private void handleLeftMotor(float leftValue) {
        logger.debug("Left Motor: {}", String.format("%.2f", leftValue));
        currentLeftVelocity = leftValue;
        robot.move(currentLeftVelocity, currentRightVelocity);
    }

    /**
     * Handle right motor control from right joystick Y-axis
     */
    private void handleRightMotor(float rightValue) {
        logger.debug("Right Motor: {}", String.format("%.2f", rightValue));
        currentRightVelocity = rightValue;
        robot.move(currentLeftVelocity, currentRightVelocity);
    }

    /**
     * Handle left trigger
     */
    private void handleLeftTrigger(float value) {
        logger.debug("Left trigger: {}", String.format("%.2f", value));
        // Can be used for specific actions like acceleration
    }

    /**
     * Handle right trigger
     */
    private void handleRightTrigger(float value) {
        logger.debug("Right trigger: {}", String.format("%.2f", value));
        // Can be used for specific actions like deceleration
    }
}

