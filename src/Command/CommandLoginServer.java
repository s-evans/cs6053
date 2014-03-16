import java.math.BigInteger;

public class CommandLoginServer extends CommandLogin {
    protected String mPassword;

    public CommandLoginServer(String args) throws Exception {
        super(args);
        throw new Exception("Not Implemented");
    }

    public CommandLoginServer(
            MessageTextParser conn, 
            String ident, String cookie, String password) {
        super(conn, ident, cookie);

        mPassword = password;
    }

    protected boolean PasswordCsum() throws Exception {
        // Receive the message
        MessagePassCsum msg = (MessagePassCsum) mMtp.recv();

        // Create expected string
        String expected = SHA.perform(mPassword);

        // Validate the message
        if ( !msg.mChecksum.equals(expected) ) { 
            System.out.println(
                    "Password checksum validation failed; exp = " + expected + "; act = " + msg.mChecksum + ";");
            return false;
        }

        return true;
    }

    protected boolean Quit() throws Exception {
        // Parse thru a message group
        ParserHelper ph = new ParserHelper(mMtp);
        if ( !ph.parseToCommand("QUIT") ) {
            // May not get the QUIT message if our server is alive so just short circuit here
            return true;
        }

        // Wait for a waiting message
        if ( !ph.parseToWait() ) { 
            throw new Exception("Failed to find end of message group");
        }

        // Create a QUIT message
        MessageQuit msgQuit = new MessageQuit();

        // Send the quit message
        mMtp.send(msgQuit);

        // Get a result message (COMMAND_ERROR also possible)
        MessageResult msgResult = (MessageResult) mMtp.recv();

        // Validate the result message command type
        if ( !msgResult.mCommand.equals(msgQuit.directive()) ) {
            throw new Exception("Result type validation failed");
        }
        
        return true;
    }

    public boolean Execute() throws Exception {
        // Receive the banner
        if ( !Banner() ) {
            throw new Exception("Comment validation failed");
        }

        // Receive the password checksum
        if ( !PasswordCsum() ) { 
            throw new Exception("Password checksum validation failed");
        }

        // Do DH KEX
        if ( !DiffieHellmanEx() ) {
            throw new Exception("DH KEX failed");
        }

        // Handle the alive message
        if ( !Alive() ) { 
            throw new Exception("Failed to handle alive");
        }

        // TODO: This might need to move

        // Handle the quit message
        if ( !Quit() ) {
            throw new Exception("Failed to handle quit message");
        }

        return true;
    }
}
