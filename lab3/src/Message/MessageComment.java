public class MessageComment extends Message {
    protected String mComment;

    public MessageComment() {
        mComment = "";
    }

    public MessageComment(String comment) {
        mComment = comment;
    }

    public String directive() {
        return "COMMENT";
    }

    public String serialize() {
        return directive().concat(sDirDelimit).concat(sArgDelimit).concat(mComment);
    }
}