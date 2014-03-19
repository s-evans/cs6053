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

    public static final int DEFAULT_HOST_PORT = 22334; 

    // Entry point
    public static void main(String[] args) throws Exception {

        // TODO: Add a verb to the CLI to allow the client to do customizeable things for each run of the process 

        // Validate input
        if (args.length < 3 || args.length > 4) {
            System.out.println("Usage: java Client <monitor-host-name> <monitor-port> <ident> [host-port]");
            return;
        }

        // Get host port if it exists
        int hostPort = DEFAULT_HOST_PORT;
        if (args.length >= 4) {
            hostPort = Integer.parseInt(args[3]);
        }

        // Create and start the client
        Client client = new Client(
                new String(args[0]), Integer.parseInt(args[1]), hostPort, args[2]); 
        client.start(); 
    }

    // Constructor
    public Client(String monitorHostName, int monitorPort, int localPort, String ident) throws Exception {
        mIdent = ident;
        mMonitorHostName = monitorHostName;
        mMonitorPort = monitorPort;
        mLocalPort = localPort;
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
        // TODO: Add all verbs here

        // TODO: Remove the below (just test code)

        CommandTransferClient cmdXferClient = new 
            CommandTransferClient(mMtp, "brule", 1, "dangus");
        mMessageHandler.addCommand(cmdXferClient);
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
