package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Network server for broadcasting robot control commands to Raspberry Pi 5
 * Listens for controller input and sends motor commands over TCP
 */
public class RobotNetworkServer {
    private static final Logger logger = LoggerFactory.getLogger(RobotNetworkServer.class);

    private int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private ExecutorService executorService;
    private CopyOnWriteArrayList<ClientHandler> connectedClients;
    private Gson gson;

    public RobotNetworkServer(int port) {
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(10);
        this.connectedClients = new CopyOnWriteArrayList<>();
        this.gson = new Gson();
    }

    /**
     * Start the network server
     */
    public void start() {
        if (running) {
            logger.warn("Server already running");
            return;
        }

        running = true;
        logger.info("Starting robot network server on port {}", port);

        // Accept connections in a separate thread
        Thread acceptThread = new Thread(this::acceptConnections);
        acceptThread.setName("RobotNetworkServer-Accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    /**
     * Stop the network server and disconnect all clients
     */
    public void stop() {
        running = false;
        logger.info("Stopping robot network server");

        // Disconnect all clients
        for (ClientHandler client : connectedClients) {
            client.disconnect();
        }
        connectedClients.clear();

        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket", e);
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("Robot network server stopped");
    }

    /**
     * Accept incoming client connections
     */
    private void acceptConnections() {
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Server listening on port {}", port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("New client connected: {}", clientSocket.getInetAddress());

                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    connectedClients.add(handler);
                    executorService.execute(handler);

                } catch (SocketTimeoutException e) {
                    // Timeout, continue accepting
                } catch (IOException e) {
                    if (running) {
                        logger.error("Error accepting client connection", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Server socket error", e);
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                logger.error("Error closing server socket", e);
            }
        }
    }

    /**
     * Broadcast motor command to all connected robots
     */
    public void broadcastMotorCommand(float leftVelocity, float rightVelocity) {
        JsonObject command = new JsonObject();
        command.addProperty("type", "motor");
        command.addProperty("left", leftVelocity);
        command.addProperty("right", rightVelocity);

        String json = gson.toJson(command);
        broadcastToClients(json);
    }

    /**
     * Broadcast a message to all connected clients
     */
    private void broadcastToClients(String message) {
        for (ClientHandler client : connectedClients) {
            client.sendMessage(message);
        }
    }

    /**
     * Remove a disconnected client from the list
     */
    protected void removeClient(ClientHandler handler) {
        connectedClients.remove(handler);
        logger.info("Client disconnected. Active connections: {}", connectedClients.size());
    }

    /**
     * Handler for individual client connections
     */
    protected static class ClientHandler implements Runnable {
        private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

        private Socket socket;
        private PrintWriter writer;
        private BufferedReader reader;
        private RobotNetworkServer server;
        private volatile boolean connected = true;

        public ClientHandler(Socket socket, RobotNetworkServer server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Send welcome message
                JsonObject welcome = new JsonObject();
                welcome.addProperty("status", "connected");
                welcome.addProperty("message", "Connected to robot control server");
                sendMessage(new Gson().toJson(welcome));

                // Read incoming commands
                String line;
                while (connected && (line = reader.readLine()) != null) {
                    logger.debug("Received from client: {}", line);
                    // Process any incoming commands from Raspberry Pi if needed
                }

            } catch (IOException e) {
                logger.error("Client communication error", e);
            } finally {
                disconnect();
            }
        }

        /**
         * Send a message to this client
         */
        protected synchronized void sendMessage(String message) {
            if (connected && writer != null) {
                writer.println(message);
            }
        }

        /**
         * Disconnect this client
         */
        protected void disconnect() {
            connected = false;
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                logger.error("Error closing client connection", e);
            }
            server.removeClient(this);
        }
    }
}

