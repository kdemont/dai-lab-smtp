package ch.heig.dai.lab.smtp;

/**
 * Represents an email message with a subject and body.
 *
 * @author Julien Holzer
 * @author Kilian Demont
 */
class Message {
    private final String subject;
    private final String body;

    /**
     * Constructor for creating a Message object.
     *
     * @param subject The subject of the message.
     * @param body    The body of the message.
     */
    public Message(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    /**
     * Gets the subject of the message.
     *
     * @return The subject of the message.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Gets the body of the message.
     *
     * @return The body of the message.
     */
    public String getBody() {
        return body;
    }
}