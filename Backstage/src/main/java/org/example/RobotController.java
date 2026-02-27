package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridges PS4 controller input to robot movement
 */
public class RobotController implements ControllerListener {
    private static final Logger logger = LoggerFactory.getLogger(RobotController.class);

    private Robot robot;
    private PS4Controller ps4Controller;

    // Dead zone threshold for analog sticks
    private static final float DEAD_ZONE = 0.15f;

    // Sensitivity multiplier for stick input
    private static final float SENSITIVITY = 1.0f;

    public RobotController(Robot robot) throws ControllerException {
        this.robot = robot;
        this.ps4Controller = new PS4Controller(this);
    }

    /**
     * Start the controller and robot control
     */
    public void start() {
        ps4Controller.start();
        logger.info("Robot controller started");
    }

    /**
     * Stop the controller and robot control
     */
    public void stop() {
        ps4Controller.stop();
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
            case PS4Controller.AXIS_LEFT_STICK_X:
                // Left stick horizontal - rotation
                handleRotation(value);
                break;
            case PS4Controller.AXIS_LEFT_STICK_Y:
                // Left stick vertical - forward/backward (Y is inverted typically)
                handleMovement(-value);
                break;
            case PS4Controller.AXIS_RIGHT_STICK_X:
                // Right stick horizontal - strafe/turn
                handleStrafe(value);
                break;
            case PS4Controller.AXIS_RIGHT_STICK_Y:
                // Right stick vertical - up/down
                logger.debug("Right stick Y: {}", String.format("%.2f", value));
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
     * Handle forward/backward movement from left stick Y
     */
    private void handleMovement(float forwardValue) {
        logger.debug("Movement: {}", String.format("%.2f", forwardValue));
        robot.move(forwardValue, forwardValue);
    }

    /**
     * Handle rotation from left stick X
     */
    private void handleRotation(float rotationValue) {
        logger.debug("Rotation: {}", String.format("%.2f", rotationValue));
        robot.rotate(rotationValue);
    }

    /**
     * Handle strafing from right stick X
     */
    private void handleStrafe(float strafeValue) {
        logger.debug("Strafe: {}", String.format("%.2f", strafeValue));
        // For differential drive: left and right at different speeds
        robot.move(-strafeValue, strafeValue);
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

