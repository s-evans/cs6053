import java.io.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ListIterator;

public class MessageTextParser {
    protected BufferedReader mIn;
    protected PrintWriter mOut;
    protected MessageFactory mMsgFactory;
    protected List<Codec> mCodecList;
    protected boolean mAuthenticated;
    protected String mCsum;
    protected String mIdent;

    // Constructor
    public MessageTextParser(
            BufferedReader input, 
            PrintWriter output, 
            MessageFactory messageFactory) {
        mCodecList = new LinkedList<Codec>();
        mIn = input;
        mOut = output;
        mMsgFactory = messageFactory;
        mAuthenticated = false;
        mCsum = null;
        mIdent = null;
    }

    public void setCsum (String csum) {
        mCsum = csum;
    }

    public String getCsum () {
        return mCsum;
    }

    public boolean isAuthenticated() {
        return mAuthenticated;
    }

    public void isAuthenticated(boolean authenticated) {
        mAuthenticated = authenticated;
    }

    public void setIdent(String ident) {
        mIdent = ident;
    }

    public String getIdent() {
        return mIdent;
    }

    // Add a codec to send and receive operations symetrically
    public void addCodec ( Codec c ) {
        mCodecList.add(c);
    }

    // Add a codec to send and receive operations symetrically (front of the list)
    public void addCodecFront ( Codec c ) {
        mCodecList.add(0, c);
    }

    // Get a message string from the stream
    public Message recv() throws Exception {
        Message msg;
        String line;

        // Read the next line. 
        line = mIn.readLine();

        // Pass the message to the decode stack in reverse order
        ListIterator<Codec> iter = mCodecList.listIterator(mCodecList.size());
        while ( iter.hasPrevious() ) {
            System.out.println("MessageTextParser [recv]: Encoded:\n\t" + line);
            line = iter.previous().decode(line);
            System.out.println("MessageTextParser [recv]: Decoded:\n\t" + line);
        }

        System.out.println("MessageTextParser [recv]: Returning " + line);

        // Create the message object 
        msg = mMsgFactory.createMsg(line);

        return msg;
    }

    // Send a message using the encode stack
    public void send(Message msg) throws Exception {
        send(msg.serialize());
    }

    // Send string data using the encode stack
    public void send(String msg) throws Exception {

        // Pass the message to the encode stack
        Iterator<Codec> iter = mCodecList.iterator();
        while ( iter.hasNext() ) {
            System.out.println("MessageTextParser [send]: Unencoded:\n\t" + msg);
            msg = iter.next().encode(msg);
            System.out.println("MessageTextParser [send]: Encoded:\n\t" + msg);
        }

        // Output the message
        mOut.println(msg);
        if (mOut.checkError() == true) {
            throw (new IOException());
        }

        // Courtesy flush
        mOut.flush();
        if (mOut.checkError() == true) {
            throw (new IOException());
        }
    }
 
}

