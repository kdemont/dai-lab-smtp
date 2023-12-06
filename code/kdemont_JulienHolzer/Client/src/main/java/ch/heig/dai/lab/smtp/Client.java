package ch.heig.dai.lab.smtp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The main client class for sending emails to groups of victims.
 * SMTP client application for sending emails to groups of victims
 * using messages read from JSON files.
 * The program reads victim and message lists from JSON files, shuffles
 * the list of victims, divides them into n groups, and sends emails to
 * each group using a random message from the messages list.
 * Messages and victims are read from "messages.json" and "victims.json"
 * files respectively.
 *
 * @author Julien Holzer
 * @author Kilian Demont
 */
public class Client {
    private static final String SMTP_SERVER = "localhost";
    private static final int SMTP_PORT = 1025;

    /**
     * Main method to initiate the email sending process.
     *
     * @param args Command-line arguments for the number of groups.
     */
    public static void main(String[] args) {
        // Check if a command line argument is provided
        if (args.length == 0) {
            System.err.println("Error: Please provide the number of groups as a command line argument.");
            return;
        }

        // Read the number of groups provided as a command line argument
        int n;
        try {
            n = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number format for the number of groups.");
            return;
        }

        // Check if the number of groups is invalid
        if (n <= 0) {
            System.err.println("Error: The number of groups must be greater than zero.");
            return;
        }

        // Read victim and message lists from JSON files
        List<String> victims = readFromJsonFile("/victims.json", "victims");
        List<Message> messages = readMessagesFromJsonFile("/messages.json", "messages");

        // Check if the e-mail adresses are valid
        for (var victim : victims){
            if (!checkValidAddress(victim)){
                return;
            }
        }

        // Shuffle the list of victims
        Collections.shuffle(victims);

        // Divide the victims into n groups, each having 2-5 email addresses
        List<List<String>> groups = divideIntoGroups(victims, n);

        for (List<String> group : groups) {
            // The first address of the group is the sender
            String sender = group.get(0);

            // The rest are receivers (victims)
            List<String> receivers = group.subList(1, group.size());

            // Select a random message from the messages list
            Message randomMessage = getRandomMessage(messages);

            // Send emails to the group
            sendEmail(sender, receivers, randomMessage.getSubject(), randomMessage.getBody());
        }

        System.out.println("All messages have been sent successfully.");
    }

    /**
     * Reads a list of strings from a JSON file.
     *
     * @param filePath The path to the JSON file.
     * @param arrayKey The key for the array in the JSON file.
     * @return A list of strings read from the JSON file.
     */
    private static List<String> readFromJsonFile(String filePath, String arrayKey) {
        try {
            InputStream inputStream = Client.class.getResourceAsStream(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

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
            System.err.println("Client: exception while reading json: " + e);
            return List.of(); // Return an empty list in case of an error
        }
    }

    /**
     * Reads a list of messages from a JSON file.
     *
     * @param filePath The path to the JSON file.
     * @param arrayKey The key for the array in the JSON file.
     * @return A list of messages read from the JSON file.
     */
    private static List<Message> readMessagesFromJsonFile(String filePath, String arrayKey) {
        try {
            InputStream inputStream = Client.class.getResourceAsStream(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();

            JSONObject jsonObject = new JSONObject(jsonString.toString());
            JSONArray jsonArray = jsonObject.getJSONArray(arrayKey);

            List<Message> messages = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject messageObject = jsonArray.getJSONObject(i);
                String subject = messageObject.getString("subject");
                String body = messageObject.getString("body");
                messages.add(new Message(subject, body));
            }
            return messages;
        } catch (IOException e) {
            System.err.println("Client: exception while reading json: " + e);
            return List.of(); // Return an empty list in case of an error
        }
    }

    /**
     * Sends an email to a group of receivers.
     *
     * @param sender   The sender's email address.
     * @param receivers The list of receivers' email addresses.
     * @param subject  The subject of the email.
     * @param body     The body of the email.
     */
    private static void sendEmail(String sender, List<String> receivers, String subject, String body) {
        try (Socket socket = new Socket(SMTP_SERVER, SMTP_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            // SMTP communication logic
            reader.readLine();
            sendCommand(writer, "ehlo localhost");

            int index = 0;
            while (index != -1) {
                String answer = reader.readLine();
                index = answer.indexOf("-");
            }

            sendCommand(writer, "MAIL FROM: <" + sender + ">");

            for (var receiver : receivers) {
                sendCommand(writer, "RCPT TO: <" + receiver + ">");
            }

            sendCommand(writer, "DATA");

            sendCommand(writer, "Content-Type: text/plain; charset=UTF-8");
            sendCommand(writer, "From: <" + sender + ">");
            StringBuilder allReceivers = new StringBuilder();
            for (var receiver : receivers) {
                allReceivers.append("<").append(receiver).append(">");
                if (!Objects.equals(receiver, receivers.get(receivers.size() - 1)))
                    allReceivers.append(", ");
            }
            sendCommand(writer, "To: " + allReceivers);
            sendCommand(writer, "Subject: =?UTF-8?B?" + Base64.getEncoder().encodeToString(subject.getBytes(StandardCharsets.UTF_8)) + "?=");
            sendCommand(writer, ""); // Empty line before the body
            sendCommand(writer, body);
            sendCommand(writer, "\r\n.\r");

        } catch (IOException e) {
            System.err.println("Client: exception while using client socket: " + e);
        }
    }

    /**
     * Sends a command to the SMTP server.
     *
     * @param writer  The BufferedWriter for sending commands.
     * @param command The command to be sent.
     * @throws IOException If an I/O error occurs.
     */
    private static void sendCommand(BufferedWriter writer, String command) throws IOException {
        writer.write(command + "\n");
        writer.flush();
    }

    /**
     * Divides a list into groups of two to five objects.
     *
     * @param list The list to be divided.
     * @param n The number of groups.
     * @return A list of groups.
     */
    private static List<List<String>> divideIntoGroups(List<String> list, int n) {
        List<List<String>> result = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            int groupSize = (int) (Math.random() * 4) + 2;

            List<String> tempList = new ArrayList<>(list); // Copy of the email list

            Collections.shuffle(tempList); // Shuffle the list to randomize email selection

            List<String> group = new ArrayList<>();
            for (int j = 0; j < groupSize && j < tempList.size(); j++) {
                group.add(tempList.get(j));
            }

            result.add(group);
        }

        return result;
    }


    /**
     * Selects a random message from a list of messages.
     *
     * @param messages The list of messages.
     * @return A random message.
     */
    private static Message getRandomMessage(List<Message> messages) {
        Random random = new Random();
        int index = random.nextInt(messages.size());
        return messages.get(index);
    }


    /**
     * Checks the validity of an email address based on the following criteria:
     * - The email address length should not exceed 254 characters.
     * - The email address must contain the '@' symbol.
     * - The email prefix follows an acceptable format: alphanumeric characters, periods, underscores, and dashes.
     * - The email domain follows an acceptable format: alphanumeric characters, dashes, and a valid top-level domain (TLD).
     *
     * @param address The email address to be validated.
     * @return {@code true} if the email address is valid, {@code false} otherwise.
     */
    private static boolean checkValidAddress(String address) {
        // Check that the email address does not exceed the authorised size
        if (address.length() > 254) {
            System.err.println("Error: The email address \"" + address + "\" is too long.");
            return false;
        }

        // Check if '@' symbol is present
        if (address.indexOf('@') == -1) {
            System.err.println("Error: Email address \"" + address + "\" does not contain the '@' symbol.");
            return false;
        }

        // Split the email address into prefix and domain
        String[] parts = address.split("@");
        String prefix = parts[0];
        String domain = parts[1];

        // Check email prefix format
        String prefixRegex = "[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*";
        if (!prefix.matches(prefixRegex)) {
            System.err.println("Error: Invalid email prefix format in \"" + address + "\".");
            return false;
        }

        // Check email domain format
        String domainRegex = "[a-zA-Z0-9]+(-[a-zA-Z0-9]+)*\\.[a-zA-Z]{2,}";
        if (!domain.matches(domainRegex)) {
            System.err.println("Error: Invalid email domain format in \"" + address + "\".");
            return false;
        }

        return true;
    }
}

