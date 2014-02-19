import java.util.Map;

public abstract class MessageFactory {

    protected Map<String, Class<? extends Message>> mMap;

    public MessageFactory() {

    }

    public Message createMsg(String directive, String args) {
        try {
            Class<? extends Message> m = mMap.get(directive);

            if ( m == null ) {
                return new MessageInvalid();
            }

            return m.getDeclaredConstructor(String.class).newInstance(args);
        } catch (Exception e) {
            // TODO: Handle this?
            e.printStackTrace();
            return new MessageInvalid();
        }
    }
}