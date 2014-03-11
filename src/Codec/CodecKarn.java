import java.math.BigInteger;

public class CodecKarn extends Codec {

    protected Karn mKarn; 

    // Constructor
    public CodecKarn (BigInteger bi) {
        mKarn = new Karn(bi);
    }

    // Useful methods
        
    // TODO: Validate the usage pattern for Karn

    // Decode a message
    public String decode(String in) throws Exception {
        return mKarn.decrypt(in);
    }
   
    // Encode a message
    public String encode(String in) throws Exception {
        return mKarn.encrypt(in);
    }
}

