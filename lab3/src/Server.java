import java.io.*;
import java.net.*;

public class Server implements Runnable {

    ServerSocket s = null;
    public static int MONITOR_PORT;
    public static int LOCAL_PORT;
    Thread runner;
    String IDENT;
    String PASSWORD;

    public Server(int p, int lp, String name, String password) {
        IDENT = name;
        PASSWORD = password;
        try {
            s = new ServerSocket(p);
            MONITOR_PORT = p;
            LOCAL_PORT = lp;
        } catch (IOException e) {
            System.out.println("Server [Server]: IOException:\n\t" + e + this);
        }
    }

    public void start() {
        if (runner == null) {
            runner = new Thread(this);
            runner.start();
        }
    }

    public void run() {
        try {
            int i = 1;
            for (;;) {
                Socket incoming = s.accept();
                new ConnectionHandler(incoming, i, IDENT, PASSWORD).start();
                // Spawn a new thread for each new connection
                i++;
            }
        } catch (Exception e) {
            System.out.println("Server [run]: Exception:\n\t" + e + this);
        }
    }
}

class ConnectionHandler extends MessageParser implements Runnable {
    private Socket incoming;
    private int counter;
    Thread runner;

    public ConnectionHandler(Socket i, int c, String name, String password) throws IOException {
        super(name, password);
        incoming = i;
        counter = c;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            out = new PrintWriter(incoming.getOutputStream(), true);

            hostPort = Server.LOCAL_PORT;
            CType = 1; // Indicates Server
            
            System.out.println("ConnectionHandler(" + counter +") [Login]: Starting login from Server...");
            
            boolean success;
            try {
                //Attempt to Login, starting the DH exchange
                if (!Login()) {
                    System.out.println("ActiveClient [run]: Login failed");
                    System.exit(1);
                }

                String monMsg = GetMonitorMessage();
                String nextCmd = GetNextCommand(monMsg,"");


                if (!nextCmd.trim().equals("QUIT")) {
                    throw new Exception("ConnectionHandler(" + counter +") [Login]: Monitor may not be legit.  Asking for "
                            + nextCmd + " instead of ALIVE");
                }
                
                success = Execute("QUIT");
            } catch (Exception e) {
                System.out.println("ConnectionHandler(" + counter +") [Login]: Exception:\n\t" + e + this);
                success = false;
            }

            if (!success) {
                System.out.println("ConnectionHandler(" + counter +") [run]: Login failed");
                System.exit(1);
            }
            
            System.out.println("ConnectionHandler(" + counter +") [run]: Login succeeded");
            
            incoming.close();
        } catch (IOException e) {
            System.out.println("ConnectionHandler [run]: IOException:\n\t" + e + this);
        } catch (NullPointerException n) {
            System.out.println("ConnectionHandler [run]: NullPointerException:\n\t" + n + this);
        }
    }

    public void start() {
        if (runner == null) {
            runner = new Thread(this);
            runner.start();
        }
    }
}
