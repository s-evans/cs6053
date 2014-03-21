import java.io.*;
import java.net.*;

public class Client implements Runnable {

    protected Thread mThread;
    protected Socket mMonConnSock = null;
    protected String mMonitorHostName;
    protected int mLocalPort;
    protected int mMonitorPort;
    protected String mIdent;
    protected PrintWriter mOut;
    protected BufferedReader mIn;
    protected MessageFactory mMsgFactory;
    protected MessageTextParser mMtp;
    protected IdentFile mIdentFile;
    protected MessageHandler mMessageHandler;
    protected String[] mArgs;

    public static final int DEFAULT_HOST_PORT = 22334; 

    // Entry point
    public static void main(String[] args) throws Exception {

        // Validate input
        if ( args.length < 4 ) {
            // Print out usage
            CliHandler cliHandler = new CliHandler();
            System.out.println("Usage: java Client <monitor-host-name> <monitor-port> <ident> [server-port]");
            System.out.println(cliHandler.getUsage());
            return;
        }

        // Create and start the client
        Client client = new Client(args);
        client.start(); 
    }

    // Constructor
    public Client(String monitorHostName, int monitorPort, int localPort, String ident) throws Exception {
        mIdent = ident;
        mMonitorHostName = monitorHostName;
        mMonitorPort = monitorPort;
        mLocalPort = localPort;
    }

    public Client(String[] args) {
        // Monitor junk
        mMonitorHostName = args[0];
        mMonitorPort = Integer.parseInt(args[1]);

        try {
            // Login junk
            mIdent = args[2]; 

            // Get host port if it exists
            mLocalPort = DEFAULT_HOST_PORT;
            mLocalPort = Integer.parseInt(args[3]);
        } catch ( Exception e ) {
            // Ignore
        }

        // Argument junk
        mArgs = args;
    }

    // Starts the thread running
    protected void start() {
        if (mThread == null) {
            mThread = new Thread(this);
            mThread.start();
        }
    }

    protected void CreateConnection() throws Exception {
        // Create socket to monitor
        System.out.println("Active Client: trying monitor: " + mMonitorHostName + " port: " + mMonitorPort + "...");
        mMonConnSock = new Socket(mMonitorHostName, mMonitorPort);
        System.out.println("completed.");
    }

    protected void CreateBufferedIO() throws Exception {
        // Create io buffer objects
        mOut = new PrintWriter(mMonConnSock.getOutputStream(), true);
        mIn = new BufferedReader(new InputStreamReader(mMonConnSock.getInputStream()));
    }

    protected void DoLogin() throws Exception { 
        System.out.println("Client [run]: Attempting log in");

        // Create the login command object 
        CommandLoginClient cmdLogin = new CommandLoginClient(
                mMtp, mIdent, mIdentFile.mCookie);

        // Execute the login command 
        if ( !cmdLogin.Execute() ) {
            throw new Exception("Failed to log in");
        }

        // at this point, we think we're legit
        System.out.println("Client [run]: Login succeeded");
    }

    protected void InitializeIdentFile() throws Exception {
        // Create ident file object
        mIdentFile = new IdentFile(mIdent);

        // Attempt to read data from the ident file
        if ( !mIdentFile.Read() ) {
            throw new Exception("Failed to read ident file");
        }
    }

    protected void CreateStreamParser() throws Exception {
        // Create message factory
        mMsgFactory = new MessageFactoryClient();

        // Create MessageTextParser object
        mMtp = new MessageTextParser(mIn, mOut, mMsgFactory);
    }

    protected void PopulateMessageHandler() throws Exception {
        // Add HOST_PORT directive handling
        CommandHostPort cmdHostPort = new CommandHostPort(
                mMtp, mMonConnSock.getLocalAddress().getHostName(), mLocalPort);
        mMessageHandler.addMessageHandler("HOST_PORT", cmdHostPort);
    }

    protected void PopulateCommandList() throws Exception {
        // Parse the CLI for commands
        CliHandler cliHandler = new CliHandler(mMtp);
        Command[] cmds = cliHandler.getCommands(mArgs);

        // Validate the count of commands
        if ( cmds.length == 0 ) {
            throw new Exception("No commands found!");
        }
        
        // Iterate over the command list
        for ( int i = 0 ; i < cmds.length ; i++ ) {
            mMessageHandler.addCommand(cmds[i]);
        }
    }

    protected void RunMessageHandler() throws Exception {
        // Create a message handler
        mMessageHandler = new MessageHandler(mMtp);
        
        // Add verbs to the message handler
        PopulateCommandList();

        // Add message handler routines
        PopulateMessageHandler();

        // Run the message handler
        mMessageHandler.run();
    }

    // Thread function
    public void run() {
        try {
            InitializeIdentFile();

            CreateConnection();

            CreateBufferedIO();

            CreateStreamParser();

            DoLogin();

            RunMessageHandler();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
