public class CommandLoginClient extends CommandLogin {

    public CommandLoginClient(
            MessageTextParser conn, String ident, String cookie) throws Exception {
        super(conn, ident, cookie);
    }

    public CommandLoginClient(
            MessageTextParser mtp, String[] args) throws Exception {
        // Create parent class
        super(mtp, args[0], null);

        // Read the creds from file
        IdentFile identFile = new IdentFile(args[0]);
        if ( !identFile.Read() ) {
            throw new Exception("Failed to read ident file");
        }

        // Set the cookie here
        mCookie = identFile.mCookie;
    }

    public String Require() {
        return require();
    }

    static public String require() {
        return "IDENT";
    }

    static public String verb() {
        return "login";
    }

    static public String usage() {
        return verb().concat(" <ident>");
    }

    static public String explain() {
        return "Specifies the parameters to use when the monitor issues a 'REQUIRE: IDENT' command.";
    }

    public boolean Execute() throws Exception {

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
