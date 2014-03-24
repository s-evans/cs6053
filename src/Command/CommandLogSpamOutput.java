
public class CommandLogSpamOutput extends Command {

    protected CodecSpamOutput mCodecSpam;

    public CommandLogSpamOutput(MessageTextParser mtp, String[] args) {
        // Create super class
        super(mtp);

        // Create our spam codec
        mCodecSpam = new CodecSpamOutput(mtp, args);

        // Add a log spam codec to the stream
        mMtp.addCodecFront(mCodecSpam);
    }

    static public String verb() {
        return "log_output";
    }

    static public String usage() {
        return verb().concat(" <command-from-monitor> ...");
    }

    static public String explain() {
        return "Arguments are a space separated list of text. This text should match something that the monitor is likely to send to a client. This is implemented as an output stream filter, pushed to the front of the filter list. Therefore, successive log-related verbs found on the CLI will supercede previous filters, and will be output first.";
    }

    public boolean Execute() throws Exception {
        return true;
    } 
}
