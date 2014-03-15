import java.math.BigInteger;

public class CommandLoginClient extends CommandLogin {
    protected int mServerPort;
    protected String mServerHostName;

    public CommandLoginClient(String args) throws Exception {
        super(args);
        throw new Exception("Not Implemented");
    }

    public CommandLoginClient(
            MessageTextParser conn, 
            String ident, String cookie,
            String serverHostName, int serverPort) {
        super(conn, ident, cookie);

        mServerHostName = serverHostName;
        mServerPort = serverPort;
    }

    protected boolean HostPort() throws Exception {
        // Parse thru a message group
        ParserHelper ph = new ParserHelper(mMtp);
        if ( !ph.parseToCommand("HOST_PORT") ) {
            // May not get the HOST_PORT message if our server is alive so just short circuit
            return true;
        }

        // Wait for a waiting message
        if ( !ph.parseToWait() ) { 
            throw new Exception("Failed to find end of message group");
        }

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
        // Receive the banner
        if ( !Banner() ) {
            throw new Exception("Comment validation failed");
        }

        // Do DH KEX
        if ( !DiffieHellmanEx() ) {
            throw new Exception("DH KEX failed");
        }

        // Handle the alive message
        if ( !Alive() ) { 
            throw new Exception("Failed to handle alive");
        }

        // Handle the host port directive
        if ( !HostPort() ) { 
            throw new Exception("Failed to handle host/port message");
        }
        
        return true;
    }
}
