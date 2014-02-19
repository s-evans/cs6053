public class MessagePublicKey extends Message {
    protected String mV;
    protected String mN;

    public MessagePublicKey() {
        mV = "";
        mN = "";
    }

    public MessagePublicKey(String v, String n) {
        mV = v;
        mN = n;
    }

    public MessagePublicKey(String args) {
        // TODO: Implement
    }

    public String directive() {
        return "PUBLIC_KEY";
    }

    public String serialize() {
        return directive()
                .concat(sArgDelimit).concat(mV)
                .concat(sArgDelimit).concat(mN);
    }
}