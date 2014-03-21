public class CodecSpam extends Codec {

    // Constructor
    public CodecSpam () {
        // Not much to do here!
    }

    // Decode a message
    public String decode(String in) throws Exception {
        // Pass thru all messages from the monitor
        return in;
    }
   
    // Encode a message
    public String encode(String in) throws Exception {
        // Obfuscate outgoing messages
        return ObfuscateSend.ObfuscateCommand(in, true);
    }
}

