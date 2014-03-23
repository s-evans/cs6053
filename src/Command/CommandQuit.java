
public class CommandQuit extends CommandRequire {

    public CommandQuit(MessageTextParser mtp) throws Exception {
        super(mtp);
    }

    public String Require() {
        return "QUIT";
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
        
        System.out.println("Got quit command, ending connection.");
        
        return false;
    }

    public boolean Execute() throws Exception {
        return Quit();
    }
}
