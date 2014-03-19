
public abstract class CommandTransfer extends Command {
    String mRecipientIdent; 
    Integer mAmount; 
    String mSenderIdent;

    public CommandTransfer(
            MessageTextParser mtp, String recipient, Integer amount, String sender) {
        // Create parent class
        super(mtp);

        // Populate common junk
        mRecipientIdent = recipient;
        mAmount = amount;
        mSenderIdent = sender;
    }

    public abstract boolean Execute() throws Exception; 
}
