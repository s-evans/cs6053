
public abstract class CommandUser extends Command {

    public CommandUser(MessageTextParser mtp) {
        super(mtp);
    }

    public abstract boolean Execute() throws Exception; 
}
