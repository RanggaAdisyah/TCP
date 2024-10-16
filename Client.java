import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 1212);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader
                    (new InputStreamReader(socket.getInputStream()));
            BufferedReader clientInput = new BufferedReader(new InputStreamReader(System.in));

            String clientMessage, serverMessage;
            while (true) {
                System.out.print("Klien: ");
                clientMessage = clientInput.readLine();
                System.out.println("pesan telah dikirim");
                out.println(clientMessage);
                if (clientMessage.equalsIgnoreCase("exit")) {
                    System.out.println("Klien memutuskan koneksi.");
                    break;
                }

                // Menerima pesan dari server
                if ((serverMessage = in.readLine()) != null) {
                    System.out.println("Dari Server: " + serverMessage);
                    if (serverMessage.equalsIgnoreCase("exit")) {
                        System.out.println("Server memutuskan koneksi.");
                        break;
                    }
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
