
public abstract class CommandTransfer extends Command {
    MessageTextParser mMtp;
    String mRecipientIdent; 
    Integer mAmount; 
    String mSenderIdent;

    public CommandTransfer(String args) throws Exception {
        throw new Exception("not implemented");
    }

    public CommandTransfer(
            MessageTextParser mtp, String recipient, Integer amount, String sender) {
        mMtp = mtp;
        mRecipientIdent = recipient;
        mAmount = amount;
        mSenderIdent = sender;
    }

    public abstract boolean Execute() throws Exception; 
}
