package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network-based robot implementation that sends motor commands to Raspberry Pi 5
 * over a TCP network connection
 */
public class NetworkRobotImpl implements Robot {
    private static final Logger logger = LoggerFactory.getLogger(NetworkRobotImpl.class);

    private RobotNetworkServer networkServer;
    private boolean connected = false;
    private double currentLeftVelocity = 0;
    private double currentRightVelocity = 0;

    /**
     * Create a network robot with server on specified port
     */
    public NetworkRobotImpl(int serverPort) {
        this.networkServer = new RobotNetworkServer(serverPort);
        this.connected = true;
        logger.info("Network Robot initialized on port {}", serverPort);
        networkServer.start();
    }

    /**
     * Create a network robot with default port 5555
     */
    public NetworkRobotImpl() {
        this(5555);
    }

    @Override
    public void move(double leftVelocity, double rightVelocity) {
        currentLeftVelocity = clamp(leftVelocity, -1.0, 1.0);
        currentRightVelocity = clamp(rightVelocity, -1.0, 1.0);

        logger.debug("Move: left={}, right={}",
            String.format("%.2f", currentLeftVelocity),
            String.format("%.2f", currentRightVelocity));

        // Send motor command over network to Raspberry Pi
        if (networkServer != null) {
            networkServer.broadcastMotorCommand(
                (float) currentLeftVelocity,
                (float) currentRightVelocity
            );
        }
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

        // Send stop command to robot
        if (networkServer != null) {
            networkServer.broadcastMotorCommand(0.0f, 0.0f);
        }
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
     * Shutdown the network server
     */
    public void shutdown() {
        logger.info("Shutting down network server");
        if (networkServer != null) {
            networkServer.stop();
        }
        connected = false;
    }

    /**
     * Clamp a value between min and max
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

