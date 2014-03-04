public class MessageRandomParticipant extends Message {
    public MessageRandomParticipant() {

    }

    public MessageRandomParticipant(String arg) {

    }

    public String directive() {
        return "RANDOM_PARTICIPANT_HOST_PORT";
    }

    public String serialize() {
        return directive();
    }
}