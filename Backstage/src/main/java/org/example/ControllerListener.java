package org.example;

/**
 * Interface for PS4 controller events
 */
public interface ControllerListener {
    /**
     * Called when a button is pressed
     * @param buttonId Button ID constant from PS4Controller
     */
    void onButtonPressed(int buttonId);

    /**
     * Called when a button is released
     * @param buttonId Button ID constant from PS4Controller
     */
    void onButtonReleased(int buttonId);

    /**
     * Called when an analog axis changes (sticks, triggers)
     * @param axisId Axis ID constant from PS4Controller
     * @param value Axis value from -1.0 to 1.0
     */
    void onAxisMotion(int axisId, float value);

    /**
     * Called when controller is disconnected
     */
    void onControllerDisconnected();
}

