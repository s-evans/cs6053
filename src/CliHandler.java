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
        mVerbMap.put(CommandLoginClient.verb(), CommandLoginClient.class);
        mVerbMap.put(CommandHostPort.verb(), CommandHostPort.class);
        mVerbMap.put(CommandTransferClient.verb(), CommandTransferClient.class);
        mVerbMap.put(CommandLogSpamOutput.verb(), CommandLogSpamOutput.class); 
        mVerbMap.put(CommandLogSpamInput.verb(), CommandLogSpamInput.class); 
        mVerbMap.put(CommandArbitrary.verb(), CommandArbitrary.class); 
        mVerbMap.put(CommandChangePassword.verb(), CommandChangePassword.class);
    }

    public String getUsage() throws Exception {
        String usage = "";
        
        // Iterate over the list of command classes
        for ( Class<? extends Command> cmdClass : mVerbMap.values() ) {
            // Get their usage string output
            Method method = cmdClass.getMethod("usage");
            Object obj = method.invoke(null);

            // Append to a single string
            usage += "--" + obj + "\n";

            // Get explanation
            method = cmdClass.getMethod("explain");
            obj = method.invoke(null);

            // Append to a single string
            usage += obj + "\n\n";
        }

        return usage;
    }

    protected void invalidCommand(String arg) {
        System.out.println("Invalid command: " + arg);
    }

    public CommandRequire[] getRequireCommands(String[] args) throws Exception {
        List<CommandRequire> cmdList = new ArrayList<CommandRequire>();

        for ( int i = 0 ; i < args.length ; i++ ) {
            // Ignore non-verbs
            if ( !args[i].startsWith(sVerbPrefix) ) {
                continue;
            }

            // Create verb string without prepend
            String verb = args[i].substring(sVerbPrefix.length());

            // Look up verb in the map and get the command class
            Class<? extends CommandRequire> cmdClass = null;

            // Attempt to cast command to a require command
            try {
                cmdClass = (Class<? extends CommandRequire>) mVerbMap.get(verb);
            } catch ( Exception e ) {
                continue;
            }

            // Validate verb is in the map
            if ( cmdClass == null ) {
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

            try {
                // Create a new command object
                CommandRequire cmd = cmdClass.
                    getDeclaredConstructor(MessageTextParser.class, String[].class).
                    newInstance(mMtp, cmdArgs); 

                // Add the command object to a list
                cmdList.add(cmd);
            } catch ( Exception e ) {
                continue;
            }
        } 

        // Return the command list
        return cmdList.toArray(new CommandRequire[cmdList.size()]);
    }
 
    public CommandUser[] getUserCommands(String[] args) throws Exception {
        List<CommandUser> cmdList = new ArrayList<CommandUser>();

        for ( int i = 0 ; i < args.length ; i++ ) {
            // Ignore non-verbs
            if ( !args[i].startsWith(sVerbPrefix) ) {
                continue;
            }

            // Create verb string without prepend
            String verb = args[i].substring(sVerbPrefix.length());

            // Look up verb in the map and get the command class
            Class<? extends CommandUser> cmdClass = null;

            // Attempt to cast to a user command type
            try {
                cmdClass = (Class<? extends CommandUser>) mVerbMap.get(verb);
            } catch ( Exception e ) {
                continue;
            }

            // Validate verb is in the map
            if ( cmdClass == null ) {
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

            try { 
                // Create a new command object
                CommandUser cmd = cmdClass.
                    getDeclaredConstructor(MessageTextParser.class, String[].class).
                    newInstance(mMtp, cmdArgs); 

                // Add the command object to a list
                cmdList.add(cmd);
            } catch ( Exception e ) {
                continue;
            }
        } 

        // Return the command list
        return cmdList.toArray(new CommandUser[cmdList.size()]);
    }
}
