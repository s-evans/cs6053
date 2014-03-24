
public class CommandChangePassword extends CommandUser {

    protected String mCurrentPassword;
    protected String mNewPassword;

    public CommandChangePassword(MessageTextParser mtp, String[] args) {
        // Create super class
        super(mtp);

        // Internal junk
        mCurrentPassword = args[0];
        mNewPassword = args[1];
    }

    static public String verb() {
        return "change_password";
    }

    static public String usage() {
        return verb().concat(" <current-password> <new-password>");
    }

    static public String explain() {
        return "Issues the CHANGE_PASSWORD command following authentication with the monitor, and updates the associated identity file for the user.";
    }

    public boolean Execute() throws Exception {

        // Create CHANGE_PASSWORD message
        MessageChangePassword msgChPwd = new MessageChangePassword(
                mCurrentPassword, mNewPassword);

        // Send the CHANGE_PASSWORD message
        mMtp.send(msgChPwd);

        // Receive the RESULT message
        MessageResult msgResult = (MessageResult) mMtp.recv();

        // Parse the RESULT message for a CHANGE_PASSWORD response
        if ( !msgResult.mCommand.equals(msgChPwd.directive()) ) {
            throw new Exception("Unexpected result message type; exp = " + msgChPwd.directive() + "; act = " + msgResult.mCommand + ";");
        }

        // Validate the result value
        if ( msgResult.mResult.trim().split(" ").length != 1 ) {
            throw new Exception("Unexpected result data; recv'd = " + msgResult.mResult + ";");
        }
        if ( msgResult.mResult.trim().length() != 19 ) {
            throw new Exception("Unex   pected result data; recv'd = " + msgResult.mResult + ";");
        }

        // Print out the screen as a last ditch effort in case we fail at writing to file
        System.out.println("ident = " + mMtp.getIdent() + "; password = " + mNewPassword + "; cookie = " + msgResult.mResult + ";");

        // Create indent file object
        IdentFile identFile = new IdentFile(mMtp.getIdent());
        identFile.mPassword = mNewPassword;
        identFile.mCookie = msgResult.mResult;
        
        // Save off the data to file
        if ( !identFile.Write() ) {
            throw new Exception("Failed to write identity information to file!");
        }

        return true;
    }
}
