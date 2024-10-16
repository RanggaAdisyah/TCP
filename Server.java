import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private static List<ClientHandler> clientHandlers = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1212);
            System.out.println("Server siap menerima koneksi...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Klien terhubung!");

                ClientHandler clientHandler = new ClientHandler(clientSocket, clientHandlers.size() + 1);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private int clientId;

        public ClientHandler(Socket socket, int id) {
            this.clientSocket = socket;
            this.clientId = id;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    synchronized (System.out) {
                        System.out.printf("[Dari Klien %d]: %s\n", clientId, clientMessage);
                    }

                    if (clientMessage.equalsIgnoreCase("exit")) {
                        synchronized (System.out) {
                            System.out.printf("Klien %d memutuskan koneksi.\n", clientId);
                        }
                        break;
                    }
                    // Server memilih mengirim ke siapa
                    broadcastMessage(clientId);
                }

            } catch (SocketException e) {
                System.out.printf("Klien %d terputus secara tiba-tiba.\n", clientId);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // Tutup semua stream dan socket
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (clientSocket != null) clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientHandlers.remove(this);  // Hapus handler setelah selesai
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }

    private static void broadcastMessage(int fromClientId) {
        Scanner scanner = new Scanner(System.in);

        synchronized (System.out) {
            System.out.print("Kirim pesan ke (1: Klien 1, 2: Klien 2, 0: Semua): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            String serverMessage;
            switch (choice) {
                case 1:
                case 2:
                    System.out.printf("Server (ke Klien %d): ", choice);
                    serverMessage = scanner.nextLine();
                    clientHandlers.get(choice - 1).sendMessage("Dari Server: " + serverMessage);
                    break;
                case 0:
                    System.out.print("Server (kirim ke semua): ");
                    serverMessage = scanner.nextLine();
                    for (ClientHandler handler : clientHandlers) {
                        handler.sendMessage("Dari Server: " + serverMessage);
                    }
                    break;
                default:
                    System.out.println("Pilihan tidak valid.");
                    break;
            }
        }
    }
}
