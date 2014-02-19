public class MessageWaiting extends Message {
    public MessageWaiting() {

    }

    public MessageWaiting(String args) {

    }

    public String directive() {
        return "WAITING";
    }

    public String serialize() {
        return directive().concat(sDirDelimit);
    }
}