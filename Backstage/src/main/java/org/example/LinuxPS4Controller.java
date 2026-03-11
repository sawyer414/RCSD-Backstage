package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Linux-native PS4 controller reader using /dev/input/js* (joystick interface).
 *
 * Each event from the joystick device is a fixed 8-byte struct:
 *   4 bytes: timestamp (ms)   - int32
 *   2 bytes: value            - int16
 *   1 byte:  type             - JS_EVENT_BUTTON (0x01) or JS_EVENT_AXIS (0x02)
 *   1 byte:  number           - button/axis index
 *
 * This avoids JInput entirely and works on ARM64 (Raspberry Pi 5).
 */
public class LinuxPS4Controller {

    private static final Logger logger = LoggerFactory.getLogger(LinuxPS4Controller.class);

    private static final int JS_EVENT_BUTTON = 0x01;
    private static final int JS_EVENT_AXIS   = 0x02;
    private static final int JS_EVENT_INIT   = 0x80;  // sent on open to report current state

    // Axis indices on most Linux PS4 mappings
    private static final int AXIS_LEFT_X  = 0;
    private static final int AXIS_LEFT_Y  = 1;
    private static final int AXIS_RIGHT_X = 3;
    private static final int AXIS_RIGHT_Y = 4;
    private static final int AXIS_L2      = 2;
    private static final int AXIS_R2      = 5;

    private final String devicePath;
    private final ControllerListener listener;
    private volatile boolean running = false;
    private Thread readerThread;

    public LinuxPS4Controller(String devicePath, ControllerListener listener) {
        this.devicePath = devicePath;
        this.listener = listener;
    }

    /** Find first available /dev/input/js* device. Returns null if none found. */
    public static String findDevice() {
        try {
            List<Path> devices = Files.list(Paths.get("/dev/input"))
                    .filter(p -> p.getFileName().toString().startsWith("js"))
                    .sorted()
                    .collect(Collectors.toList());
            if (!devices.isEmpty()) {
                return devices.get(0).toString();
            }
        } catch (IOException e) {
            logger.warn("Could not list /dev/input: {}", e.getMessage());
        }
        return null;
    }

    public void start() {
        if (running) return;
        running = true;
        readerThread = new Thread(this::readLoop);
        readerThread.setName("LinuxPS4-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
        logger.info("LinuxPS4Controller started on {}", devicePath);
    }

    public void stop() {
        running = false;
        if (readerThread != null) {
            readerThread.interrupt();
        }
        logger.info("LinuxPS4Controller stopped");
    }

    private void readLoop() {
        try (FileInputStream fis = new FileInputStream(devicePath)) {
            byte[] buf = new byte[8];
            ByteBuffer bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);

            while (running) {
                int bytesRead = 0;
                while (bytesRead < 8) {
                    int r = fis.read(buf, bytesRead, 8 - bytesRead);
                    if (r == -1) {
                        logger.warn("Controller device closed");
                        running = false;
                        if (listener != null) listener.onControllerDisconnected();
                        return;
                    }
                    bytesRead += r;
                }

                bb.rewind();
                /* int timestamp = */ bb.getInt();   // skip timestamp
                short value  = bb.getShort();
                byte  type   = bb.get();
                byte  number = bb.get();

                // Strip init flag
                int eventType = type & ~JS_EVENT_INIT;

                if (eventType == JS_EVENT_BUTTON && listener != null) {
                    int btn = mapButton(number & 0xFF);
                    if (btn >= 0) {
                        if (value != 0) listener.onButtonPressed(btn);
                        else            listener.onButtonReleased(btn);
                    }
                } else if (eventType == JS_EVENT_AXIS && listener != null) {
                    int axis = mapAxis(number & 0xFF);
                    if (axis >= 0) {
                        // Normalise int16 to -1.0 .. 1.0
                        float norm = value / 32767.0f;
                        if (Math.abs(norm) < 0.1f) norm = 0.0f; // dead zone
                        listener.onAxisMotion(axis, norm);
                    }
                }
            }
        } catch (IOException e) {
            if (running) {
                logger.error("Error reading from {}: {}", devicePath, e.getMessage());
                if (listener != null) listener.onControllerDisconnected();
            }
        }
    }

    /** Map Linux joystick button index -> PS4Controller button constants */
    private int mapButton(int n) {
        switch (n) {
            case 0:  return PS4Controller.BUTTON_CROSS;
            case 1:  return PS4Controller.BUTTON_CIRCLE;
            case 2:  return PS4Controller.BUTTON_SQUARE;
            case 3:  return PS4Controller.BUTTON_TRIANGLE;
            case 4:  return PS4Controller.BUTTON_L1;
            case 5:  return PS4Controller.BUTTON_R1;
            case 6:  return PS4Controller.BUTTON_L2;
            case 7:  return PS4Controller.BUTTON_R2;
            case 8:  return PS4Controller.BUTTON_SHARE;
            case 9:  return PS4Controller.BUTTON_OPTIONS;
            case 10: return PS4Controller.BUTTON_L3;
            case 11: return PS4Controller.BUTTON_R3;
            case 12: return PS4Controller.BUTTON_PS;
            case 13: return PS4Controller.BUTTON_TOUCHPAD;
            default: return -1;
        }
    }

    /** Map Linux joystick axis index -> PS4Controller axis constants */
    private int mapAxis(int n) {
        switch (n) {
            case AXIS_LEFT_X:  return PS4Controller.AXIS_LEFT_STICK_X;
            case AXIS_LEFT_Y:  return PS4Controller.AXIS_LEFT_STICK_Y;
            case AXIS_RIGHT_X: return PS4Controller.AXIS_RIGHT_STICK_X;
            case AXIS_RIGHT_Y: return PS4Controller.AXIS_RIGHT_STICK_Y;
            case AXIS_L2:      return PS4Controller.AXIS_L2_TRIGGER;
            case AXIS_R2:      return PS4Controller.AXIS_R2_TRIGGER;
            default:           return -1;
        }
    }
}

