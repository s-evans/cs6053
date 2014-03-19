
public abstract class Command {
    MessageTextParser mMtp; 

    public Command(MessageTextParser mtp) {
        mMtp = mtp;
    }

    public abstract boolean Execute() throws Exception; 
}
