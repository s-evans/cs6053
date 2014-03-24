import java.math.BigInteger;

public abstract class CommandLogin extends CommandRequire {
    protected String mIdent;
    protected String mCookie;

    public CommandLogin(
            MessageTextParser conn, 
            String ident, String cookie) {
        super(conn);
        mIdent = ident;
        mCookie = cookie;
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
            throw new Exception("Unexpected result message type; exp = " + msgAlive.directive() + "; act = " + msgResult.mCommand + ";");
        }
        
        // Validate the result code  
        final String exp = "Identity has been verified.";
        if ( !msgResult.mResult.contains(exp) ) {
            throw new Exception("Result message validation failed; exp = " + exp + "; act = " + msgResult.mCommand + ";");
        }

        // Set that we are now authenticated
        mMtp.isAuthenticated(true);

        // Save the ident of the current connection
        mMtp.setIdent(mIdent);

        return true;
    }

    protected boolean DiffieHellmanEx() throws Exception {

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
            throw new Exception("Unexpected result message type; exp = " + msgIdent.directive() + "; act = " + msgResult.mCommand + ";");
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
