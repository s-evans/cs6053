
public class CommandHostPort extends CommandRequire {
    protected int mServerPort;
    protected String mServerHostName;

    public CommandHostPort(MessageTextParser mtp,
            String serverHostName, int serverPort) throws Exception {
        // Create super class
        super(mtp);

        // Populate junk
        mServerPort = serverPort;
        mServerHostName = serverHostName;
    }

    public CommandHostPort(
            MessageTextParser mtp, String[] args) throws Exception {
        // Create super class
        super(mtp);

        // Populate junk
        mServerHostName = args[0];
        mServerPort = Integer.parseInt(args[1]);
    }

    public String Require() {
        return require();
    }

    static public String require() {
        return "HOST_PORT";
    }

    static public String verb() {
        return "host_port";
    }

    static public String usage() {
        return verb().concat(" <hostname> <port>");
    }

    static public String explain() {
        return "Specifies the parameters to use when the monitor issues a 'REQUIRE: HOST_PORT' command.";
    }

    protected boolean HostPort() throws Exception {
        // Create a host port message
        MessageHostPort msgHostPort = new MessageHostPort(mServerHostName, mServerPort);

        // Send the host port message
        mMtp.send(msgHostPort);

        // Get a result message (COMMAND_ERROR also possible)
        MessageResult msgResult = (MessageResult) mMtp.recv();

        // Validate the result message command type
        if ( !msgResult.mCommand.equals(msgHostPort.directive()) ) {
            throw new Exception("Result type validation failed");
        }
        
        return true;
    }

    public boolean Execute() throws Exception {
        return HostPort();
    }
}
