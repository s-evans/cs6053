
public class CommandTransferServer extends Command {
    protected Sender mSender;

    public CommandTransferServer(String args) {
        super(args);
        throw new Exception("not implemented");
    }

    public CommandTransferServer(
            String recipient, Integer amount, String sender) {
        // Create parent class
        super(recipient, amount, sender);

        // Create ZKP sender helper
        mSender = new Sender(64);
    }

    protected boolean Transfer() throws Exception {
        // Receive a transfer message from the monitor
        MessageTransfer msgXferReq = (MessageTransfer) mMtp.recv();

        // Validate the sender and receiver user names make sense
        if ( !ShouldWeConsiderThisTransfer(
                    msgXferReq.mRecipientIdent,
                    msgXferReq.mPointsRequested,
                    msgXferReq.mSenderIdent ) ) {
            throw new Exception("Not even considering transferring " 
                    + amount + " from " + senderName + " to " + recipientName);
        }

        return true;
    }

    protected boolean PublicKey() throws Exception {
        // Get the public key result message from the monitor
        MessageResult msgResult = (MessageResult) mMtp.recv();

        // Validate the command referenced by the result
        if ( !msgResult.mCommand.equals("PUBLIC_KEY") ) {
            throw new Exception("Unexpected result command type; exp = " + "PUBLIC_KEY" + "; act = " + msgResult.mCommand + ";");
        }

        // Create public key message from the result message
        MessagePublicKey msgPubKey = new MessagePublicKey(msgResult.mResult);

        // Set the public key internally
        mSender.setPublicKey(msgPubKey.mV, msgPubKey.mN);

        return true;
    }

    protected boolean Rounds() throws Exception {
        // Get the ROUNDS command message from the montior
        ParserHelper ph = new ParserHelper(mMtp);
        if ( !ph.parseToCommand("ROUNDS") ) { 
            throw new Exception("Failed to get ROUNDS command message");
        }

        // Wait to receive the WAITING message from the monitor
        if ( !ph.parseToWait() ) {
            throw new Exception("Failed to get waiting message");
        }

        // Create a ROUNDS message
        MessageRounds msgRounds = new MessageRounds(mSender.getRounds());

        // Send a ROUNDS message to the monitor
        mMtp.send(msgRounds);

        return true;
    }

    protected boolean AuthorizeSet() throws Exception {
        // Get the AUTHORIZE_SET result message from the monitor
        MessageResult msgResult = (MessageResult) mMtp.recv();

        // Validate the result message
        if ( !msgResult.mCommand.equals("AUTHORIZE_SET") ) {
            throw new Exception("Unexpected result message cmd; exp = " + "AUTHORIZE_SET" + ";" + msgResult.mCommand + ";");
        }

        // Create an authorize set message from the result message 
        MessageAuthorizeSet msgAuthSet = 
            new MessageAuthorizeSet(msgResult.mResult);

        // Set the authorize set internally
        mSender.setAuthorizeSet(msgAuthSet.mSet.toArray(new String[msgAuthSet.mSet.size()]));

        return true;
    }

    protected boolean SubsetA() throws Exception {
        // Get the SUBSET_A command message from the monitor
        ParserHelper ph = new ParserHelper(mMtp);
        if ( !ph.parseToCommand("SUBSET_A") ) { 
            throw new Exception("Failed to get SUBSET_A command message");
        }

        // Wait to receive the WAITING message from the monitor
        if ( !ph.parseToWait() ) {
            throw new Exception("Failed to get waiting message");
        }

        // Create a SUBSET_A command message
        MessageSubsetA msgSubA = new MessageSubA(mSender.getSubsetA());

        // Send the SUBSET_A command message
        mMtp.send(msgSubA);

        return true;
    }

    protected boolean SubsetK() throws Exception {
        // Get the SUBSET_K result message from the monitor
        MessageResult msgResult = (MessageResult) mMtp.recv();
        
        // Verify the command referenced in the result message
        if ( !msgResult.mCommand.equals("SUBSET_K") ) { 
            throw new Exception("Unexpected result message command; exp = " + "SUBSET_K" + "; act = " + msgResult.mCommand + ";");
        }

        // Create a SUBSET_K message from the result message
        MessageSubsetK msgSubK = new MessageSubsetK(msgResult.mResult);

        // Evaluate the SUBSET_K result message
        if ( !mSender.checkSubsetK(msgSubK.mSet) ) {
            throw new Exception("SUBSET_K validation failed!");
        }

        return true;
    }

    protected boolean SubsetJ() throws Exception {
        // Get the SUBSET_J result message from the monitor
        MessageResult msgResult = (MessageResult) mMtp.recv();
        
        // Verify the command referenced in the result message
        if ( !msgResult.mCommand.equals("SUBSET_J") ) { 
            throw new Exception("Unexpected result message command; exp = " + "SUBSET_J" + "; act = " + msgResult.mCommand + ";");
        }

        // Create a SUBSET_J message from the result message
        MessageSubsetJ msgSubJ = new MessageSubsetJ(msgResult.mResult);

        // Evaluate the SUBSET_J result message
        if ( !mSender.checkSubsetJ(msgSubJ.mSet) ) {
            throw new Exception("SUBSET_J validation failed!");
        }

        return true;
    }

    protected boolean TransferResponse() throws Exception {
        // Get the TRANSFER_RESPONSE command message from the monitor
        ParserHelper ph = new ParserHelper(mMtp);
        if ( !ph.parseToCommand("TRANSFER_RESPONSE") ) { 
            throw new Exception("Failed to get TRANSFER_RESPONSE command message");
        }

        // Wait to receive the WAITING message from the monitor
        if ( !ph.parseToWait() ) {
            throw new Exception("Failed to get waiting message");
        }

        // Create the TRANSFER_RESPONSE message
        MessageTransferResponse msgXferResp = 
            new MessageTransferResponse(mSender.response());

        // Send the TRANSFER_RESPONSE message to the monitor
        mMtp.send(msgXferResp);

        return true;
    }

    protected boolean EvaluateTransfer(String nextMsg) throws Exception {

        // Handle the transfer command
        if ( !Transfer() ) {
            throw new Exception("Failed to receive a transfer request message");
        }

        // Handle the public key message
        if ( !PublicKey() ) { 
            throw new Exception("Failed to receive the public key message");
        }

        // Handle the rounds message
        if ( !Rounds() ) {
            throw new Exception("Failed to handle the rounds message");
        }

        // Handle the authorize set message
        if ( !AuthorizeSet() ) {
            throw new Exception("Failed to handle the AUTHORIZE_SET message");
        }

        // Handle the subset a message
        if ( !SubsetA() ) {
            throw new Exception("Failed to handle the subset a message");
        }

        // Handle the subset k result message
        if ( !SubsetK() ) {
            throw new Exception("Failed to handle the subset k result message");
        }

        // Handle the subset j result message
        if ( !SubsetJ() ) {
            throw new Exception("Failed to handle the subset j result message");
        }

        // Handle the transfer response message
        if ( !TransferResponse() ) {
            throw new Exception("Failed to handle the transfer reponse message");
        }

        return true;
    }

    private static boolean ShouldWeConsiderThisTransfer(String recipientName, Integer amount, String senderName) {
        boolean senderInOurGroup = IsUserInOurGroup(senderName);
        boolean recipientInOurGroup = IsUserInOurGroup(recipientName);

        if (senderInOurGroup && !recipientInOurGroup) {
            System.out.println("transfer is leaving our group");
            return false;
        } 

        if (!senderInOurGroup && recipientInOurGroup) {
            System.out.println("transfer is entering our group");
            return true;
        } 

        if (senderInOurGroup && recipientInOurGroup) {
            System.out.println("transfer is completely within our group");
            return true;
        } 

        System.out.println("transfer is completely outside of our group");
        return false;
    }

    protected static boolean IsUserInOurGroup(String username) {
        String user = username.toUpperCase();

        return (user == "ALIVE" || user == "IDENT" || user == "PASSWORD");
    }

    public boolean Execute() throws Exception {
        return EvaluateTransfer();
    }
}
