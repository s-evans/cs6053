import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class MessageHandler {
    protected MessageTextParser mMtp;
    protected HashMap<String, Command> mMap;
    protected List<Command> mCommandList;

    public MessageHandler(MessageTextParser mtp) {
        mMtp = mtp;
		mMap = new HashMap<String, Command>();
        mCommandList = new ArrayList<Command>();
    }

    // Add a command to execute when getting a REQUIRE message
    public void addMessageHandler(String directive, Command cmd) {
        mMap.put(directive, cmd);
    }
    
    // Add a free form command to be executed in order following an open WAITING message
    public void addCommand(Command cmd) {
        mCommandList.add(cmd);
    }

    // Begin handling messages
    public void run() throws Exception {

        while ( true ) {
            // Get a message from the stream
            Message msg = mMtp.recv();

            try {
                // Handle require messages
                MessageRequire msgReq = (MessageRequire) msg;

                // Pull off the waiting message off the stream
                MessageWaiting msgWait = (MessageWaiting) mMtp.recv();

                // TODO: Think about how to handle cases where we're not handling certain commands

                // Look up the command object given the command string 
                Command cmd = mMap.get(msgReq.mCommand);

                // Execute the command
                if ( !cmd.Execute() ) {
                    System.out.println("Failed to execute command!");
                }
            } catch (Exception e) {
                // Ignore
            }

            try {
                // Handle waiting message that are received without a require message
                MessageWaiting msgWait = (MessageWaiting) msg;

                // Check if we're all out of commands we want to execute
                if ( mCommandList.isEmpty() ) {
                    System.out.println("No commands remaining");
                    return;
                }

                // Remove the first command from the list
                Command cmd = mCommandList.remove(0);

                // Execute the command 
                if ( !cmd.Execute() ) {
                    System.out.println("Failed to execute command");
                }
            } catch (Exception e) {
                // Ignore 
            }

            // TODO: Add in handling for cases like the server's handling of the transfer message

        }
    }
}
