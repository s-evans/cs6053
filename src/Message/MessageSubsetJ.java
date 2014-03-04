import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MessageSubsetJ extends Message {
    public List<String> mSet;

    public MessageSubsetJ() {

    }

    public MessageSubsetJ(String args) {
        // TODO: Implement
    }

    public String directive() {
        return "SUBSET_J";
    }

    public String serialize() {
        String base = directive();

        for ( String str : mSet ) {
            base = base.concat(sArgDelimit).concat(str);
        }

        return base;
    }
}