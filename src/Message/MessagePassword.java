public class MessagePassword extends Message {
    protected String mPassword;

    public MessagePassword() {
        mPassword = "";
    }

    public MessagePassword(String password) {
        mPassword = password;
    }

    public String directive() {
        return "PASSWORD";
    }

    public String serialize() {
        return directive().concat(sArgDelimit).concat(mPassword);
    }
}