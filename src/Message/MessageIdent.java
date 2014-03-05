public class MessageIdent extends Message {
    public String mUserName;
    public String mPublicKey;

    public MessageIdent() {
        mUserName = "";
        mPublicKey = "";
    }

    public MessageIdent(String args) throws Exception {
        // Parse the input string 
        String[] vals = args.split(" ");
        if ( vals.length > 2 || vals.length < 1 ) {
            throw new Exception(new String("PARSE ERROR"));
        }

        // Get the user name 
        mUserName = vals[0];
        mPublicKey = "";

        // Get the DH public key
        if ( vals.length == 2 ) { 
            mPublicKey = vals[1];
        }
    }

    public MessageIdent(String userName, String publicKey) {
        mUserName = userName;
        mPublicKey = publicKey;
    }

    public String directive() {
        return "IDENT";
    }

    public String serialize() {
        String optional = "";

        if ( !mPublicKey.equals("") ) {
            optional = sArgDelimit.concat(mPublicKey);
        }

        return directive().concat(sArgDelimit).concat(mUserName).concat(optional);
    }
}
