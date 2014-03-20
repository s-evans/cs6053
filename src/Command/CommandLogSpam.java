
public class CommandLogSpam extends Command {

    public CommandLogSpam(MessageTextParser mtp, String[] args) {
        // Create super class
        super(mtp);

        // TODO: Do something with the arg(s)? 
    }

    static public String verb() {
        return "inject_log";
    }

    static public String usage() {
        return verb().concat(" <injected-string>");
    }

    public boolean Execute() throws Exception {
        // TODO: Implement this stuff
        return false;
    } 
}
