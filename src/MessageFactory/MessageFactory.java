import java.util.HashMap;

// This class is used to create message objects for incoming messages
public abstract class MessageFactory {

    protected HashMap<String, Class<? extends Message>> mMap;

    public MessageFactory() {
		mMap = new HashMap<String, Class<? extends Message>>();
    }

    public Message createMsg(String input) throws Exception {
        // Parse the string 
        String[] strs = input.split(" |:");
        if ( strs.length < 1 ) {
            throw new Exception("Failed to parse message");
        }

        // Get directive 
        String directive = strs[0]; 

        // Parse the string again
        strs = input.split(" "); 
        if ( strs.length < 1 ) {
            throw new Exception("Failed to parse message");
        }

        // Get the remaining trailing characters
        String args = input.substring(strs[0].length()).trim(); 

        // Get the class that corresponds to the directive string
        Class<? extends Message> m = mMap.get(directive);

        // Create the message class given the string of arguments
        return m.getDeclaredConstructor(String.class).newInstance(args);
    }
}
