public class MessageTransferResponse extends Message {
    enum Response {
        ACCEPT,
        DECLINE
    }

    protected Response mResponse;

    protected String getResponse() {
        if ( mResponse == Response.ACCEPT ) {
            return "ACCEPT";
        } else {
            return "DECLINE";
        }
    }

    public MessageTransferResponse() {
        mResponse = Response.DECLINE;
    }

    public MessageTransferResponse(Response response) {
        mResponse = response;
    }

    public MessageTransferResponse(String args) {
        // TODO: Implement
    }

    public String directive() {
        return "TRANSFER_RESPONSE";
    }

    public String serialize() {
        return directive()
                .concat(sArgDelimit).concat(getResponse());
    }
}