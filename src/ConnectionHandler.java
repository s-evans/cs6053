import java.net.*;
import java.io.*;

class ConnectionHandler extends MessageParser implements Runnable {

    private Socket incoming;
    private int counter;
    Thread runner;

    public ConnectionHandler(Socket i, int c, String name) throws IOException {
        super(name);
        incoming = i;
        counter = c;
    }

    // Run for each new incoming connection from the monitor (presumably)
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            out = new PrintWriter(incoming.getOutputStream(), true);

            CType = 1; // Indicates Server

            System.out.println("ConnectionHandler(" + counter + ") [Login]: Starting login from Server...");

            boolean success;
            try {
                // Attempt to Login, starting the DH exchange
                if (!Login(true)) {
                    System.out.println("ConnectionHandler [run]: Login failed");
                    System.exit(1);
                }
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }

            if (!success) {
                System.out.println("ConnectionHandler(" + counter + ") [run]: Login failed");
                System.exit(1);
            }

            System.out.println("ConnectionHandler(" + counter + ") [run]: Login succeeded");

            incoming.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (runner == null) {
            runner = new Thread(this);
            runner.start();
        }
    }
}
