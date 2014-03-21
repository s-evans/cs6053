public class MessageHostPort extends Message {
    public String mHostName;
    public Integer mPort;

    public void setPort(int port) {
        if ( port > 65000 || port < 2048 ) {
            throw new IllegalArgumentException();
        }

        mPort = port;
    }

    public MessageHostPort() {
        mHostName = "";
        setPort(2048);
    }

    public MessageHostPort(String hostName, int port) {
        mHostName = hostName;
        setPort(port);
    }

    public MessageHostPort(String args) throws Exception {
        // Parse the input string 
        String[] vals = args.split(" ");
        if ( vals.length != 2 ) {
            throw new Exception(new String("PARSE ERROR"));
        }

        // Get the values
        mHostName = vals[0];
        setPort(Integer.parseInt(vals[1]));
    }

    public String directive() {
        return "HOST_PORT";
    }

    public String serialize() {
        return directive()
                .concat(sArgDelimit).concat(mHostName)
                .concat(sArgDelimit).concat(mPort.toString());
    }
}
