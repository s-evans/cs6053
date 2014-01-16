public class CommandHandler {
    String currentCommand;

    public synchronized String CreateCommand() {
        try {
            wait();
        } catch (Exception e) {}

        return currentCommand;
    }

    public synchronized void SetCommand(String command) {
        currentCommand = command;
        notify();
    }
}
