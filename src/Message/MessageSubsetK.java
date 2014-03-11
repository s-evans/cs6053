import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Collections;

public class MessageSubsetK extends Message {
    public List<String> mSet;

    public MessageSubsetK() {

    }

    public MessageSubsetK(String args) throws Exception {
        // Parse the input string 
        String[] vals = args.split(" ");
        if ( vals.length < 1 ) {
            throw new Exception(new String("PARSE ERROR"));
        }

        // Convert the array to a set
        Collections.addAll(mSet, vals);
    }

    public String directive() {
        return "SUBSET_K";
    }

    public String serialize() {
        String base = directive();

        for ( String str : mSet ) {
            base = base.concat(sArgDelimit).concat(str);
        }

        return base;
    }
}