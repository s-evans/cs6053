import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class MessageSubsetK extends Message {
    public List<String> mSet;

    public MessageSubsetK() {

    }

    public MessageSubsetK(String[] set) throws Exception {
        // Initialize the list
        mSet = new ArrayList<String>();

        // Convert the array to a set
        Collections.addAll(mSet, set);
    }

    public MessageSubsetK(String args) throws Exception {
        // Clean up the input
        args = args.trim();
        
        // Initialize the list
        mSet = new ArrayList<String>();

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
