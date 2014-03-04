public class MessageChangePassword extends Message {
    protected String mCurrentPassword;
    protected String mNewPassword;

    public MessageChangePassword() {
        mCurrentPassword = "";
        mNewPassword = "";
    }

    public MessageChangePassword(String currentPassword, String newPassword) {
        mCurrentPassword = currentPassword;
        mNewPassword = newPassword;
    }

    public MessageChangePassword(String args) {
        // TODO: Implement
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