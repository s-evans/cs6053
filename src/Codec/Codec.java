
public abstract class Codec {
    // Constructor
    public Codec () {

    }

    // Useful methods

    // Decode a message
    public abstract String decode(String in) throws Exception; 
   
    // Encode a message
    public abstract String encode(String in) throws Exception;
}

