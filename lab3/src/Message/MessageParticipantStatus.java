public class MessageParticipantStatus extends Message {
    public MessageParticipantStatus() {

    }

    public MessageParticipantStatus(String args) {

    }

    public String directive() {
        return "PARTICIPANT_STATUS";
    }

    public String serialize() {
        return directive();
    }
}