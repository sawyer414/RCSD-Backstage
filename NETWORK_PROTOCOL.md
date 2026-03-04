# Network Protocol Documentation

## Overview

The robot control system uses TCP sockets with JSON-formatted messages for communication between the PC (running the PS4 controller) and Raspberry Pi 5 (running the motor controller).

## Protocol Specification

### Connection Details
- **Protocol**: TCP/IP
- **Default Port**: 5555 (configurable)
- **Message Format**: JSON over TCP
- **Line Delimiter**: Newline character (`\n`)
- **Encoding**: UTF-8

### Message Types

#### 1. Motor Control Command (Server → Client)

Sent whenever joystick or button input changes motor state.

```json
{
  "type": "motor",
  "left": 0.75,
  "right": 0.75
}
```

**Fields**:
- `type` (string): Always `"motor"` for motor commands
- `left` (float): Left motor velocity (-1.0 to 1.0)
  - `-1.0`: Full reverse
  - `0.0`: Stop
  - `1.0`: Full forward
- `right` (float): Right motor velocity (-1.0 to 1.0)

**Example Sequences**:

Moving Forward (both joysticks pushed forward):
```json
{"type": "motor", "left": 1.0, "right": 1.0}
{"type": "motor", "left": 0.8, "right": 0.8}
{"type": "motor", "left": 0.6, "right": 0.6}
{"type": "motor", "left": 0.0, "right": 0.0}
```

Turning Right (left forward, right back):
```json
{"type": "motor", "left": 1.0, "right": -1.0}
```

Left Motor Only:
```json
{"type": "motor", "left": 0.5, "right": 0.0}
```

Immediate Stop:
```json
{"type": "motor", "left": 0.0, "right": 0.0}
```

#### 2. Connection Acknowledgment (Server → Client)

Sent immediately upon successful connection.

```json
{
  "status": "connected",
  "message": "Connected to robot control server"
}
```

### Velocity Value Interpretation

| Value | Meaning |
|-------|---------|
| `0.0` | Motor stopped |
| `0.1` to `0.5` | Slow speed |
| `0.5` to `1.0` | Normal to full speed |
| `-0.1` to `-0.5` | Slow reverse |
| `-0.5` to `-1.0` | Normal to full reverse |

### Dead Zone Handling

Input values between `-0.15` and `0.15` (approximately) are converted to `0.0` by the controller to eliminate joystick drift.

## Client Implementation Reference

### Receiving Motor Commands

```java
// Listen for incoming commands
BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
Gson gson = new Gson();

String line;
while ((line = reader.readLine()) != null) {
    JsonObject command = gson.fromJson(line, JsonObject.class);
    
    if ("motor".equals(command.get("type").getAsString())) {
        float leftSpeed = command.get("left").getAsFloat();
        float rightSpeed = command.get("right").getAsFloat();
        
        // Apply motor control
        motorController.setMotorSpeed(LEFT_MOTOR, leftSpeed);
        motorController.setMotorSpeed(RIGHT_MOTOR, rightSpeed);
    }
}
```

### Sending Acknowledgment (Optional)

```java
PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
JsonObject ack = new JsonObject();
ack.addProperty("status", "ready");
ack.addProperty("robot_id", "rpi-001");
writer.println(gson.toJson(ack));
```

## Example Network Trace

### Connection Establishment

```
[CLIENT] Connects to SERVER:5555
[SERVER] Accepts connection from CLIENT
[SERVER] Sends: {"status":"connected","message":"Connected to robot control server"}
[CLIENT] Receives: {"status":"connected","message":"Connected to robot control server"}
```

### Joystick Movement Sequence

```
[USER] Pushes left joystick forward
[SERVER] Sends: {"type":"motor","left":1.0,"right":0.0}
[CLIENT] Receives and executes: setMotorSpeed(LEFT_MOTOR, 1.0)

[USER] Pushes right joystick forward
[SERVER] Sends: {"type":"motor","left":1.0,"right":1.0}
[CLIENT] Receives and executes: setMotorSpeed(RIGHT_MOTOR, 1.0)

[USER] Releases both joysticks
[SERVER] Sends: {"type":"motor","left":0.0,"right":0.0}
[CLIENT] Receives and executes: setMotorSpeed(LEFT_MOTOR, 0.0), setMotorSpeed(RIGHT_MOTOR, 0.0)

[USER] Disconnects controller or closes application
[SERVER] Sends: {"type":"motor","left":0.0,"right":0.0}
[SERVER] Closes connection
[CLIENT] Detects disconnection and stops motors
```

## Scaling and Sensitivity

### Motor Speed Scaling

The velocity values are pre-scaled before transmission:

```
Raw Joystick Value (-1.0 to 1.0)
    ↓
Apply Dead Zone (if |value| < 0.15, set to 0)
    ↓
Apply Sensitivity Multiplier (×1.0 by default)
    ↓
Network Transmission
```

To adjust sensitivity, modify the `SENSITIVITY` constant in `ControlConfig.java`.

## Connection Management

### Timeout Behavior

- **Server**: Broadcasts commands to all connected clients every time controller input changes (approx. 16ms per poll cycle)
- **Client**: Should handle graceful disconnection if server stops sending data for >5 seconds

### Reconnection

- If connection is lost, the client should attempt to reconnect every 5 seconds
- The server automatically removes disconnected clients from the broadcast list

## Error Handling

### Common Issues

1. **Invalid JSON**: Client should log and ignore malformed messages
2. **Network Error**: Client should disconnect and attempt reconnection
3. **Motor Out of Range**: Clamp velocity to [-1.0, 1.0] range
4. **Timeout**: Close connection and reconnect after delay

### Recommended Error Handling

```java
try {
    // Network communication code
} catch (JsonSyntaxException e) {
    logger.warn("Invalid JSON received, ignoring");
} catch (IOException e) {
    logger.error("Network error, attempting reconnection");
    disconnect();
    Thread.sleep(5000); // Wait 5 seconds before reconnect
    connect();
}
```

## Performance Metrics

### Update Frequency

- **Controller Poll Rate**: ~60 Hz (16ms intervals)
- **Network Transmission**: On-change + polling
- **Typical Latency**: <50ms on local network
- **Maximum Clients**: 10 (configurable thread pool size)

### Bandwidth Usage

- **Per Message**: ~50-60 bytes (JSON with motor commands)
- **Typical Rate**: 10-60 messages/second
- **Typical Usage**: 0.5-3.6 KB/s

## Future Extensions

### Possible Protocol Enhancements

1. **Sensor Feedback**:
```json
{"type": "sensor", "battery": 0.85, "temperature": 45.2}
```

2. **Configuration Commands**:
```json
{"type": "config", "max_speed": 0.8, "dead_zone": 0.2}
```

3. **Status Reporting**:
```json
{"type": "status", "connected": true, "active_clients": 1}
```

4. **Button Events**:
```json
{"type": "button", "button_id": 1, "state": "pressed"}
```

## Testing the Protocol

### Using netcat (Linux/Mac)

**Terminal 1 - Server**:
```bash
java -jar Backstage-1.0-SNAPSHOT.jar
```

**Terminal 2 - Mock Client**:
```bash
nc localhost 5555
```

Then observe the JSON messages being sent when you move the joysticks.

### Using telnet (Windows)

```cmd
telnet localhost 5555
```

### Using Python (Advanced Testing)

```python
import socket
import json

# Connect to server
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect(('localhost', 5555))

# Receive welcome message
welcome = sock.recv(1024)
print("Welcome:", welcome.decode())

# Listen for commands
while True:
    data = sock.recv(1024)
    if data:
        command = json.loads(data.decode())
        print(f"Left: {command['left']:.2f}, Right: {command['right']:.2f}")

sock.close()
```

## Security Considerations

⚠️ **Warning**: This protocol is designed for local networks only. For external or untrusted networks, add:

1. **Authentication**: Token-based authentication
2. **Encryption**: TLS/SSL wrapper
3. **Validation**: Input validation and sanitization
4. **Rate Limiting**: Command rate limiting to prevent DoS

Example with TLS:
```java
// Add TLS socket factory
SSLContext sslContext = SSLContext.getInstance("TLS");
sslContext.init(null, null, null);
SSLSocketFactory socketFactory = sslContext.getSocketFactory();
socket = (SSLSocket) socketFactory.createSocket(host, port);
```

