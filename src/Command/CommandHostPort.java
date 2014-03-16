
public class CommandHostPort extends Command {
    protected MessageTextParser mMtp;
    protected int mServerPort;
    protected String mServerHostName;

    public CommandHostPort(String args) throws Exception {
        super(args);
        throw new Exception("Not Implemented");
    }

    public CommandHostPort(MessageTextParser mtp,
            String serverHostName, int serverPort) throws Exception {
        mMtp = mtp;
        mServerPort = serverPort;
        mServerHostName = serverHostName;
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
