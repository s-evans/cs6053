import java.net.*;
import java.io.*;

class ConnectionHandler implements Runnable {

    private Socket mIncomingConnSock;
    private int mConnNumber;
    private String mIdent;
    Thread mThread;

    public ConnectionHandler(
            Socket connSock, int connNum, String ident) {
        mIncomingConnSock = connSock;
        mConnNumber = connNum;
        mIdent = ident;
    }

    // Run for each new mIncomingConnSock connection from the monitor (presumably)
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(mIncomingConnSock.getInputStream()));
            PrintWriter out = new PrintWriter(mIncomingConnSock.getOutputStream(), true);

            System.out.println("ConnectionHandler(" + mConnNumber + ") [Login]: Starting login from Server...");

            boolean success;
            try {
                // TODO: Attempt to Login, starting the DH exchange
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }

            if (!success) {
                System.out.println("ConnectionHandler(" + mConnNumber + ") [run]: Login failed");
                System.exit(1);
            }

            System.out.println("ConnectionHandler(" + mConnNumber + ") [run]: Login succeeded");

            mIncomingConnSock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (mThread == null) {
            mThread = new Thread(this);
            mThread.start();
        }
    }
}
