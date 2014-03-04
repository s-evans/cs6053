public class MessageSignOff extends Message {
    public MessageSignOff() {

    }

    public MessageSignOff(String args) {

    }

    public String directive() {
        return "SIGN_OFF";
    }

    public String serialize() {
        return directive();
    }
}