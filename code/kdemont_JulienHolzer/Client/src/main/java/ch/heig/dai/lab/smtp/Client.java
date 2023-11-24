/*
-----------------------------------------------------------------------------------
File name         : Client.java

Author(s)         : Kilian Demont and Julien Holzer

Creation date     : 09.11.2023

Description       : SMTP client application for sending emails to groups of victims
                    using messages read from JSON files.

                    The program reads victim and message lists from JSON files, shuffles
                    the list of victims, divides them into n groups, and sends emails to
                    each group using a random message from the messages list.

                    Messages and victims are read from "messages.json" and "victims.json"
                    files respectively.

-----------------------------------------------------------------------------------
*/

package ch.heig.dai.lab.smtp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The main client class for sending emails to groups of victims.
 */
public class Client {
    private static final String SMTP_SERVER = "localhost";
    private static final int SMTP_PORT = 1025;

    /**
     * Main method to initiate the email sending process.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        // Read the number of groups provided as a command line argument
        int n = 10; //Integer.parseInt(args[0]); // Uncomment at the end

        // Read victim and message lists from JSON files
        List<String> victims = readFromJsonFile("/victims.json", "victims");
        List<Message> messages = readMessagesFromJsonFile("/messages.json", "messages");

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
            System.out.println("Client: exception while reading json: " + e);
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
            System.out.println("Client: exception while reading json: " + e);
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
                System.out.println(answer);
            }

            sendCommand(writer, "MAIL FROM: <" + sender + ">");
            String test1 = reader.readLine();

            for (var receiver : receivers) {
                sendCommand(writer, "RCPT TO: <" + receiver + ">");
                test1 = reader.readLine();
                System.out.println(test1);
            }

            sendCommand(writer, "DATA");
            test1 = reader.readLine();
            System.out.println(test1);

            sendCommand(writer, "From: <" + sender + ">");
            StringBuilder allReceivers = new StringBuilder();
            for (var receiver : receivers) {
                allReceivers.append("<").append(receiver).append(">");
                if (!Objects.equals(receiver, receivers.get(receivers.size() - 1)))
                    allReceivers.append(", ");
            }
            System.out.println(allReceivers);
            sendCommand(writer, "To: " + allReceivers);
            sendCommand(writer, "Subject: " + subject);
            sendCommand(writer, ""); // Empty line before the body
            sendCommand(writer, body);
            sendCommand(writer, "\r\n.\r");
            test1 = reader.readLine();
            System.out.println(test1);

        } catch (IOException e) {
            System.out.println("Client: exception while using client socket: " + e);
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
}

