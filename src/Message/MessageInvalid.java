public class MessageInvalid extends Message {

    // NOTE: This is not a message that exists on the wire
    // Instead this just represents a bad or unhandled message

    public MessageInvalid() {

    }

    public MessageInvalid(String args) {

    }

    public String directive() {
        return null;
    }

    public String serialize() {
        return null;
    }
}
