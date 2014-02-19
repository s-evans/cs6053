public class MessageGameIdents extends Message {
    public MessageGameIdents() {

    }

    public MessageGameIdents(String args) {

    }

    public String directive() {
        return "GET_GAME_IDENTS";
    }

    public String serialize() {
        return directive();
    }
}