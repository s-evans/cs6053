
public class ParserHelper {
    protected MessageTextParser mMtp;

    public ParserHelper(MessageTextParser mtp) {
        mMtp = mtp;
    }
    
    public boolean parseToWait () throws Exception {
        boolean found = false;

        // Parse thru to the end of a message group
        for ( int i = 0 ; i < 50 && !found ; i++ ) { 
            try {
                // Get a message
                MessageWaiting msgWait = (MessageWaiting) mMtp.recv();
            } catch ( Exception e ) {
                System.out.println("ignoring message");
                continue;
            }

            found = true;
        } 

        return found;
    }

    public boolean parseToCommand (String directive) throws Exception {
        boolean found = false;

        // Parse thru a message group
        for ( int i = 0 ; i < 50 && !found ; i++ ) { 
            // Get a message
            Message msg = mMtp.recv();

            // Default construct some messages
            MessageWaiting msgWait;
            MessageRequire msgRequire;

            // Attempt to cast to a wait message
            try {
                msgWait = (MessageWaiting) msg;
            } catch (Exception e) {
                System.out.println("Got wait message early");
                break;
            }

            // Attempt to get a require message
            try {
                msgRequire = (MessageRequire) msg;
            } catch (Exception e) {
                System.out.println("Ignoring message");
                continue;
            }

            // Validate require directive
            if ( !msgRequire.mCommand.equals(directive) ) { 
                System.out.println("Unexpected command required; exp = " + directive + "; act = " + msgRequire.mCommand + ";");
                break;
            }

            found = true;
        } 
        
        return found;
    }
}
