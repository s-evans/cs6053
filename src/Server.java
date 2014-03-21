import java.net.*;

public class Server implements Runnable {

    protected ServerSocket mListeningSock = null;
    protected Thread mThread;
    protected int mListeningPort;
    protected String mIdent;

    public static final int HOST_PORT = 22334; 
   
    // Entry point 
    public static void main(String[] args) throws Exception {

        // Validate argument count
        if (args.length != 2) {
            System.out.println("Usage: java Server <local-listening-port> <ident>");
            return;
        }
    
        // Create the server object
        Server server = new Server(Integer.parseInt(args[0]), args[1]);

        // Start the server 
        server.start(); 
    }

    // Constructor
    public Server(int listeningPortNumber, String ident) throws Exception {
        mIdent = ident;
        mListeningPort = listeningPortNumber;
        mListeningSock = new ServerSocket(mListeningPort);
    }

    // Runs the thread
    protected void start() {
        if (mThread == null) {
            mThread = new Thread(this);
            mThread.start();
        }
    }

    // Thread function
    public void run() {
        int i = 1;
        for (;;) {
            try {
                // Accept incoming connections
                Socket incoming = mListeningSock.accept();

                // Pass off new connections to the connection handler
                ConnectionHandler handler = new ConnectionHandler(incoming, i, mIdent);

                // Count connections
                i++;

                // Start the handler running
                handler.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

