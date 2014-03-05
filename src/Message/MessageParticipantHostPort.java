public class MessageParticipantHostPort extends Message {
    public String mIdent;

    public MessageParticipantHostPort() {
        mIdent = "";
    }

    public MessageParticipantHostPort(String ident) {
        mIdent = ident;
    }

    public String directive() {
        return "PARTICIPANT_HOST_PORT";
    }

    public String serialize() {
        return directive().concat(sArgDelimit).concat(mIdent);
    }
}