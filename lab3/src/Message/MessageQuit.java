public class MessageQuit extends Message {
    public MessageQuit() {

    }

    public MessageQuit(String args) {

    }

    public String directive() {
        return "QUIT";
    }

    public String serialize() {
        return directive();
    }
}