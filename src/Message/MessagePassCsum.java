public class MessagePassCsum extends Message {
    public String mChecksum;

    public MessagePassCsum() {
        mChecksum = "";
    }

    public MessagePassCsum(String checksum) {
        mChecksum = checksum;
    }

    public String directive() {
        return "PARTICIPANT_PASSWORD_CHECKSUM";
    }

    public String serialize() {
        return directive().concat(sDirDelimit).concat(sArgDelimit).concat(mChecksum);
    }
}