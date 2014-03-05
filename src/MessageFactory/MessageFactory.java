import java.util.Map;

public abstract class MessageFactory {

    protected Map<String, Class<? extends Message>> mMap;

    public MessageFactory() {

    }

    public Message createMsg(String directive, String args) throws Exception {
        // Get the class that corresponds to the directive string
        Class<? extends Message> m = mMap.get(directive);

        // Create the message class given the string of arguments
        return m.getDeclaredConstructor(String.class).newInstance(args);
    }
}
