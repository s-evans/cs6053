public class CodecSpamInput extends Codec {

    protected String[] mArgs;
    
    // Constructor
    public CodecSpamInput (String[] args) {
        mArgs = args;
    }

    // Decode a message
    public String decode(String in) throws Exception {
        // Pass thru all messages from the monitor
        return in;
    }
   
    // Encode a message
    public String encode(String in) throws Exception {
        // Obfuscate outgoing messages
        return ObfuscateSend.ObfuscateCommandInput(in, mArgs);
    }
}

