public class MessageChangePassword extends Message {
    public String mCurrentPassword;
    public String mNewPassword;

    public MessageChangePassword() {
        mCurrentPassword = "";
        mNewPassword = "";
    }

    public MessageChangePassword(String currentPassword, String newPassword) {
        mCurrentPassword = currentPassword;
        mNewPassword = newPassword;
    }

    public MessageChangePassword(String args) throws Exception {
        // Parse the input string 
        String[] vals = args.split(" ");
        if ( vals.length != 2 ) {
            throw new Exception(new String("PARSE ERROR"));
        }

        // Get values
        mCurrentPassword = vals[0];
        mNewPassword = vals[1];
    }

    public String directive() {
        return "CHANGE_PASSWORD";
    }

    public String serialize() {
        return directive().
                concat(sArgDelimit).concat(mCurrentPassword).
                concat(sArgDelimit).concat(mNewPassword);
    }
}
