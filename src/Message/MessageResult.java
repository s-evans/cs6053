public class MessageResult extends Message {
    public String mCommand;
    public String mResult;

    public MessageResult() {
        mCommand = "";
        mResult = "";
    }

    public MessageResult(String command, String result) {
        mCommand = command;
        mResult = result;
    }

    public MessageResult(String args) throws Exception {
        // Parse the input string 
        String[] vals = args.split(" ");
        if ( vals.length != 2 ) {
            throw new Exception(new String("PARSE ERROR"));
        }

        // Populate from values
        mCommand = vals[0];
        mResult = args.substring(mCommand.length());
    }

    public String directive() {
        return "RESULT";
    }

    public String serialize() {
        return directive().concat(sDirDelimit).concat(sArgDelimit).
                concat(mCommand).concat(sArgDelimit).concat(mResult);
    }
}
