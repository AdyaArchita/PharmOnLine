package edu.pharmacy.desktop.chat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Swing client used by the pharmacy support team or a customer. */
public class ChatClient extends JFrame {
    private final String host;
    private final int port;
    private final String username;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private final JTextArea chatArea = new JTextArea(14, 36);
    private final JTextField messageField = new JTextField();

    public ChatClient(String host, int port, String username) {
        super("Pharmacy Support Chat - " + username);
        this.host = host;
        this.port = port;
        this.username = username;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 420);
        setLocationRelativeTo(null);

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        JButton sendButton = new JButton("Send");
        messageField.addActionListener(event -> sendMessage());
        sendButton.addActionListener(event -> sendMessage());
        bottom.add(messageField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);

        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().add(new JScrollPane(chatArea), BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new javax.swing.JLabel("Connected as: " + username));
        getContentPane().add(header, BorderLayout.NORTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent event) {
                disconnect();
            }
        });

        setPreferredSize(new Dimension(520, 420));
        pack();
        connect();
    }

    private void connect() {
        try {
            socket = new Socket(host, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("/name " + username);
            appendMessage("Connected to pharmacy support desk.");
            Thread readerThread = new Thread(this::readMessages, "chat-reader-" + username);
            readerThread.setDaemon(true);
            readerThread.start();
        } catch (UnknownHostException exception) {
            appendMessage("Unknown host: " + exception.getMessage());
        } catch (IOException exception) {
            appendMessage("Unable to connect: " + exception.getMessage());
        }
    }

    private void readMessages() {
        try {
            String line;
            while ((line = input.readLine()) != null) {
                appendMessage(line);
            }
        } catch (IOException exception) {
            appendMessage("Connection closed: " + exception.getMessage());
        }
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        if (output != null) {
            output.println(text);
        }
        messageField.setText("");
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + System.lineSeparator()));
    }

    public void disconnect() {
        try {
            if (output != null) {
                output.println("/quit");
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException exception) {
            System.err.println("Error closing chat client: " + exception.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatClient client = new ChatClient("localhost", ChatServer.DEFAULT_PORT, "Pharmacist");
            client.setVisible(true);
        });
    }
}
