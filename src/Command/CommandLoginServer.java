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

        return true;
    }
}
