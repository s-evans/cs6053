
public abstract class CommandRequire extends Command {

    public CommandRequire(MessageTextParser mtp) {
        super(mtp);
    }

    public abstract String Require();

    public abstract boolean Execute() throws Exception; 
}
