import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

class ClientHandler implements Runnable {
    private static final Map<Integer, ClientHandler> clientHandlers = new HashMap<>();
    private static int clientCounter = 1; // Counter to assign unique ID to each client
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    public int clientId;
    private JTextArea serverTextArea;

    // To keep track of message history per client pair
    private static final Map<String, List<String>> messageHistory = new HashMap<>();

    public ClientHandler(Socket clientSocket, JTextArea serverTextArea) throws IOException {
        this.clientSocket = clientSocket;
        this.serverTextArea = serverTextArea;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.clientId = clientCounter++;

        synchronized (clientHandlers) {
            clientHandlers.put(clientId, this); // Register client
        }
    }

    @Override
    public void run() {
        try {
            out.println(clientId); // Send client ID to the client

            String message;
            while ((message = in.readLine()) != null) {
                String messageToSend = "Client " + clientId + ": " + message;

                // Update server's GUI with the received message
                serverTextArea.append(messageToSend + "\n");

                // Handle message history
                synchronized (messageHistory) {
                    // Store the message for each client pair
                    for (Map.Entry<Integer, ClientHandler> entry : clientHandlers.entrySet()) {
                        if (entry.getKey() != clientId) {
                            String pairKey = getPairKey(clientId, entry.getKey());
                            messageHistory.computeIfAbsent(pairKey, k -> new ArrayList<>()).add(messageToSend);
                        }
                    }
                }

                // Send message to all other clients except the sender
                synchronized (clientHandlers) {
                    for (ClientHandler client : clientHandlers.values()) {
                        if (client != this) {
                            client.out.println(messageToSend);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                synchronized (clientHandlers) {
                    clientHandlers.remove(clientId); // Remove the client when disconnected
                }
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getPairKey(int client1, int client2) {
        return (client1 < client2) ? client1 + ":" + client2 : client2 + ":" + client1;
    }

    public static List<String> getMessageHistory(int clientId1, int clientId2) {
        String pairKey = (clientId1 < clientId2) ? clientId1 + ":" + clientId2 : clientId2 + ":" + clientId1;
        return messageHistory.getOrDefault(pairKey, new ArrayList<>());
    }
}

public class TCPServer {
    private static JTextArea serverTextArea;

    public static void main(String[] args) {
        JFrame frame = new JFrame("TCP Server");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        serverTextArea = new JTextArea();
        serverTextArea.setEditable(false);
        panel.add(new JScrollPane(serverTextArea), BorderLayout.CENTER);

        frame.add(panel);
        frame.setVisible(true);

        try (ServerSocket serverSocket = new ServerSocket(9876)) {
            serverTextArea.append("Server is running and waiting for connections...\n");

            ExecutorService threadPool = Executors.newCachedThreadPool();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, serverTextArea);
                
                serverTextArea.append("Client " + clientHandler.clientId + " connected: " + clientSocket.getInetAddress() + "\n");

                // Start a new thread for each client
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
