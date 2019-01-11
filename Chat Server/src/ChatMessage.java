import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.
    private int type;
    private String message;
    private String recipient;

    public ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public ChatMessage(int type, String message, String recipient) {
        this(type, message);
        this.recipient = recipient;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }


    public String getRecipient() {
        return recipient;
    }
}
