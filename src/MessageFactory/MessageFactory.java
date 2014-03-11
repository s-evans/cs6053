import java.util.Map;

// This class is used to create message objects for incoming messages
public abstract class MessageFactory {

    protected Map<String, Class<? extends Message>> mMap;

    public MessageFactory() {

    }

    public Message createMsg(String input) throws Exception {
        // Parse the string 
        String[] strs = input.split("( |:)"); // TODO: Validate this regex string
        if ( strs.length < 1 ) {
            throw new Exception("Failed to parse message");
        }

        // Get directive and args from line
        String directive = strs[0]; 
        String args = input.substring(strs[0].length()); 

        // Get the class that corresponds to the directive string
        Class<? extends Message> m = mMap.get(directive);

        // Create the message class given the string of arguments
        return m.getDeclaredConstructor(String.class).newInstance(args);
    }
}
