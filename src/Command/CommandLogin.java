import java.math.BigInteger;

public abstract class CommandLogin {
    protected MessageTextParser mMtp;
    protected String mIdent;
    protected String mPassword;
    protected String mCookie;

    private final String sExpectedComment = "Monitor Version 2.2.1";

    public CommandLogin(String args) throws Exception {
        throw new Exception("Not Implemented");
    }

    public CommandLogin(
            MessageTextParser conn, 
            String ident, String password, String cookie) {
        mMtp = conn;
        mIdent = ident;
        mPassword = password;
        mCookie = cookie;
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
        System.out.println("CommandLogin [Alive]: Using cookie: \n\t" + mCookie);
        MessageAlive msgAlive = new MessageAlive(mCookie);
        
        // Send the Alive message
        mMtp.send(msgAlive);

        // Get a result message (COMMAND_ERROR also possible)
        MessageResult msgResult = (MessageResult) mMtp.recv();

        // Validate the result message command type
        if ( !msgResult.mCommand.equals(msgAlive.directive()) ) {
            throw new Exception("Result type validation failed");
        }
        
        // Validate the result code  
        if ( !msgResult.mResult.contains("Identity has been verified.") ) {
            throw new Exception("Result message validation failed");
        }

        return true;
    }

    protected boolean DiffieHellmanEx() throws Exception {
        // Parse thru a message group
        ParserHelper ph = new ParserHelper(mMtp);
        if ( !ph.parseToCommand("IDENT") ) {
            throw new Exception("Failed to find REQUIRE: IDENT");
        }

        // Wait for a waiting message
        if ( !ph.parseToWait() ) { 
            throw new Exception("Failed to find end of message group");
        }

        // Create DH obj
        PlantDHKey.plantKey();
        DiffieHellmanExchange diffieHellmanExchange = new DiffieHellmanExchange();

        // Get public key from file
        BigInteger publicKey =
            diffieHellmanExchange.getDHParmMakePublicKey("DHKey");
        
        // Create an IDENT message
        MessageIdent msgIdent = new MessageIdent(mIdent, publicKey.toString(32));

        // Send the IDENT message
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
    
    public abstract boolean Execute() throws Exception; 
}
