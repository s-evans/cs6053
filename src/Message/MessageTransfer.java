public class MessageTransfer extends Message {
    protected String mRecipientIdent;
    protected Integer mPointsRequested;
    protected String mSenderIdent;

    public MessageTransfer() {
        mRecipientIdent = "";
        mPointsRequested = 0;
        mSenderIdent = "";
    }

    public MessageTransfer(String recipient, Integer points, String sender) {
        mRecipientIdent = recipient;
        mPointsRequested = points;
        mSenderIdent = sender;
    }

    public MessageTransfer(String args) {
        // TODO: Implement
    }

    public String directive() {
        return "TRANSFER_REQUEST";
    }

    public String serialize() {
        return directive().concat(sDirDelimit)
                .concat(sArgDelimit).concat(mRecipientIdent)
                .concat(sArgDelimit).concat(mPointsRequested.toString())
                .concat(sArgDelimit).concat("FROM")
                .concat(sArgDelimit).concat(mSenderIdent);
    }
}