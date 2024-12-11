import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class TCPClient {
    private static int clientId;
    private static JTextArea textArea;
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;

    public static void main(String[] args) {
        JFrame frame = new JFrame("TCP Client");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setEditable(false);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JTextField textField = new JTextField();
        JButton sendButton = new JButton("Send");

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);

        try {
            socket = new Socket("localhost", 9876);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Get the client ID from the server
            clientId = Integer.parseInt(in.readLine());

            // Start a new thread to listen for messages from the server
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String incomingMessage;
                        while ((incomingMessage = in.readLine()) != null) {
                            textArea.append(incomingMessage + "\n");
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();

            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String message = textField.getText();
                    if (!message.isEmpty()) {
                        textArea.append("Client " + clientId + ": " + message + "\n");

                        sendMessage(message);

                        textField.setText("");
                    }
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void sendMessage(String message) {
        out.println(message);  
    }
}
