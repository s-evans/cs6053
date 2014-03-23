
public class CommandLogSpamInput extends CommandUser {

    protected CodecSpamInput mCodecSpam;

    public CommandLogSpamInput(MessageTextParser mtp, String[] args) {
        // Create super class
        super(mtp);

        // Create our spam codec
        mCodecSpam = new CodecSpamInput(args);
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

    // TODO: There is likely a much better solution but I'm tired

    // Induce another WAITING Message from the monitor
    protected void badDesign() throws Exception {
        // Create a message
        MessageParticipantStatus msgPs = new MessageParticipantStatus();

        // Send the message
        mMtp.send(msgPs);

        // Get the result message
        MessageResult msgResult = (MessageResult) mMtp.recv();
    }

    public boolean Execute() throws Exception {
        // Add a log spam codec to the stream
        mMtp.addCodecFront(mCodecSpam);

        // Need to get the monitor to say something again at this point, otherwise we'll be waiting on each other forever
        badDesign();
        
        return true;
    } 
}
