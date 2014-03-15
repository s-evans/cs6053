
public class MessageFactoryClient extends MessageFactory {

    public MessageFactoryClient() {
        // Create the super class
		super(); 

        // Add a bunch of messages
        mMap.put(new MessageWaiting().directive(), MessageWaiting.class);
        mMap.put(new MessageRequire().directive(), MessageRequire.class);
        mMap.put(new MessageCommandError().directive(), MessageCommandError.class);
        mMap.put(new MessageComment().directive(), MessageComment.class);
        mMap.put(new MessageResult().directive(), MessageResult.class);
        mMap.put(new MessagePassCsum().directive(), MessagePassCsum.class);
        mMap.put(new MessageTransfer().directive(), MessageTransfer.class);

        // TODO: Add alive message handling
    }
}
