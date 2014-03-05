public abstract class Message {
    public Message(String args) {

    }

    public Message() {

    }

    static public final String sArgDelimit = " ";
    static public final String sDirDelimit = ":";

    // Returns the directive associated with the message
    public abstract String directive();

    // Returns the message
    public abstract String serialize();
}