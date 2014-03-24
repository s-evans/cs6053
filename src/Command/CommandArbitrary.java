
public class CommandArbitrary extends CommandUser {

    protected String[] mArgs;

    public CommandArbitrary (MessageTextParser mtp, String[] args) {
        // Create super class
        super(mtp);

        // Populate junk
        mArgs = args;
    }

    static public String verb() {
        return "arbitrary";
    }

    static public String usage() {
        return verb().concat(" <command-string> ...");
    }

    public static String explain() {
        return "Issues arbitrary data over the connection once the monitor issues an open ended WAITING command";
    }

    private static String join (String[] array, String delimit) {
        if (array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        int i;
        for ( i = 0 ; i < array.length - 1 ; i++ ) {
            sb.append(array[i]+delimit);
        }
        return sb.toString() + array[i];
    }

    public boolean Execute() throws Exception {
        mMtp.send(join(mArgs, " "));
        return true;
    }
}

