import java.io.*;
import java.net.*;

public class Server implements Runnable {

    protected ServerSocket s = null;
    protected int LOCAL_PORT;
    protected Thread runner;
    protected String IDENT;
    protected String PASSWORD;

    public static final int HOST_PORT = 22334; 
   
    // Entry point 
    public static void main(String[] args) throws Exception {

        // Validate argument count
        if (args.length != 2) {
            System.out.println("Usage: java Server <local-listening-port> <ident>");
            return;
        }
        
        // Start the server 
        Server server = new Server(Integer.parseInt(args[0]), args[1]);
        server.start(); 
    }

    // Constructor
    public Server(int lp, String name) {
        IDENT = name;
        LOCAL_PORT = lp;
       
        try {
            s = new ServerSocket(LOCAL_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Runs the thread
    protected void start() {
        if (runner == null) {
            runner = new Thread(this);
            runner.start();
        }
    }

    // Thread function
    public void run() {
        try {
            int i = 1;
            for (;;) {
                // Accept incoming connections
                Socket incoming = s.accept();

                // Pass off new connections to the connection handler
                new ConnectionHandler(incoming, i, IDENT).start();

                // Count connections
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

