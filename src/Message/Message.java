public abstract class Message {
    static public final String sArgDelimit = " ";
    static public final String sDirDelimit = ":";

    public Message(String args) {

    }

    public Message() {

    }

    // Returns the directive associated with the message
    public abstract String directive();

    // Returns the message
    public abstract String serialize();
}
