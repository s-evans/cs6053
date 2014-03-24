
public class CommandLogSpamInput extends Command {

    protected CodecSpamInput mCodecSpam;

    public CommandLogSpamInput(MessageTextParser mtp, String[] args) {
        // Create super class
        super(mtp);

        // Create our spam codec
        mCodecSpam = new CodecSpamInput(mtp, args);

        // Add a log spam codec to the stream
        mMtp.addCodecFront(mCodecSpam);
    }

    static public String verb() {
        return "log_input";
    }

    static public String usage() {
        return verb().concat(" <command-from-client> ...");
    }

    static public String explain() {
        return "Arguments are a space separated list of text. This text should match something that the monitor is likely to receive from a client. This is implemented as an output stream filter, pushed to the front of the filter list. Therefore, successive log-related verbs found on the CLI will supercede previous filters, and will be output first.";
    }

    public boolean Execute() throws Exception {
        return true;
    } 
}
