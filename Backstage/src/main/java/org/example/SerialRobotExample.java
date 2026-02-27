package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example: Robot implementation with serial communication for hardware control
 *
 * This example shows how to extend the Robot interface to communicate with
 * actual robot hardware via serial port. Customize this based on your specific
 * robot platform (Arduino, Raspberry Pi, etc.)
 *
 * To use this, uncomment in Main.java:
 *   Robot robot = new SerialRobot("COM3", 9600);  // Replace with your COM port
 */
public class SerialRobotExample implements Robot {
    private static final Logger logger = LoggerFactory.getLogger(SerialRobotExample.class);

    private String portName;
    private int baudRate;
    private boolean connected = false;

    // TODO: Add actual serial port communication library
    // import com.fazecast.jSerialComm.*;
    // private SerialPort serialPort;

    public SerialRobotExample(String portName, int baudRate) {
        this.portName = portName;
        this.baudRate = baudRate;
        initializeSerialConnection();
    }

    /**
     * Initialize serial port connection to robot
     */
    private void initializeSerialConnection() {
        try {
            // TODO: Initialize serial port
            // serialPort = SerialPort.getCommPort(portName);
            // serialPort.setBaudRate(baudRate);
            // serialPort.openPort();

            connected = true;
            logger.info("Serial robot connected on {} at {} baud", portName, baudRate);
        } catch (Exception e) {
            logger.error("Failed to connect to robot on port {}", portName, e);
            connected = false;
        }
    }

    @Override
    public void move(double leftVelocity, double rightVelocity) {
        if (!connected) {
            logger.warn("Robot not connected");
            return;
        }

        // Example: Send motor commands as "L:100,R:-50\n"
        int leftSpeed = (int) (leftVelocity * 100);
        int rightSpeed = (int) (rightVelocity * 100);

        String command = String.format("M:%d,%d\n", leftSpeed, rightSpeed);
        sendCommand(command);

        logger.debug("Move command sent: left={}, right={}", leftSpeed, rightSpeed);
    }

    @Override
    public void rotate(double angularVelocity) {
        if (!connected) {
            logger.warn("Robot not connected");
            return;
        }

        // Set motors to opposite velocities
        move(-angularVelocity, angularVelocity);
    }

    @Override
    public void stop() {
        if (!connected) {
            logger.warn("Robot not connected");
            return;
        }

        String command = "STOP\n";
        sendCommand(command);
        logger.info("Stop command sent");
    }

    @Override
    public void performAction(String action) {
        if (!connected) {
            logger.warn("Robot not connected");
            return;
        }

        String command = String.format("ACTION:%s\n", action);
        sendCommand(command);
        logger.info("Action performed: {}", action);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    /**
     * Send a command via serial port
     */
    private void sendCommand(String command) {
        if (!connected) {
            logger.warn("Cannot send command - not connected");
            return;
        }

        try {
            // TODO: Send via serial port
            // byte[] commandBytes = command.getBytes();
            // serialPort.writeBytes(commandBytes, commandBytes.length);

            logger.debug("Command sent: {}", command.trim());
        } catch (Exception e) {
            logger.error("Failed to send command", e);
        }
    }

    /**
     * Close the connection
     */
    public void close() {
        if (connected) {
            try {
                // TODO: Close serial port
                // serialPort.closePort();

                connected = false;
                logger.info("Serial robot disconnected");
            } catch (Exception e) {
                logger.error("Error closing serial connection", e);
            }
        }
    }
}

