import java.util.List;

public class MessageSubsetA extends Message {
    public List<String> mSet;

    public MessageSubsetA() {

    }

    public MessageSubsetA(String args) {
        // TODO: Implement
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