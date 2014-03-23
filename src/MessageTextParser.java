import java.io.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ListIterator;

public class MessageTextParser {
    protected BufferedReader in;
    protected PrintWriter out;
    protected MessageFactory msgFactory;
    protected List<Codec> mCodecList;
    protected boolean mAuthenticated;
    protected String mCsum;

    // Constructor
    public MessageTextParser(
            BufferedReader input, 
            PrintWriter output, 
            MessageFactory messageFactory) {
        mCodecList = new LinkedList<Codec>();
        in = input;
        out = output;
        msgFactory = messageFactory;
        mAuthenticated = false;
        mCsum = null;
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
        line = in.readLine();

        // Pass the message to the decode stack in reverse order
        ListIterator<Codec> iter = mCodecList.listIterator(mCodecList.size());
        while ( iter.hasPrevious() ) {
            System.out.println("MessageTextParser [recv]: Encoded:\n\t" + line);
            line = iter.previous().decode(line);
            System.out.println("MessageTextParser [recv]: Decoded:\n\t" + line);
        }

        System.out.println("MessageTextParser [recv]: Returning " + line);

        // Create the message object 
        msg = msgFactory.createMsg(line);

        return msg;
    }

    public void send(Message msg) throws Exception {

        System.out.println("MessageTextParser [send]: plaintext message:\n\t" + msg.serialize());

        // Pass the message to the encode stack
        String outMsg = msg.serialize();
        Iterator<Codec> iter = mCodecList.iterator();
        while ( iter.hasNext() ) {
            System.out.println("MessageTextParser [send]: Unencoded:\n\t" + outMsg);
            outMsg = iter.next().encode(outMsg);
            System.out.println("MessageTextParser [send]: Encoded:\n\t" + outMsg);
        }

        // Output the message
        out.println(outMsg);
        if (out.checkError() == true) {
            throw (new IOException());
        }

        // Courtesy flush
        out.flush();
        if (out.checkError() == true) {
            throw (new IOException());
        }
    }
}

