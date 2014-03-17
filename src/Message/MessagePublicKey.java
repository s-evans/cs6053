public class MessagePublicKey extends Message {
    public String mV;
    public String mN;

    public MessagePublicKey() {
        mV = "";
        mN = "";
    }

    public MessagePublicKey(String v, String n) {
        mV = v;
        mN = n;
    }

    public MessagePublicKey(String args) throws Exception {
        args = args.trim();

        // Parse the input string 
        String[] vals = args.split(" ");
        if ( vals.length != 2 ) {
            throw new Exception(new String(
                        "Unexpected arg count; exp = " + 2 + "; act = " + vals.length + ";"));
        }

        // Populate from values
        mV = vals[0];
        mN = vals[1];
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
