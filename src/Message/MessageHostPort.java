import org.w3c.dom.ranges.RangeException;

public class MessageHostPort extends Message {
    protected String mHostName;
    protected Integer mPort;

    protected void setPort(int port) {
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

    public MessageHostPort(String args) {
        // TODO: Implement
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