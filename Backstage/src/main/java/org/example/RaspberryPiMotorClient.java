package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;

/**
 * Raspberry Pi 5 Robot Motor Controller
 * This client connects to the network server and receives motor control commands
 *
 * Run this on the Raspberry Pi with motor control libraries (e.g., Pi4J)
 */
public class RaspberryPiMotorClient {
    private static final Logger logger = LoggerFactory.getLogger(RaspberryPiMotorClient.class);

    private String serverHost;
    private int serverPort;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private volatile boolean connected = false;
    private MotorController motorController;

    public RaspberryPiMotorClient(String serverHost, int serverPort, MotorController motorController) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.motorController = motorController;
    }

    /**
     * Connect to the network server
     */
    public void connect() {
        try {
            logger.info("Connecting to robot control server at {}:{}", serverHost, serverPort);
            socket = new Socket(serverHost, serverPort);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            connected = true;

            logger.info("Connected to server");

            // Start listening for commands
            Thread listenerThread = new Thread(this::listenForCommands);
            listenerThread.setName("RaspberryPi-Listener");
            listenerThread.setDaemon(false);
            listenerThread.start();

        } catch (IOException e) {
            logger.error("Failed to connect to server at {}:{}", serverHost, serverPort, e);
            connected = false;
        }
    }

    /**
     * Listen for incoming motor control commands
     */
    private void listenForCommands() {
        Gson gson = new Gson();
        String line;

        try {
            while (connected && (line = reader.readLine()) != null) {
                try {
                    JsonObject command = gson.fromJson(line, JsonObject.class);

                    if (command.has("type") && "motor".equals(command.get("type").getAsString())) {
                        float leftVelocity = command.get("left").getAsFloat();
                        float rightVelocity = command.get("right").getAsFloat();

                        logger.debug("Motor command received - Left: {}, Right: {}",
                            String.format("%.2f", leftVelocity),
                            String.format("%.2f", rightVelocity));

                        // Execute motor command
                        if (motorController != null) {
                            motorController.setMotorSpeed(MotorController.LEFT_MOTOR, leftVelocity);
                            motorController.setMotorSpeed(MotorController.RIGHT_MOTOR, rightVelocity);
                        }
                    }
                } catch (JsonSyntaxException e) {
                    logger.warn("Invalid JSON received: {}", line);
                }
            }
        } catch (IOException e) {
            logger.error("Error listening for commands", e);
        } finally {
            disconnect();
        }
    }

    /**
     * Disconnect from the server
     */
    public void disconnect() {
        connected = false;

        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            logger.error("Error disconnecting from server", e);
        }

        logger.info("Disconnected from server");
    }

    /**
     * Check if connected to the server
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Interface for motor controller implementation
     * Implement this with actual GPIO/PWM control for your robot's motors
     */
    public interface MotorController {
        int LEFT_MOTOR = 0;
        int RIGHT_MOTOR = 1;

        /**
         * Set motor speed
         * @param motorId Motor identifier (LEFT_MOTOR or RIGHT_MOTOR)
         * @param speed Speed from -1.0 (full reverse) to 1.0 (full forward)
         */
        void setMotorSpeed(int motorId, float speed);
    }

    /**
     * Example implementation using mock motors (for testing without RPi)
     */
    public static class MockMotorController implements MotorController {
        private static final Logger logger = LoggerFactory.getLogger(MockMotorController.class);

        @Override
        public void setMotorSpeed(int motorId, float speed) {
            String motorName = motorId == LEFT_MOTOR ? "LEFT" : "RIGHT";
            logger.info("Motor {} speed: {}", motorName, String.format("%.2f", speed));
            // TODO: Replace with actual GPIO control using Pi4J or similar library
        }
    }

    /**
     * Example main for testing the Raspberry Pi client
     * Usage: java RaspberryPiMotorClient <serverHost> <serverPort>
     */
    public static void main(String[] args) {
        String serverHost = "localhost";
        int serverPort = 5555;

        if (args.length >= 1) {
            serverHost = args[0];
        }
        if (args.length >= 2) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid port number, using default {}", serverPort);
            }
        }

        logger.info("=== Raspberry Pi Motor Controller ===");
        logger.info("Connecting to server at {}:{}", serverHost, serverPort);

        // Create a mock motor controller for demonstration
        MotorController motorController = new MockMotorController();

        // Create and connect client
        RaspberryPiMotorClient client = new RaspberryPiMotorClient(serverHost, serverPort, motorController);
        client.connect();

        // Keep running until disconnected
        while (client.isConnected()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        client.disconnect();
        logger.info("Motor controller stopped");
    }
}

