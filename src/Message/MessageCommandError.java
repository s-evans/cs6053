public class MessageCommandError extends Message {
    protected String mErrorMessage;

    public MessageCommandError() {
        mErrorMessage = "";
    }

    public MessageCommandError(String errorMessage) {
        mErrorMessage = errorMessage;
    }

    public String directive() {
        return "COMMAND_ERROR";
    }

    public String serialize() {
        return directive().concat(sDirDelimit).concat(sArgDelimit).concat(mErrorMessage);
    }
}