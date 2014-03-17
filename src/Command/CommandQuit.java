
public class CommandQuit extends Command {
    protected MessageTextParser mMtp;

    public CommandQuit(String args) throws Exception {
        super(args);
        throw new Exception("Not Implemented");
    }

    public CommandQuit(MessageTextParser mtp) throws Exception {
        mMtp = mtp;
    }

    protected boolean Quit() throws Exception {
        // Create a QUIT message
        MessageQuit msgQuit = new MessageQuit();

        // Send the quit message
        mMtp.send(msgQuit);

        // Get a result message (COMMAND_ERROR also possible)
        MessageResult msgResult = (MessageResult) mMtp.recv();

        // Validate the result message command type
        if ( !msgResult.mCommand.equals(msgQuit.directive()) ) {
            throw new Exception("Result type validation failed");
        }
        
        return false;
    }

    public boolean Execute() throws Exception {
        return Quit();
    }
}
