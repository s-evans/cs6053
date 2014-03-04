import java.io.*;
import java.net.*;

public class Client extends MessageParser implements Runnable {

    protected String monitorHostName;
    Thread runner;
    Socket toMonitor = null;
    protected int localPort;
    int DELAY = 90000; // Interval after which a new Active Client is started
    long prevTime;
    long present;
    protected int monitorPort;

    protected static final int HOST_PORT = 22334; // TODO: randomize this

    // Entry point
    public static void main(String[] args) throws Exception {
   
        // Validate input
        if (args.length != 3) {
            System.out.println("Usage: java Client <monitor-host-name> <monitor-port> <ident>");
            return;
        }

        // Create and start the client
        Client client = new Client(
                new String(args[0]), Integer.parseInt(args[1]), HOST_PORT, args[2]); 
        client.start(); 
    }

    // Constructor
    public Client(String mname, int p, int lp, String name) throws IOException {
        super(name);
        try {
            monitorHostName = mname;
            monitorPort = p;
            localPort = lp;
        } catch (NullPointerException n) {
            n.printStackTrace();
        }
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
        while (Thread.currentThread() == runner) {
            try {
                System.out.print("Active Client: trying monitor: " + monitorHostName + " port: " + monitorPort + "...");
                toMonitor = new Socket(monitorHostName, monitorPort);
                System.out.println("completed.");

                out = new PrintWriter(toMonitor.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(toMonitor.getInputStream()));

                hostName = toMonitor.getLocalAddress().getHostName();
                CType = 0; // Indicates Client

                hostPort = localPort;

                // Attempt to Login, starting the DH exchange
                if (!Login(false)) {
                    System.out.println("Client [run]: Login failed");
                    System.exit(1);
                }

                // at this point, we think we're legit
                System.out.println("Client [run]: Login succeeded");
                IsVerified = 1;

                System.out.println("***************************");
                
                toMonitor.close();
                out.close();
                in.close();

                try {
                    System.out.println("Client [run]: Sleeping for " + DELAY + "...");
                    Thread.sleep(DELAY);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("Client [run]: Looping again...");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
