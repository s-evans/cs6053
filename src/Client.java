import java.io.*;
import java.net.*;

public class Client implements Runnable {

    protected Thread mThread;
    protected Socket mMonConnSock = null;
    protected String mMonitorHostName;
    protected int mMonitorPort;
    protected PrintWriter mOut;
    protected BufferedReader mIn;
    protected MessageFactory mMsgFactory;
    protected MessageTextParser mMtp;
    protected MessageHandler mMessageHandler;
    protected String[] mArgs;

    private final String sExpectedComment = "Monitor Version 2.2.1";
    public static final int DEFAULT_HOST_PORT = 22334; 

    // Entry point
    public static void main(String[] args) throws Exception {

        // Validate input
        if ( args.length < 2 ) {
            // Print out usage
            CliHandler cliHandler = new CliHandler();
            System.out.println("Usage: java Client <monitor-host-name> <monitor-port>\n");
            System.out.println(cliHandler.getUsage());
            return;
        }

        // Create and start the client
        Client client = new Client(args);
        client.start(); 
    }

    // Constructor
    public Client(String monitorHostName, int monitorPort) throws Exception {
        mMonitorHostName = monitorHostName;
        mMonitorPort = monitorPort;
    }

    public Client(String[] args) {
        // Monitor junk
        mMonitorHostName = args[0];
        mMonitorPort = Integer.parseInt(args[1]);

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

    protected void CreateStreamParser() throws Exception {
        // Create message factory
        mMsgFactory = new MessageFactoryClient();

        // Create MessageTextParser object
        mMtp = new MessageTextParser(mIn, mOut, mMsgFactory);
    }

    protected void PopulateRequireCommandList() throws Exception {
        // Parse the CLI for commands
        CliHandler cliHandler = new CliHandler(mMtp);
        CommandRequire[] cmds = cliHandler.getRequireCommands(mArgs);

        // Iterate over the command list
        for ( int i = 0 ; i < cmds.length ; i++ ) {
            // Add message handler
            mMessageHandler.addRequireHandler(cmds[i]);
        }
    }

    protected void PopulateUserCommandList() throws Exception {
        // Parse the CLI for commands
        CliHandler cliHandler = new CliHandler(mMtp);
        CommandUser[] cmds = cliHandler.getUserCommands(mArgs);

        // Iterate over the command list
        for ( int i = 0 ; i < cmds.length ; i++ ) {
            mMessageHandler.addUserCommand(cmds[i]);
        }
    }

    protected void GetBanner() throws Exception {
        // Receive the banner
        MessageComment monBanner = (MessageComment) mMtp.recv();

        // Validate the banner
        if ( !monBanner.mComment.equals(sExpectedComment) ) { 
            throw new Exception(
                    "Comment validation failed; exp = " + sExpectedComment + "; act = " + monBanner.mComment + ";");
        }
    }

    protected void RunMessageHandler() throws Exception {
        // Create a message handler
        mMessageHandler = new MessageHandler(mMtp);
        
        // Add verbs to the message handler
        PopulateUserCommandList();

        // Add message handler routines
        PopulateRequireCommandList();

        // Run the message handler
        mMessageHandler.run();
    }

    // Thread function
    public void run() {
        try {
            CreateConnection();

            CreateBufferedIO();

            CreateStreamParser();

            GetBanner();

            RunMessageHandler();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
