package edu.pharmacy.desktop.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/** Multi-client TCP chat server used for pharmacy support communication. */
public class ChatServer {
    public static final int DEFAULT_PORT = 5001;

    private final ServerSocket serverSocket;
    private final Set<ClientHandler> clients = new CopyOnWriteArraySet<>();

    public ChatServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public void start() {
        System.out.println("Support chat server listening on port " + DEFAULT_PORT + ".");
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket);
                clients.add(client);
                Thread thread = new Thread(client, "chat-client-" + clients.size());
                thread.start();
            } catch (IOException exception) {
                if (!serverSocket.isClosed()) {
                    System.err.println("Chat server accept failed: " + exception.getMessage());
                }
            }
        }
    }

    public void shutdown() throws IOException {
        serverSocket.close();
        for (ClientHandler client : clients) {
            client.close();
        }
    }

    private void broadcast(String sender, String message) {
        for (ClientHandler client : clients) {
            client.send("[" + sender + "] " + message);
        }
    }

    private final class ClientHandler implements Runnable {
        private final Socket socket;
        private final BufferedReader input;
        private final PrintWriter output;
        private String username = "Guest";

        private ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new PrintWriter(socket.getOutputStream(), true);
            send("Welcome to the pharmacy support desk. Use /name YourName to change your name.");
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = input.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    if (line.startsWith("/name ")) {
                        username = line.substring(6).trim();
                        if (username.isEmpty()) {
                            username = "Guest";
                        }
                        send("Your name is now: " + username);
                        continue;
                    }
                    if ("/quit".equalsIgnoreCase(line)) {
                        break;
                    }
                    broadcast(username, line);
                }
            } catch (IOException exception) {
                System.err.println("Client connection lost: " + exception.getMessage());
            } finally {
                clients.remove(this);
                broadcastSystem("User " + username + " disconnected.");
                close();
            }
        }

        private void send(String message) {
            output.println(message);
        }

        private void broadcastSystem(String message) {
            for (ClientHandler client : clients) {
                if (client != this) {
                    client.send(message);
                }
            }
        }

        private void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
                // Closing the socket is best-effort.
            }
        }
    }

    public static void main(String[] args) {
        try {
            ChatServer server = new ChatServer(DEFAULT_PORT);
            server.start();
        } catch (IOException exception) {
            System.err.println("Unable to start chat server: " + exception.getMessage());
        }
    }
}
