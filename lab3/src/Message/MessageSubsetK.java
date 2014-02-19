import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MessageSubsetK extends Message {
    public List<String> mSet;

    public MessageSubsetK() {

    }

    public MessageSubsetK(String args) {
        // TODO: Implmeent
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