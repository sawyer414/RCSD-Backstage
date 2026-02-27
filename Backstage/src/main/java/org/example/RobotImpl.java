package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example implementation of a robot controlled via PS4 controller
 */
public class RobotImpl implements Robot {
    private static final Logger logger = LoggerFactory.getLogger(RobotImpl.class);

    private boolean connected = false;
    private double currentLeftVelocity = 0;
    private double currentRightVelocity = 0;

    public RobotImpl() {
        this.connected = true;
        logger.info("Robot initialized");
    }

    @Override
    public void move(double leftVelocity, double rightVelocity) {
        currentLeftVelocity = clamp(leftVelocity, -1.0, 1.0);
        currentRightVelocity = clamp(rightVelocity, -1.0, 1.0);

        logger.debug("Move: left={}, right={}",
            String.format("%.2f", currentLeftVelocity),
            String.format("%.2f", currentRightVelocity));

        // TODO: Send motor commands to actual robot
        // sendMotorCommand(LEFT_MOTOR, currentLeftVelocity);
        // sendMotorCommand(RIGHT_MOTOR, currentRightVelocity);
    }

    @Override
    public void rotate(double angularVelocity) {
        angularVelocity = clamp(angularVelocity, -1.0, 1.0);

        logger.debug("Rotate: {}", String.format("%.2f", angularVelocity));

        // Set motors to opposite velocities for rotation
        move(-angularVelocity, angularVelocity);
    }

    @Override
    public void stop() {
        currentLeftVelocity = 0;
        currentRightVelocity = 0;

        logger.info("Robot stopped");

        // TODO: Send stop command to robot
        // sendMotorCommand(LEFT_MOTOR, 0);
        // sendMotorCommand(RIGHT_MOTOR, 0);
    }

    @Override
    public void performAction(String action) {
        logger.info("Performing action: {}", action);

        switch (action.toLowerCase()) {
            case "forward":
                move(1.0, 1.0);
                break;
            case "backward":
                move(-1.0, -1.0);
                break;
            case "left":
                move(-1.0, 1.0);
                break;
            case "right":
                move(1.0, -1.0);
                break;
            case "stop":
                stop();
                break;
            default:
                logger.warn("Unknown action: {}", action);
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    /**
     * Clamp a value between min and max
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

