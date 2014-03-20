import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class CliHandler {
    protected MessageTextParser mMtp;
    protected Map<String, Class<? extends Command>> mVerbMap;

    protected static final String sVerbPrefix = "--";
    
    public CliHandler(MessageTextParser mtp) {
        mMtp = mtp;
        populateMap();
    }

    public CliHandler() {
        mMtp = null;
        populateMap();
    }

    protected void populateMap() {
        // Populate the map
        mVerbMap = new HashMap<String, Class<? extends Command>>();
        mVerbMap.put(CommandTransferClient.verb(), CommandTransferClient.class);
        mVerbMap.put(CommandLogSpam.verb(), CommandLogSpam.class); 
    }

    public String getUsage() throws Exception {
        String usage = "";
        
        // Iterate over the list of command classes
        for ( Class<? extends Command> cmdClass : mVerbMap.values() ) {
            // Get their usage string output
            Method method = cmdClass.getMethod("usage");
            Object obj = method.invoke(null);

            // Append to a single string
            usage += "\t--" + obj + "\n";
        }

        return usage;
    }

    protected void invalidCommand(String arg) {
        System.out.println("Invalid command: " + arg);
    }

    public Command[] getCommands(String[] args) throws Exception {
        List<Command> cmdList = new ArrayList<Command>();

        for ( int i = 0 ; i < args.length ; i++ ) {
            // Ignore non-verbs
            if ( !args[i].startsWith(sVerbPrefix) ) {
                continue;
            }

            // Create verb string without prepend
            String verb = args[i].substring(sVerbPrefix.length());

            // Look up verb in the map and get the command class
            Class<? extends Command> cmdClass = mVerbMap.get(verb);

            // Validate verb is in the map
            if ( cmdClass == null ) {
                invalidCommand(verb);
                continue;
            }

            // Parse to end of args or to next verb in order to get arg list
            int j = i + 1;
            for ( ; j < args.length && !args[j].startsWith(sVerbPrefix) ; j++ ) {
                // Just counting
            }

            // Populate the argument array
            String[] cmdArgs = null;
            int argCount = j - i - 1;
            if ( argCount > 0 ) {
                // Create a new string array for our args
                cmdArgs = new String[argCount];

                // Populate the string array
                for ( int g = 0 ; g < argCount ; g++ ) {
                    cmdArgs[g] = args[i + g + 1];
                }
            }

            // Create a new command object
            Command cmd = cmdClass.
                getDeclaredConstructor(MessageTextParser.class, String[].class).
                newInstance(mMtp, cmdArgs); 

            // Add the command object to a list
            cmdList.add(cmd);
        } 

        // Return the command list
        return cmdList.toArray(new Command[cmdList.size()]);
    }
}
