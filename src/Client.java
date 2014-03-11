import java.io.*;
import java.net.*;

public class Client implements Runnable {

    Thread runner;
    Socket toMonitor = null;

    protected String monitorHostName;
    protected int localPort;
    protected int monitorPort;
    protected String ident;

    protected static final int HOST_PORT = 22334; 

    // Entry point
    public static void main(String[] args) throws Exception {

        // TODO: Add a verb to the CLI to allow the client to do customizeable things for each run of the process 
   
        // Validate input
        if (args.length < 3) {
            System.out.println("Usage: java Client <monitor-host-name> <monitor-port> <ident> <host-port>");
            return;
        }

        // Create and start the client
        Client client = new Client(
                new String(args[0]), Integer.parseInt(args[1]), HOST_PORT, args[2]); 
        client.start(); 
    }

    // Constructor
    public Client(String mname, int p, int lp, String name) throws Exception {
        ident = name;
        monitorHostName = mname;
        monitorPort = p;
        localPort = lp;
    }

    // Starts the thread running
    public void start() {
        if (runner == null) {
            runner = new Thread(this);
            runner.start();
        }
    }

    // Thread function
    public void run() {
        try {
            // Create socket to monitor
            System.out.print("Active Client: trying monitor: " + monitorHostName + " port: " + monitorPort + "...");
            toMonitor = new Socket(monitorHostName, monitorPort);
            System.out.println("completed.");

            // Create io buffer objects
            PrintWriter out = new PrintWriter(toMonitor.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(toMonitor.getInputStream()));

            // Create message factory
            MessageFactoryClient msgFactory = new MessageFactoryClient();

            // Create MessageTextParser object
            MessageTextParser mtp = new MessageTextParser(in, out, msgFactory);

            // TODO: Invoke login command using the ident

            // at this point, we think we're legit
            System.out.println("Client [run]: Login succeeded");

            // TODO: execute the verb

        } catch ( Exception e) {
            e.printStackTrace();
        }

    }
}
