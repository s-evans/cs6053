public class CommandLoginServer extends CommandLogin {
    protected String mPassword;

    public CommandLoginServer(
            MessageTextParser conn, 
            String ident, String cookie, String password) throws Exception {
        // Create parent class
        super(conn, ident, cookie);

        // Save off values
        mPassword = password;
    }

    public CommandLoginServer( 
            MessageTextParser mtp, String ident) throws Exception {
        // Create the parent class
        super(mtp, ident, null);

        // Read the creds from file
        IdentFile identFile = new IdentFile(ident);
        if ( !identFile.Read() ) {
            throw new Exception("Failed to read ident file");
        }

        // Use data from the file
        mCookie = identFile.mCookie;
        mPassword = identFile.mPassword;
    }

    public String Require() {
        return "IDENT";
    }

    protected boolean PasswordCsum() throws Exception {

        // Create expected string
        String expected = SHA.perform(mPassword);

        // Get the string received from the PARTICIPANT_PASSWORD_CHECKSUM message
        String received = mMtp.getCsum();

        // Validate received string pointer
        if ( received == null ) {
            throw new Exception("Started login before receiving password csum!");
        }

        // Validate the message
        if ( !received.equals(expected) ) { 
            System.out.println(
                    "Password checksum validation failed; exp = " + expected + "; act = " + received + ";");
            return false;
        }

        return true;
    }

    public boolean Execute() throws Exception {

        // Validate the password checksum
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

        return true;
    }
}
