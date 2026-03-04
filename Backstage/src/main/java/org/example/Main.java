package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for PS4 controller robot control
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    // Default server port for network communication
    private static final int DEFAULT_SERVER_PORT = 5555;

    public static void main(String[] args) {
        logger.info("=== PS4 Controller Robot Control System ===");
        logger.info("Mode: Network-based (Raspberry Pi 5 Compatible)");

        try {
            // Get server port from arguments or use default
            int serverPort = DEFAULT_SERVER_PORT;
            if (args.length > 0) {
                try {
                    serverPort = Integer.parseInt(args[0]);
                    logger.info("Using custom server port: {}", serverPort);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid port number '{}', using default port {}", args[0], DEFAULT_SERVER_PORT);
                }
            }

            logger.info("Initializing...");

            // Create network robot instance (broadcasts to Raspberry Pi)
            NetworkRobotImpl robot = new NetworkRobotImpl(serverPort);
            logger.info("Network robot created successfully");
            logger.info("Waiting for Raspberry Pi connections on port {}", serverPort);

            // Create robot controller (bridges PS4 controller to robot)
            RobotController controller = new RobotController(robot);
            logger.info("Robot controller created successfully");

            // Start the controller
            controller.start();
            logger.info("Controller started - waiting for PS4 controller input");

            // Keep the application running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down...");
                controller.stop();
                robot.shutdown();
                logger.info("Goodbye!");
            }));

            // Wait indefinitely
            Thread.currentThread().join();

        } catch (ControllerException e) {
            logger.error("Controller initialization failed: {}", e.getMessage());
            logger.info("Make sure your PS4 controller is connected via USB or wireless adapter");
            System.exit(1);
        } catch (InterruptedException e) {
            logger.error("Application interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}

