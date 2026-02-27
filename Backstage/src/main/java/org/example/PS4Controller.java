package org.example;

import net.java.games.input.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PS4 Controller interface for robot control.
 * Maps PS4 controller inputs to robot commands.
 */
public class PS4Controller {
    private static final Logger logger = LoggerFactory.getLogger(PS4Controller.class);
    
    private Controller controller;
    private ControllerListener listener;
    private boolean running = false;
    
    // PS4 Button mappings
    public static final int BUTTON_CROSS = 0;      // X
    public static final int BUTTON_CIRCLE = 1;     // O
    public static final int BUTTON_SQUARE = 2;     // Square
    public static final int BUTTON_TRIANGLE = 3;   // Triangle
    public static final int BUTTON_L1 = 4;
    public static final int BUTTON_R1 = 5;
    public static final int BUTTON_L2 = 6;
    public static final int BUTTON_R2 = 7;
    public static final int BUTTON_SHARE = 8;
    public static final int BUTTON_OPTIONS = 9;
    public static final int BUTTON_L3 = 10;        // Left stick click
    public static final int BUTTON_R3 = 11;        // Right stick click
    public static final int BUTTON_PS = 12;        // PS button
    public static final int BUTTON_TOUCHPAD = 13;
    
    // Axis mappings
    public static final int AXIS_LEFT_STICK_X = 0;
    public static final int AXIS_LEFT_STICK_Y = 1;
    public static final int AXIS_RIGHT_STICK_X = 2;
    public static final int AXIS_RIGHT_STICK_Y = 3;
    public static final int AXIS_L2_TRIGGER = 4;
    public static final int AXIS_R2_TRIGGER = 5;
    
    public PS4Controller(ControllerListener listener) throws ControllerException {
        this.listener = listener;
        initializeController();
    }
    
    /**
     * Initialize and find a PS4 controller
     */
    private void initializeController() throws ControllerException {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        
        if (controllers.length == 0) {
            throw new ControllerException("No controllers found. Connect a PS4 controller.");
        }
        
        // Find PS4 controller
        for (Controller ctrl : controllers) {
            if (ctrl.getName().toLowerCase().contains("ps") || 
                ctrl.getName().toLowerCase().contains("sony") ||
                ctrl.getName().toLowerCase().contains("playstation") ||
                ctrl.getName().toLowerCase().contains("wireless")) {
                this.controller = ctrl;
                logger.info("Found PS4 Controller: " + ctrl.getName());
                return;
            }
        }
        
        // If no PS4 controller found, use first available controller
        this.controller = controllers[0];
        logger.warn("PS4 Controller not found. Using: " + controller.getName());
    }
    
    /**
     * Start polling the controller
     */
    public void start() {
        if (running) {
            logger.warn("Controller already running");
            return;
        }
        
        running = true;
        logger.info("Starting PS4 Controller polling...");
        
        Thread pollingThread = new Thread(this::pollController);
        pollingThread.setName("PS4-Controller-Poller");
        pollingThread.setDaemon(true);
        pollingThread.start();
    }
    
    /**
     * Stop polling the controller
     */
    public void stop() {
        running = false;
        logger.info("Stopping PS4 Controller polling...");
    }
    
    /**
     * Poll controller state and invoke callbacks
     */
    private void pollController() {
        while (running) {
            if (!controller.poll()) {
                logger.warn("Controller disconnected!");
                running = false;
                if (listener != null) {
                    listener.onControllerDisconnected();
                }
                break;
            }
            
            EventQueue queue = controller.getEventQueue();
            Event event = new Event();
            
            while (queue.getNextEvent(event)) {
                handleEvent(event);
            }
            
            try {
                Thread.sleep(16); // ~60 Hz polling rate
            } catch (InterruptedException e) {
                logger.error("Polling thread interrupted", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Handle controller events
     */
    private void handleEvent(Event event) {
        Component component = event.getComponent();
        float value = event.getValue();
        
        if (listener == null) {
            return;
        }
        
        // Handle buttons
        if (component.isAnalog() == false && value == 1.0f) {
            handleButtonPress(component);
        } else if (component.isAnalog() == false && value == 0.0f) {
            handleButtonRelease(component);
        }
        
        // Handle axes (analog sticks, triggers)
        if (component.isAnalog()) {
            handleAxisMotion(component, value);
        }
    }
    
    /**
     * Handle button press
     */
    private void handleButtonPress(Component component) {
        String name = component.getName().toLowerCase();
        
        if (name.contains("cross") || name.contains("0")) {
            listener.onButtonPressed(BUTTON_CROSS);
        } else if (name.contains("circle") || name.contains("1")) {
            listener.onButtonPressed(BUTTON_CIRCLE);
        } else if (name.contains("square") || name.contains("2")) {
            listener.onButtonPressed(BUTTON_SQUARE);
        } else if (name.contains("triangle") || name.contains("3")) {
            listener.onButtonPressed(BUTTON_TRIANGLE);
        } else if (name.contains("l1") || name.contains("lb")) {
            listener.onButtonPressed(BUTTON_L1);
        } else if (name.contains("r1") || name.contains("rb")) {
            listener.onButtonPressed(BUTTON_R1);
        } else if (name.contains("l2") || name.contains("lt")) {
            listener.onButtonPressed(BUTTON_L2);
        } else if (name.contains("r2") || name.contains("rt")) {
            listener.onButtonPressed(BUTTON_R2);
        } else if (name.contains("share")) {
            listener.onButtonPressed(BUTTON_SHARE);
        } else if (name.contains("options")) {
            listener.onButtonPressed(BUTTON_OPTIONS);
        }
    }
    
    /**
     * Handle button release
     */
    private void handleButtonRelease(Component component) {
        String name = component.getName().toLowerCase();
        
        if (name.contains("cross") || name.contains("0")) {
            listener.onButtonReleased(BUTTON_CROSS);
        } else if (name.contains("circle") || name.contains("1")) {
            listener.onButtonReleased(BUTTON_CIRCLE);
        } else if (name.contains("square") || name.contains("2")) {
            listener.onButtonReleased(BUTTON_SQUARE);
        } else if (name.contains("triangle") || name.contains("3")) {
            listener.onButtonReleased(BUTTON_TRIANGLE);
        } else if (name.contains("l1") || name.contains("lb")) {
            listener.onButtonReleased(BUTTON_L1);
        } else if (name.contains("r1") || name.contains("rb")) {
            listener.onButtonReleased(BUTTON_R1);
        } else if (name.contains("l2") || name.contains("lt")) {
            listener.onButtonReleased(BUTTON_L2);
        } else if (name.contains("r2") || name.contains("rt")) {
            listener.onButtonReleased(BUTTON_R2);
        } else if (name.contains("share")) {
            listener.onButtonReleased(BUTTON_SHARE);
        } else if (name.contains("options")) {
            listener.onButtonReleased(BUTTON_OPTIONS);
        }
    }
    
    /**
     * Handle analog stick and trigger motion
     */
    private void handleAxisMotion(Component component, float value) {
        String name = component.getName().toLowerCase();
        
        // Ignore dead zone
        if (Math.abs(value) < 0.1f) {
            value = 0;
        }
        
        if (name.contains("x") && name.contains("left")) {
            listener.onAxisMotion(AXIS_LEFT_STICK_X, value);
        } else if (name.contains("y") && name.contains("left")) {
            listener.onAxisMotion(AXIS_LEFT_STICK_Y, value);
        } else if (name.contains("x") && name.contains("right")) {
            listener.onAxisMotion(AXIS_RIGHT_STICK_X, value);
        } else if (name.contains("y") && name.contains("right")) {
            listener.onAxisMotion(AXIS_RIGHT_STICK_Y, value);
        } else if (name.contains("z") || (name.contains("trigger") && name.contains("left"))) {
            listener.onAxisMotion(AXIS_L2_TRIGGER, value);
        } else if (name.contains("rz") || (name.contains("trigger") && name.contains("right"))) {
            listener.onAxisMotion(AXIS_R2_TRIGGER, value);
        }
    }
    
    /**
     * Check if controller is connected
     */
    public boolean isConnected() {
        return controller != null && controller.poll();
    }
    
    /**
     * Get the controller
     */
    public Controller getController() {
        return controller;
    }
}

/**
 * Custom exception for controller-related errors
 */
class ControllerException extends Exception {
    public ControllerException(String message) {
        super(message);
    }
    
    public ControllerException(String message, Throwable cause) {
        super(message, cause);
    }
}

