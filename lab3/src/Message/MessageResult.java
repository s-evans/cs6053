public class MessageResult extends Message {
    protected String mCommand;
    protected String mResult;

    public MessageResult() {
        mCommand = "";
        mResult = "";
    }

    public MessageResult(String command, String result) {
        mCommand = command;
        mResult = result;
    }

    public MessageResult(String args) {
        // TODO: Implement
    }

    public String directive() {
        return "RESULT";
    }

    public String serialize() {
        return directive().concat(sDirDelimit).concat(sArgDelimit).
                concat(mCommand).concat(sArgDelimit).concat(mResult);
    }
}