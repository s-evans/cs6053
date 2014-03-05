public class MessageAlive extends Message {
    public String mCookie;

    public MessageAlive() {
        mCookie = "";
    }

    public MessageAlive(String cookie) {
        mCookie = cookie;
    }

    public String directive() {
        return "ALIVE";
    }

    public String serialize() {
        return directive().concat(sArgDelimit).concat(mCookie);
    }
}