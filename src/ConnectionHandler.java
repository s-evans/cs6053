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
            // Create io buffer objects from the connection stream
            BufferedReader in = new BufferedReader(new InputStreamReader(mIncomingConnSock.getInputStream()));
            PrintWriter out = new PrintWriter(mIncomingConnSock.getOutputStream(), true);

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

            System.out.println("ConnectionHandler(" + mConnNumber + ") [Login]: Starting login from Server...");

            // Create a login command object
            CommandLoginServer cmdLogin = new CommandLoginServer(
                    mtp, mIdent, identFile.mCookie, identFile.mPassword);

            // Execute the login command 
            if ( !cmdLogin.Execute() ) {
                throw new Exception("Failed to log in");
            }

            System.out.println("ConnectionHandler(" + mConnNumber + ") [run]: Login succeeded");

            // TODO: Handle a transfer directive/command ? 
            // TODO: Anything else we want to do ?

            System.out.println("ConnectionHandler(" + mConnNumber + ") [run]: All done. Exiting.");

            // Close the connection and be done
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
