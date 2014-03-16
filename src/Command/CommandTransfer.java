
public abstract class CommandTransfer extends Command {
    String mRecipientIdent; 
    Integer mAmount; 
    String mSenderIdent;

    public CommandTransfer(String args) {
        throw new Exception("not implemented");
    }

    public CommandTransfer(
            String recipient, Integer amount, String sender) {
        mRecipientIdent = recipient;
        mAmount = amount;
        mSenderIdent = sender;
    }

    public abstract boolean Execute() throws Exception; 
}
