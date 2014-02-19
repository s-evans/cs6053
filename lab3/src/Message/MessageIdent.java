public class MessageIdent extends Message {
    protected String mUserName;
    protected String mPublicKey;

    public MessageIdent() {
        mUserName = "";
        mPublicKey = "";
    }

    public MessageIdent(String args) {
        // TODO: Implmeent
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