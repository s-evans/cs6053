import java.util.Iterator;
import java.util.Set;

public class MessageAuthorizeSet extends Message {
    public Set<String> mSet;

    public MessageAuthorizeSet() {

    }

    public MessageAuthorizeSet(String args) {
        // TODO: Implement
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