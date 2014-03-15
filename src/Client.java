import java.io.*;
import java.net.*;

public class Client implements Runnable {

    Thread mThread;
    Socket mMonConnSock = null;

    protected String mMonitorHostName;
    protected int mLocalPort;
    protected int mMonitorPort;
    protected String mIdent;

    public static final int DEFAULT_HOST_PORT = 22334; 

    // Entry point
    public static void main(String[] args) throws Exception {

        // TODO: Add a verb to the CLI to allow the client to do customizeable things for each run of the process 
   
        // Validate input
        if (args.length < 3 || args.length > 4) {
            System.out.println("Usage: java Client <monitor-host-name> <monitor-port> <mIdent> [host-port]");
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
    public void start() {
        if (mThread == null) {
            mThread = new Thread(this);
            mThread.start();
        }
    }

    // Thread function
    public void run() {
        try {
            // Create socket to monitor
            System.out.println("Active Client: trying monitor: " + mMonitorHostName + " port: " + mMonitorPort + "...");
            mMonConnSock = new Socket(mMonitorHostName, mMonitorPort);
            System.out.println("completed.");

            // Create io buffer objects
            PrintWriter out = new PrintWriter(mMonConnSock.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(mMonConnSock.getInputStream()));

            // Create message factory
            MessageFactoryClient msgFactory = new MessageFactoryClient();

            // Create MessageTextParser object
            MessageTextParser mtp = new MessageTextParser(in, out, msgFactory);

            // Create ident file object
            IdentFile identFile = new IdentFile(mIdent);

            // Attempt to read data from the ident file
            if ( !identFile.Read() ) {
                throw new Exception("Failed to read ident file");
            }

            // Create the login command object 
            CommandLoginClient cmdLogin = new CommandLoginClient(
                    mtp, mIdent, identFile.mCookie,
                    mMonConnSock.getLocalAddress().getHostName(), mLocalPort);
            
            // Execute the login command 
            if ( !cmdLogin.Execute() ) {
                throw new Exception("Failed to log in");
            }

            // at this point, we think we're legit
            System.out.println("Client [run]: Login succeeded");

            // TODO: execute the verb

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
