import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class MessageSubsetA extends Message {
    public List<String> mSet;

    public MessageSubsetA() {
    }

    public MessageSubsetA(String[] set) throws Exception {
        // Initialize the list
        mSet = new ArrayList<String>();

        // Convert the array to a set
        Collections.addAll(mSet, set);
    }

    public MessageSubsetA(String args) throws Exception {
        // Clean up input 
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
