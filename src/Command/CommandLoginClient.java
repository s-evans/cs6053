import java.math.BigInteger;

public class CommandLoginClient extends CommandLogin {

    public CommandLoginClient(String args) throws Exception {
        super(args);
        throw new Exception("Not Implemented");
    }

    public CommandLoginClient(
            MessageTextParser conn, 
            String ident, String cookie) {
        super(conn, ident, cookie);
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
        
        return true;
    }
}
