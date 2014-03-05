public class MessageGetCertificate extends Message {
    public String mIdent;

    public MessageGetCertificate() {
        mIdent = "";
    }

    public MessageGetCertificate(String ident) {
        mIdent = ident;
    }

    public String directive() {
        return "GET_CERTIFICATE";
    }

    public String serialize() {
        return directive().concat(sArgDelimit).concat(mIdent);
    }
}