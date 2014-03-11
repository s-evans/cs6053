import java.io.*;
import java.util.List;
import java.util.Iterator;

public class MessageTextParser {
    private BufferedReader in;
    private PrintWriter out;
    private MessageFactory msgFactory;
    private List<Codec> mCodecList;

    // Constructor
    public MessageTextParser(
            BufferedReader input, 
            PrintWriter output, 
            MessageFactory messageFactory) {
        in = input;
        out = output;
        msgFactory = messageFactory;
    }

    // Add a codec to send and receive operations symetrically
    public void addCodec ( Codec c ) {
        mCodecList.add(c);
    }

    // Get a message string from the stream
    public Message recv() throws Exception {
        Message msg;
        String line;

        // TODO: Think about adding a timeout to avoid waiting forever
        // TODO: Think about setting an upper bound to avoid running out of memory

        // Read the next line. 
        line = in.readLine();
        System.out.println("MessageTextParser [Recv]: Returning " + line);

        // Pass the message to the decode stack
        Iterator<Codec> iter = mCodecList.iterator();
        String postLine = line;
        while (  iter.hasNext() ) {
            postLine = iter.next().decode(postLine);
        }

        // Create the message object 
        msg = msgFactory.createMsg(postLine);

        return msg;
    }

    public void send(Message msg) throws Exception {

        System.out.println("MessageTextParser [Send]: plaintext message:\n\t" + msg.serialize());

        // Pass the message to the encode stack
        Iterator<Codec> iter = mCodecList.iterator();
        String outMsg = msg.serialize();
        while (  iter.hasNext() ) {
            outMsg = iter.next().encode(outMsg);
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

