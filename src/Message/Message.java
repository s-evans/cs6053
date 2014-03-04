public abstract class Message {
    public Message(String args) {

    }

    public Message() {

    }

    static protected final String sArgDelimit = " ";
    static protected final String sDirDelimit = ":";

    // Returns the directive associated with the message
    public abstract String directive();

    // Returns the message
    public abstract String serialize();
}