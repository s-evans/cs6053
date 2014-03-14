import java.math.BigInteger;

public class CodecKarn extends Codec {

    protected Karn mKarn; 

    // Constructor
    public CodecKarn (BigInteger bi) {
        mKarn = new Karn(bi);
    }

    // Useful methods
        
    // NOTE: may want to check for Karn::IsEncrypted first and pass back input string if not encrypted

    // Decode a message
    public String decode(String in) throws Exception {
        // Validate message is encrypted
        if ( !mKarn.IsEncrypted(in) ) {
            throw new Exception("Message not karn encrypted");
        }

        // Do decryption
        return mKarn.decrypt(in);
    }
   
    // Encode a message
    public String encode(String in) throws Exception {
        return mKarn.encrypt(in);
    }
}

