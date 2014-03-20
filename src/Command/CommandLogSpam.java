
public class CommandLogSpam extends Command {

    protected CodecSpam mCodecSpam;

    public CommandLogSpam(MessageTextParser mtp, String[] args) {
        // Create super class
        super(mtp);

        // Create our spam codec
        mCodecSpam = new CodecSpam();
    }

    static public String verb() {
        return "inject_log";
    }

    static public String usage() {
        return verb();
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
