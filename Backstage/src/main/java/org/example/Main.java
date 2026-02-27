package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for PS4 controller robot control
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("=== PS4 Controller Robot Control System ===");
        logger.info("Initializing...");

        try {
            // Create robot instance
            Robot robot = new RobotImpl();
            logger.info("Robot created successfully");

            // Create robot controller (bridges PS4 controller to robot)
            RobotController controller = new RobotController(robot);
            logger.info("Robot controller created successfully");

            // Start the controller
            controller.start();
            logger.info("Controller started - waiting for input");

            // Keep the application running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down...");
                controller.stop();
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

