import java.math.BigInteger;

public class CommandLogin {
    protected MessageTextParser mMtp;
    protected String mIdent;
    protected String mPassword;
    protected String mCookie;
    protected int mServerPort;
    protected String mServerHostName;

    private final String sExpectedComment = "Monitor Version 2.2.1";

    public CommandLogin(String args) throws Exception {
        throw new Exception("Not Implemented");
    }

    public CommandLogin(
            MessageTextParser conn, 
            String ident, String password, String cookie,
            String serverHostName, int serverPort) {
        mMtp = conn;
        mIdent = ident;
        mPassword = password;
        mCookie = cookie;
        mServerHostName = serverHostName;
        mServerPort = serverPort;
    }

    protected boolean Banner() throws Exception {
        // Receive the banner
        MessageComment monBanner = (MessageComment) mMtp.recv();

        // Validate the banner
        if ( !monBanner.mComment.equals(sExpectedComment) ) { 
            System.out.println(
                    "Comment validation failed; exp = " + sExpectedComment + "; act = " + monBanner.mComment + ";");
            return false;
        }

        return true;
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

    protected boolean RequireIdent() throws Exception {
        // Get the require directive
        MessageRequire msg = (MessageRequire) mMtp.recv();

        // Validate require directive
        if ( !msg.mCommand.equals("IDENT") ) {
            System.out.println("Unexpected directive; exp = IDENT; act = " + msg.mCommand + ";");
            return false;
        }

        return true;
    }

    protected boolean Waiting() throws Exception {
        // Get the waiting directive
        MessageWaiting msg = (MessageWaiting) mMtp.recv();

        return true;
    }

    protected boolean HostPort() throws Exception {
        // Parse thru a message group
        ParserHelper ph = new ParserHelper(mMtp);
        if ( !ph.parseToCommand("HOST_PORT") ) {
            throw new Exception("Failed to find REQUIRE: HOST_PORT");
        }

        // Wait for a waiting message
        if ( !ph.parseToWait() ) { 
            throw new Exception("Failed to find end of message group");
        }

        // Create a host port message
        MessageHostPort msgHostPort = new MessageHostPort(mServerHostName, mServerPort);

        // Send the host port message
        mMtp.send(msgHostPort);

        return true;
    }

    protected boolean Alive() throws Exception {
        // Parse thru a message group
        ParserHelper ph = new ParserHelper(mMtp);
        if ( !ph.parseToCommand("ALIVE") ) {
            throw new Exception("Failed to find REQUIRE: ALIVE");
        }

        // Wait for a waiting message
        if ( !ph.parseToWait() ) { 
            throw new Exception("Failed to find end of message group");
        }

        // Create the Alive message
        MessageAlive msgAlive = new MessageAlive(mCookie);
        
        // Send the Alive message
        mMtp.send(msgAlive);

        return true;
    }

    protected boolean DiffieHellmanEx() throws Exception {
        // Create DH obj
        DiffieHellmanExchange diffieHellmanExchange = 
            new DiffieHellmanExchange();

        // Get public key from file
        BigInteger publicKey =
            diffieHellmanExchange.getDHParmMakePublicKey("DHKey");

        // Create an ident message
        MessageIdent msgIdent = new MessageIdent(mIdent, publicKey.toString());
        
        // Send the ident message
        mMtp.send(msgIdent);

        // Get the monitor DH exchange reply and use to start encryption
        // only the first line is unencrypted
        MessageResult msgResult = (MessageResult) mMtp.recv();

        // Validate the result message command type
        if ( !msgResult.mCommand.equals(msgIdent.directive()) ) {
            throw new Exception("Result type validation failed");
        }

        // Create an ident message out of the result message from the monitor
        MessageIdent identReply = new MessageIdent(msgResult.mResult); 

        System.out.println("CommandLogin [DiffieHellmanEx]: Got PK:\n\t" + identReply.mPublicKey);
        
        // Compute the shared secret
        BigInteger secret = diffieHellmanExchange.getSecret(identReply.mPublicKey);

        System.out.println("CommandLogin [DiffieHellmanEx]: Using secret:\n\t" + secret);

        // Create new stream codec
        CodecKarn codecKarn = new CodecKarn(secret);

        // Add crypto codec to the stream
        mMtp.addCodec(codecKarn);

        return true;
    }
    
    public boolean Execute() throws Exception {
        // Receive the banner
        if ( !Banner() ) {
            throw new Exception("Comment validation failed");
        }

        // TODO: Split this into two separate classes that differ on whether or not to do pwcsum validation (server does, client doesn't)
/*
        // Receive the password checksum
        if ( !PasswordCsum() ) { 
            throw new Exception("Password checksum validation failed");
        }
*/
        // Receive the require ident message
        if ( !RequireIdent() ) {
            throw new Exception("Require message validation failed");
        }

        // Receive the waiting message
        if ( !Waiting() ) { 
            throw new Exception("Waiting message validation failed");
        }

        // Do DH KEX
        if ( !DiffieHellmanEx() ) {
            throw new Exception("DH KEX failed");
        }

        // Handle the alive message
        if ( !Alive() ) { 
            throw new Exception("Failed to handle alive");
        }

        // TODO: HostPort is client only

        // Handle the host port directive
        if ( !HostPort() ) { 
            throw new Exception("Failed to handle host/port message");
        }

        // TODO: Server MAY receive a transfer or quit command at this point
        
        return true;
    }
}
