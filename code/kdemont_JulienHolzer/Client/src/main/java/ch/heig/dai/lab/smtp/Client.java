package ch.heig.dai.lab.protocoldesign;

import java.io.*;
import java.net.*;
import static java.nio.charset.StandardCharsets.*;

public class Client {
    final String SERVER_ADDRESS = "127.0.0.1";
    final int SERVER_PORT = 1234;

    public static void main(String[] args) {
        // Create a new client and run it
        Client client = new Client();
        client.run();
    }

    private void run() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            // Create input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8));

            // Read and print the welcome message and supported operations
            String welcomeMessage = in.readLine();
            String supportedOperations = in.readLine();
            System.out.println(welcomeMessage);
            System.out.println(supportedOperations);

            // Read and send user commands
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            while ((userInput = reader.readLine()) != null) {
                out.write(userInput + "\n");
                out.flush();
                String result = in.readLine();
                System.out.println("Result is: " + result);
            }
        } catch (IOException e) {
            System.out.println("Client: exception while using client socket: " + e);
        }
    }
}
