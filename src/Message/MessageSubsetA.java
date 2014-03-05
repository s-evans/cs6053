import java.util.List;
import java.util.Collections;

public class MessageSubsetA extends Message {
    public List<String> mSet;

    public MessageSubsetA() {

    }

    public MessageSubsetA(String args) throws Exception {
        // Parse the input string 
        String[] vals = args.split(" ");
        if ( vals.length < 1 ) {
            throw new Exception(new String("PARSE ERROR"));
        }

        // Convert the array to a set
        Collections.addAll(mSet, vals);
    }

    public String directive() {
        return "SUBSET_A";
    }

    public String serialize() {
        String base = directive();

        for ( String str : mSet ) {
            base = base.concat(sArgDelimit).concat(str);
        }

        return base;
    }
}
