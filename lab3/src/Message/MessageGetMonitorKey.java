public class MessageGetMonitorKey extends Message {
    public MessageGetMonitorKey() {

    }

    public MessageGetMonitorKey(String args) {

    }

    public String directive() {
        return "GET_MONITOR_KEY";
    }

    public String serialize() {
        return directive();
    }
}