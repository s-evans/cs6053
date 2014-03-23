
public class CommandPasswordCsum extends Command {
    protected String mCsum;

    public CommandPasswordCsum(
            MessageTextParser mtp, MessagePassCsum msg) {
        // Create super class
        super(mtp);

        // Get data from the message
        mCsum = msg.mChecksum;
    }

    public static String Directive() {
        return "PARTICIPANT_PASSWORD_CHECKSUM";
    }

    public boolean Execute() throws Exception {
        // Save this dude for later
        mMtp.setCsum(mCsum);
        return true;
    }
}
