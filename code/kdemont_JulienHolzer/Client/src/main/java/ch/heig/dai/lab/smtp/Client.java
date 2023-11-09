package ch.heig.dai.lab.smtp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.*;

public class Client {
    private static final String SMTP_SERVER = "localhost";
    private static final int SMTP_PORT = 1025;

    public static void main(String[] args) {
        // Read victim and message lists from JSON files
        List<String> victims = readFromJsonFile("victims.json", "emails");
        List<String> messages = readFromJsonFile("messages.json", "messages");

        // Other client logic...
        for (String victim : victims) {
            // Form groups, select messages, and send emails
            // (You'll need to implement the logic for this)
            // ...

            // Example: Send email content
            sendEmail("Your Subject", "Hello, this is the body of the email.", victim);
        }
    }

    private static void sendEmail(String subject, String body, String recipient) {
        try (Socket socket = new Socket(SMTP_SERVER, SMTP_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8))) {

            // SMTP communication logic...

            sendCommand(writer, "MAIL FROM: sender@example.com");
            sendCommand(writer, "RCPT TO: " + recipient);
            sendCommand(writer, "DATA");
            sendCommand(writer, "Subject: " + subject);
            sendCommand(writer, ""); // Empty line before the body
            sendCommand(writer, body);
            sendCommand(writer, ".");

            // SMTP communication logic...

        } catch (IOException e) {
            System.out.println("Client: exception while using client socket: " + e);
        }
    }

    private static List<String> readFromJsonFile(String filePath, String arrayKey) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();

            JSONObject jsonObject = new JSONObject(jsonString.toString());
            JSONArray jsonArray = jsonObject.getJSONArray(arrayKey);

            List<String> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getString(i));
            }
            return list;
        } catch (IOException e) {
            e.printStackTrace();
            return List.of(); // Return an empty list in case of an error
        }
    }

    private static void sendCommand(BufferedWriter writer, String command) throws IOException {
        writer.write(command + "\n");
        writer.flush();
    }
}
