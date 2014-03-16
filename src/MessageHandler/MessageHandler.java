import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class MessageHandler {
    protected MessageTextParser mMtp;
    protected HashMap<String, Command> mRequireMap;
    protected HashMap<String, Class<? extends Command>> mMessageMap;
    protected List<Command> mCommandList;

    public MessageHandler(MessageTextParser mtp) {
        mMtp = mtp;
		mRequireMap = new HashMap<String, Command>();
		mMessageMap = new HashMap<String, Class<? extends Command>>();
        mCommandList = new ArrayList<Command>();
    }

    // Add a command to execute when getting a REQUIRE message
    public void addMessageHandler(String directive, Command cmd) {
        mRequireMap.put(directive, cmd);
    }
    
    // Add a free form command to be executed in order following an open WAITING message
    public void addCommand(Command cmd) {
        mCommandList.add(cmd);
    }

    // Add a command to be created and executed given a standalone message
    public void addCommandHandler(String directive, Class<? extends Command> cmd) {
        mMessageMap.put(directive, cmd);
    }

    protected boolean handleRequire(Message msg) {
        MessageRequire msgReq; 

        try {
            // Handle require messages
            msgReq = (MessageRequire) msg;
        } catch ( Exception e ) {
            // That wasn't it!
            return true;
        }

        try {
            // Pull off the waiting message off the stream
            MessageWaiting msgWait = (MessageWaiting) mMtp.recv();
        } catch ( Exception e ) {
            // Not sure what the deal might be
            e.printStackTrace();
            return false;
        }

        try {
            // Look up the command object given the command string 
            Command cmd = mRequireMap.get(msgReq.mCommand);

            // Execute the command
            if ( !cmd.Execute() ) {
                System.out.println("Failed to execute command!");
                return false;
            }
        } catch ( Exception e ) {
            // Could be leaving stream in a bad state
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected boolean handleWaiting(Message msg) {
        try {
            // Handle waiting message that are received without a require message
            MessageWaiting msgWait = (MessageWaiting) msg;
        } catch ( Exception e ) {
            // No big deal
            return true;
        }

        // Check if we're all out of commands we want to execute
        if ( mCommandList.isEmpty() ) {
            System.out.println("No commands remaining");
            return false;
        }

        // Remove the first command from the list
        Command cmd = mCommandList.remove(0);

        try {
            // Execute the command 
            if ( !cmd.Execute() ) {
                System.out.println("Failed to execute command");
                return false;
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected boolean handleCommand(Message msg) {
        try {
            // Look up the message's directive in our map
            Class<? extends Command> c = mMessageMap.get(msg.directive());

            // Create an instance of the command using the message
            Command cmd = c.getDeclaredConstructor(MessageTextParser.class, msg.getClass()).newInstance(mMtp, msg);

            // Execute the command
            if ( !cmd.Execute() ) {
                System.out.println("Failed to execute command");
                return false;
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            return false;
        }
      
        return true;
    }

    // Begin handling messages
    public void run() throws Exception {

        while ( true ) {
            // Get a message from the stream
            Message msg = mMtp.recv();

            // Try it out as a REQUIRE message
            if ( !handleRequire(msg) ) {
                return;
            }

            // Try it out as a WAITING message
            if ( !handleWaiting(msg) ) {
                return;
            }

            // Try it out as a vanilla command message
            if ( !handleCommand(msg) ) {
                return;
            }
        }
    }
}
