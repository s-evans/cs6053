public class MessageRequire extends Message {
    protected String mCommand;

    public MessageRequire() {
        mCommand = "";
    }

    public MessageRequire(String command) {
        mCommand = command;
    }

    public String directive() {
        return "REQUIRE";
    }

    public String serialize() {
        return directive().concat(sDirDelimit).concat(sArgDelimit).concat(mCommand);
    }
}