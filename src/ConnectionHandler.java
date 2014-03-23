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

    private final String sExpectedComment = "Monitor Version 2.2.1";

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

    protected void GetBanner() throws Exception {
        // Receive the banner
        MessageComment monBanner = (MessageComment) mMtp.recv();

        // Validate the banner
        if ( !monBanner.mComment.equals(sExpectedComment) ) { 
            throw new Exception(
                    "Comment validation failed; exp = " + sExpectedComment + "; act = " + monBanner.mComment + ";");
        }
    }

    protected void PopulateMessageHandler() throws Exception {
        // Handle the REQUIRE QUIT message
        mMessageHandler.addRequireHandler(new CommandQuit(mMtp));

        // Handle the REQUIRE IDENT message
        mMessageHandler.addRequireHandler(new CommandLoginServer(mMtp, mIdent));

        // Handle the PARTICIPANT_PASSWORD_CHECKSUM message
        mMessageHandler.addCommandHandler(
                CommandPasswordCsum.Directive(), CommandPasswordCsum.class);

        // Add TRANSFER message handling 
        mMessageHandler.addCommandHandler(
                CommandTransferServer.Directive(), CommandTransferServer.class);
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

            GetBanner();

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
