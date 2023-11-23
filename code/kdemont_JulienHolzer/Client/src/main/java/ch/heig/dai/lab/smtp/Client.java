package ch.heig.dai.lab.smtp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.*;

import static java.nio.charset.StandardCharsets.*;

public class Client {
    private static final String SMTP_SERVER = "localhost";
    private static final int SMTP_PORT = 1025;

    public static void main(String[] args) {
        // Read the number of groups provided as a command line argument
        int n = 10; //Integer.parseInt(args[0]); //décommenter à la fin

        // Read victim and message lists from JSON files
        List<String> victims = readFromJsonFile(Client.class.getResource("/victims.json").getFile(), "victims");
        List<String> messages = readFromJsonFile(Client.class.getResource("/messages.json").getFile(), "messages");
        //TODO : changer la logique des messages en deux array ou autre structure


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
            String[] subjectAndMessage = getRandomSubjectAndMessage(messages);

            // Send emails to the group
            sendEmail(sender, receivers, subjectAndMessage[0], subjectAndMessage[1]);
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
            System.out.println("Client: exception while reading json: " + e);
            return List.of(); // Return an empty list in case of an error
        }
    }

private static void sendEmail(String sender, List<String> receivers, String subject, String body) {
    try (Socket socket = new Socket(SMTP_SERVER, SMTP_PORT);
         BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8))) {

        // SMTP communication logic...

        reader.readLine();
        sendCommand(writer, "ehlo localhost");

        int index = 0;
        while (index != -1) {
            String answer = reader.readLine();
            index = answer.indexOf("-");
            System.out.println(answer);
        }

        sendCommand(writer, "MAIL FROM: " + sender);
        String test1 = reader.readLine();

        for (var receiver : receivers) {
            sendCommand(writer, "RCPT TO: " + receiver);
            test1 = reader.readLine();
            System.out.println(test1);
        }
        sendCommand(writer, "DATA");
        test1 = reader.readLine();
        System.out.println(test1);

        sendCommand(writer, "From: " + sender);
        StringBuilder allReceivers = new StringBuilder();
        for (var receiver : receivers) {
            allReceivers.append(receiver);
            if (!Objects.equals(receiver, receivers.getLast()))
                allReceivers.append(", ");
        }
        System.out.println(allReceivers);
        sendCommand(writer, "To: " + allReceivers);
        sendCommand(writer, "Subject: " + subject);
        sendCommand(writer, ""); // Empty line before the body
        sendCommand(writer, body);
        sendCommand(writer, "\r\n.\r");
        test1 = reader.readLine();
        System.out.println(test1); // SMTP communication logic...

    } catch (IOException e) {
        System.out.println("Client: exception while using client socket: " + e);
    }
}
    private static void sendCommand(BufferedWriter writer, String command) throws IOException {
        writer.write(command + "\n");
        writer.flush();
    }

    private static List<List<String>> divideIntoGroups(List<String> list, int n) {
        // Shuffle the list to randomize the order
        Collections.shuffle(list);

        // Divide the list into n groups
        int groupSize = Math.min(5, Math.max(2, list.size() / n));
        return partitionList(list, groupSize);
    }

    private static <T> List<List<T>> partitionList(List<T> list, int partitionSize) {
        int size = list.size();
        int partitions = (int) Math.ceil((double) size / partitionSize);

        List<List<T>> partitionedList = new ArrayList<>(partitions);

        for (int i = 0; i < size; i += partitionSize) {
            int end = Math.min(size, i + partitionSize);
            partitionedList.add(new ArrayList<>(list.subList(i, end)));
        }

        return partitionedList;
    }

    private static String[] getRandomSubjectAndMessage(List<String> messages) {
        Random random = new Random();
        int index = random.nextInt(messages.size());
        if (messages.size() % 2 == 1) {
            index--;
        }

        return new String[]{messages.get(index), messages.get(++index)};
    }

}
