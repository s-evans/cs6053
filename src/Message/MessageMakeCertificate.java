public class MessageMakeCertificate extends Message {
    public String mV;
    public String mN;

    public MessageMakeCertificate() {
        mV = "";
        mN = "";
    }

    public MessageMakeCertificate(String v, String n) {
        mV = v;
        mN = n;
    }

    public MessageMakeCertificate(String args) throws Exception {
        // Parse the input string 
        String[] vals = args.split(" ");
        if ( vals.length != 2 ) {
            throw new Exception(new String("PARSE ERROR"));
        }

        // Populate from values
        mV = vals[0];
        mN = vals[1];
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
