package org.example;

/**
 * Interface for robot control commands
 */
public interface Robot {
    /**
     * Move the robot with velocity values
     * @param leftVelocity Left motor velocity (-1.0 to 1.0)
     * @param rightVelocity Right motor velocity (-1.0 to 1.0)
     */
    void move(double leftVelocity, double rightVelocity);

    /**
     * Rotate the robot in place
     * @param angularVelocity Rotation speed (-1.0 to 1.0)
     */
    void rotate(double angularVelocity);

    /**
     * Stop the robot
     */
    void stop();

    /**
     * Perform a special action
     * @param action Action identifier
     */
    void performAction(String action);

    /**
     * Check if robot is connected
     */
    boolean isConnected();
}

