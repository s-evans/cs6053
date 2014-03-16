import java.net.*;
import java.io.*;

class ConnectionHandler implements Runnable {

    protected Socket mIncomingConnSock;
    protected int mConnNumber;
    protected String mIdent;
    protected Thread mThread;
    protected BufferedReader mIn;
    protected PrintWriter mOut;
    protected MessageFactory mMsgFactory;
    protected IdentFile mIdentFile;
    protected MessageHandler mMessageHandler;
    protected MessageTextParser mMtp;

    public ConnectionHandler(
            Socket connSock, int connNum, String ident) {
        mIncomingConnSock = connSock;
        mConnNumber = connNum;
        mIdent = ident;
    }

    protected void CreateBufferedIO() throws Exception {
        // Create io buffer objects from the connection stream
        mIn = new BufferedReader(new InputStreamReader(mIncomingConnSock.getInputStream()));
        mOut = new PrintWriter(mIncomingConnSock.getOutputStream(), true);
    }

    protected void CreateStreamParser() throws Exception {
        // Create message factory
        mMsgFactory = new MessageFactoryServer();

        // Create MessageTextParser object
        mMtp = new MessageTextParser(mIn, mOut, mMsgFactory);
    }

    protected void InitializeIdentFile() throws Exception {
        // Create ident file object
        mIdentFile = new IdentFile(mIdent);

        // Attempt to read data from the ident file
        if ( !mIdentFile.Read() ) {
            throw new Exception("Failed to read ident file");
        }
    }

    protected void DoLogin() throws Exception {
        System.out.println("ConnectionHandler(" + mConnNumber + ") [Login]: Starting login from Server...");

        // Create a login command object
        CommandLoginServer cmdLogin = new CommandLoginServer(
                mMtp, mIdent, mIdentFile.mCookie, mIdentFile.mPassword);

        // Execute the login command 
        if ( !cmdLogin.Execute() ) {
            throw new Exception("Failed to log in");
        }

        System.out.println("ConnectionHandler(" + mConnNumber + ") [run]: Login succeeded");
    }

    protected void PopulateMessageHandler() throws Exception {
        // Handle the QUIT message
        CommandQuit cmdQuit = new CommandQuit(mMtp);
        mMessageHandler.addMessageHandler("QUIT", cmdQuit);

        // TODO: Add transfer message handling 

        
        // TODO: Remove the below

        // Receive a transfer message from the monitor
        MessageTransfer msgXferReq = (MessageTransfer) mMtp.recv();

        // Create a transfer command object
        CommandTransferServer cmdXferServer =
            new CommandTransferServer(mMtp, 
                    msgXferReq.mRecipientIdent,
                    msgXferReq.mPointsRequested,
                    msgXferReq.mSenderIdent);

        // Execute the command object
        if ( !cmdXferServer.Execute() ) { 
            System.out.println("ConnectionHandler(" + mConnNumber + ") [run]: Transfer failed!");
        }
    }

    protected void RunMessageHandler() throws Exception {
        // Create a message handler 
        mMessageHandler = new MessageHandler(mMtp);

        // Add message handler routines
        PopulateMessageHandler();

        // Run the message handler
        mMessageHandler.run();
    }

    protected void Fin() throws Exception {
        System.out.println("ConnectionHandler(" + mConnNumber + ") [run]: All done. Exiting.");
        mIncomingConnSock.close();
    }

    // Run for each new mIncomingConnSock connection from the monitor (presumably)
    public void run() {
        try {
            InitializeIdentFile();

            CreateBufferedIO();

            CreateStreamParser();

            DoLogin();

            RunMessageHandler();

            Fin();
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
