import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections.*;

public class MessageAuthorizeSet extends Message {
    public List<String> mSet;

    public MessageAuthorizeSet() {
    }

    public MessageAuthorizeSet(String args) throws Exception {
        // Clean up input 
        args = args.trim();

        // Initialize the collection
        mSet = new ArrayList<String>(); 

        // Parse the input string 
        String[] vals = args.split(" ");
        if ( vals.length < 1 ) {
            throw new Exception(new String("PARSE ERROR"));
        }

        // Copy the data
        Collections.addAll(mSet, vals);
    }

    public MessageAuthorizeSet(String[] set) {
        // Initialize the collection
        mSet = new ArrayList<String>(); 

        // Copy the data
        Collections.addAll(mSet, set);
    }

    public String directive() {
        return "AUTHORIZE_SET";
    }

    public String serialize() {
        String base = directive();

        for ( String str : mSet ) {
            base = base.concat(sArgDelimit).concat(str);
        }

        return base;
    }
}
