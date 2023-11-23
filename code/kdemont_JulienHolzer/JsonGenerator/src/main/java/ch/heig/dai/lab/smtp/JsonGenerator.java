package ch.heig.dai.lab.smtp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class JsonGenerator {
    public static void main(String[] args) {
        generateAndSaveVictims();
        generateAndSaveMessages();
    }

    private static void generateAndSaveVictims() {
        // Create a JSON array for the victims list
        JSONArray victimsArray = new JSONArray();

        // Generate more than 50 victim emails
        for (int i = 1; i <= 50; i++) {
            victimsArray.put("<victim" + i + "@example.com>");
        }

        // Create a JSON object for the victims data
        JSONObject victimsObject = new JSONObject();
        victimsObject.put("victims", victimsArray);

        // Convert the JSON object to a string
        String jsonString = victimsObject.toString();

        // Save the JSON string to a file
        saveJsonToFile(jsonString, "code/kdemont_JulienHolzer/Client/src/main/resources/victims.json");
    }

    private static void generateAndSaveMessages() {
        // Create a JSON array for the messages list
        JSONArray messagesArray = new JSONArray();

        // Generate more than 50 messages
        for (int i = 1; i <= 50; i++) {
            messagesArray.put("Subject " + i);
            messagesArray.put("Message " + i);
        }

        // Create a JSON object for the messages data
        JSONObject messagesObject = new JSONObject();
        messagesObject.put("messages", messagesArray);

        // Convert the JSON object to a string
        String jsonMessagesString = messagesObject.toString();

        // Save the JSON string to a file
        saveJsonToFile(jsonMessagesString, "code/kdemont_JulienHolzer/Client/src/main/resources/messages.json");
    }

    private static void saveJsonToFile(String json, String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(json);
            System.out.println("JSON data saved to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
