import java.net.*;
import java.io.*;

class ConnectionHandler implements Runnable {

    private Socket incoming;
    private int counter;
    private String ident;
    Thread runner;

    public ConnectionHandler(Socket i, int c, String id) {
        incoming = i;
        counter = c;
        ident = id;
    }

    // Run for each new incoming connection from the monitor (presumably)
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);

            System.out.println("ConnectionHandler(" + counter + ") [Login]: Starting login from Server...");

            boolean success;
            try {
                // TODO: Attempt to Login, starting the DH exchange
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
