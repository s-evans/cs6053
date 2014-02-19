public class MessageRounds extends Message {
    protected Integer mRounds;

    public MessageRounds() {
        mRounds = 0;
    }

    public MessageRounds(int rounds) {
        mRounds = rounds;
    }

    public MessageRounds(String args) {
        // TODO: Implement
    }

    public String directive() {
        return "ROUNDS";
    }

    public String serialize() {
        return directive().concat(sArgDelimit).concat(mRounds.toString());
    }
}