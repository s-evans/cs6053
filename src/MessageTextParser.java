import java.io.*;

public class MessageTextParser {
    private BufferedReader in;
    private PrintWriter out;
    private MessageFactory msgFactory;

    // Constructor
    public MessageTextParser(
            BufferedReader input, 
            PrintWriter output, 
            MessageFactory messageFactory) {
        in = input;
        out = output;
        msgFactory = messageFactory;
    }

    // Get a message string from the stream
    public Message Recv() throws Exception {
        Message msg;
        String currentLine;

        // TODO: Think about adding a timeout to avoid waiting forever

        // Read the next line. 
        currentLine = in.readLine();
        System.out.println("MessageTextParser [Recv]: Returning " + currentLine);

        // TODO: Pass the message to the decode stack

        // Create the message object 
        msg = msgFactory.createMsg(currentLine);

        return msg;
    }

    public void Send(Message msg) throws Exception {

        System.out.println("MessageTextParser [Send]: plaintext message:\n\t" + msg.serialize());

        // TODO: Pass to the encode stack

        // Output the message
        out.println(msg.serialize());
        if (out.checkError() == true)
            throw (new IOException());

        // Courtesy flush
        out.flush();
        if (out.checkError() == true)
            throw (new IOException());

    }
}

