package ch.heig.dai.lab.protocoldesign;

import java.io.*;
import java.net.*;
import static java.nio.charset.StandardCharsets.*;

public class Server {
    final int SERVER_PORT = 1234;

    public static void main(String[] args) {
        // Create a new server and run it
        Server server = new Server();
        server.run();
    }

    private void run() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is running on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Create input and output streams
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), UTF_8));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), UTF_8));

                out.write("Welcome to the Calculator Server!\n");
                out.write("Supported operations: ADD, MUL\n");
                out.flush();

                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    String[] parts = clientMessage.split(" ");
                    if (parts.length >= 3) {
                        String operation = parts[0];
                        int num1 = Integer.parseInt(parts[1]);
                        int num2 = Integer.parseInt(parts[2]);

                        int result = 0;
                        if (operation.equals("ADD")) {
                            result = num1 + num2;
                        } else if (operation.equals("MUL")) {
                            result = num1 * num2;
                        }

                        out.write(result);
                        out.flush();
                    }
                }

                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Server: server socket ex.: " + e);
        }
    } 
}