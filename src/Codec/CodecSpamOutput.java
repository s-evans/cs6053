public class CodecSpamOutput extends Codec {

    protected String[] mArgs;
    protected MessageTextParser mMtp;

    // Constructor
    public CodecSpamOutput (MessageTextParser mtp, String[] args) {
        mArgs = args;
        mMtp = mtp;
    }

    // Decode a message
    public String decode(String in) throws Exception {
        // Pass thru all messages from the monitor
        return in;
    }
   
    // Encode a message
    public String encode(String in) throws Exception {
        // Obfuscate outgoing messages
        return ObfuscateSend.ObfuscateCommandOutput(in, mArgs, mMtp.isAuthenticated());
    }
}

