package ch.heig.dai.lab.smtp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

/**
 * The JsonGenerator class generates and saves JSON data for victims and messages.
 *
 *  @author Julien Holzer
 *  @author Kilian Demont
 */
public class JsonGenerator {

    /**
     * The main method for running the JSON generation and saving process.
     *
     * @param args Command-line arguments (not used in this application).
     */
    public static void main(String[] args) {
        generateAndSaveVictims();
        generateAndSaveMessages();
    }

    /**
     * Generates and saves JSON data for victim email addresses.
     */
    private static void generateAndSaveVictims() {
        // Create a JSON array for the victims list
        JSONArray victimsArray = new JSONArray();

        // Generate more than 50 victim emails
        for (int i = 1; i <= 50; i++) {
            victimsArray.put("victim" + i + "@example.com");
        }

        // Create a JSON object for the victims data
        JSONObject victimsObject = new JSONObject();
        victimsObject.put("victims", victimsArray);

        // Convert the JSON object to a string
        String jsonString = victimsObject.toString();

        // Save the JSON string to a file
        saveJsonToFile(jsonString, "code/kdemont_JulienHolzer/Client/src/main/resources/victims.json");
    }

    /**
     * Generates and saves JSON data for email messages.
     */
    private static void generateAndSaveMessages() {
        // Create a JSON array for the messages list
        JSONArray messagesArray = new JSONArray();

        // Generate more than 50 messages
        for (int i = 1; i <= 50; i++) {
            JSONObject messageObject = new JSONObject();
            messageObject.put("subject", "✨Subject " + i + "✨");
            messageObject.put("body", "✨Message " + i + "✨");

            messagesArray.put(messageObject);
        }

        // Create a JSON object for the messages data
        JSONObject messagesObject = new JSONObject();
        messagesObject.put("messages", messagesArray);

        // Convert the JSON object to a string
        String jsonMessagesString = messagesObject.toString();

        // Save the JSON string to a file
        saveJsonToFile(jsonMessagesString, "code/kdemont_JulienHolzer/Client/src/main/resources/messages.json");
    }

    /**
     * Saves a JSON string to a specified file.
     *
     * @param json     The JSON string to be saved.
     * @param fileName The name of the file to which the JSON data will be saved.
     */
    private static void saveJsonToFile(String json, String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(json);
            System.out.println("JSON data saved to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
