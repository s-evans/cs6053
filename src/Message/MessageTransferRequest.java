public class MessageTransferRequest extends Message {
    public String mRecipientIdent;
    public Integer mPointsRequested;
    public String mSenderIdent;

    public MessageTransferRequest() {
        mRecipientIdent = "";
        mPointsRequested = 0;
        mSenderIdent = "";
    }

    public MessageTransferRequest(String recipient, Integer points, String sender) {
        mRecipientIdent = recipient;
        mPointsRequested = points;
        mSenderIdent = sender;
    }

    public MessageTransferRequest(String args) throws Exception {
        // Parse the input string 
        String[] vals = args.split(" ");
        if ( vals.length != 4 ) {
            throw new Exception(new String("PARSE ERROR"));
        }

        // Validate the FROM string
        if ( !vals[2].equals("FROM") ) { 
            throw new Exception(new String("Validation error"));
        }

        // Get and validate values
        mRecipientIdent = vals[0];
        mPointsRequested = Integer.parseInt(vals[1]);
        mSenderIdent = vals[3];
    }

    public String directive() {
        return "TRANSFER_REQUEST";
    }

    public String serialize() {
        return directive()
                .concat(sArgDelimit).concat(mRecipientIdent)
                .concat(sArgDelimit).concat(mPointsRequested.toString())
                .concat(sArgDelimit).concat("FROM")
                .concat(sArgDelimit).concat(mSenderIdent);
    }
}
