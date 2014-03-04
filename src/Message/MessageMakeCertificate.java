public class MessageMakeCertificate extends Message {
    protected String mV;
    protected String mN;

    public MessageMakeCertificate() {
        mV = "";
        mN = "";
    }

    public MessageMakeCertificate(String v, String n) {
        mV = v;
        mN = n;
    }

    public MessageMakeCertificate(String args) {
        // TODO: Implement
    }

    public String directive() {
        return "MAKE_CERTIFICATE";
    }

    public String serialize() {
        return directive()
                .concat(sArgDelimit).concat(mV)
                .concat(sArgDelimit).concat(mN);
    }
}