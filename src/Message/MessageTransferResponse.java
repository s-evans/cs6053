public class MessageTransferResponse extends Message {
    enum Response {
        ACCEPT,
        DECLINE
    }

    public Response mResponse;

    public MessageTransferResponse() {
        mResponse = Response.DECLINE;
    }

    public MessageTransferResponse(Response response) {
        mResponse = response;
    }

    public MessageTransferResponse(String args) throws Exception {
        // Clean up input 
        args = args.trim();

        // Parse the input string 
        String[] vals = args.split(" ");
        if ( vals.length != 1 ) {
            throw new Exception(new String("PARSE ERROR"));
        }

        // Get value
        if ( vals[0].equals("ACCEPTED") ) {
            mResponse = Response.ACCEPT; 
        } else if ( vals[0].equals("DECLINE") ) { 
            mResponse = Response.DECLINE;
        } else {
            throw new Exception( new String("Invalid value") );
        }
            
    }

    public String directive() {
        return "TRANSFER_RESPONSE";
    }

    protected String getResponse() {
        if ( mResponse == Response.ACCEPT ) {
            return "ACCEPT";
        } else {
            return "DECLINE";
        }
    }

    public String serialize() {
        return directive()
                .concat(sArgDelimit).concat(getResponse());
    }
}
