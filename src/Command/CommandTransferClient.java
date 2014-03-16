
public class CommandTransferClient extends CommandTransfer {
    protected Initiator mInitiator;

    public CommandTransferClient(String args) {
        super(args);
        throw new Exception("not implemented");
    }

    public CommandTransferClient(
            String recipient, Integer amount, String sender) throws Exception {
        // Create parent class
        super(recipient, amount, sender);

        // Create ZKP initiator helper
        mInitiator = new Initiator();
    }

    protected boolean TransferRequest() throws Exception {
        // Create a transfer request message
        MessageTransferRequest msgXferReq = new MessageTransferRequest(
                mRecipientIdent, mAmount, mSenderIdent);

        // Send the transfer request message
        mMtp.send(msgXferReq);

        return true;
    }

    protected boolean PublicKey() throws Exception {
        // Receive a require public key command message from the monitor
        ParserHelper ph = new ParserHelper(mMtp);
        if ( !ph.parseToCommand("PUBLIC_KEY") ) { 
            throw new Exception("Failed to get public key command message");
        }

        // Wait to receive the WAITING message from the monitor
        if ( !ph.parseToWait() ) {
            throw new Exception("Failed to get waiting message");
        }

        // Create a public key message
        MesssagePublicKey msgPubKey = new 
            MesssagePublicKey(mInitiator.getV(), mInitiator.getN());

        // Send the public key message to the monitor
        mMtp.send(msgPubKey);
        
        // Should eventually get a result rounds message from the monitor
        MessageResult msgResult = (MessageResult) mMtp.recv();

        // Validate that the result message references the ROUNDS message
        if ( !msgResult.mCommand.equals("ROUNDS") ) {
            throw new Exception("Unexpected result message type; exp = " + "ROUNDS" + "; act = " + msgResult.mCommand + ";");
        }

        // Save off the rounds value returned
        mInitiator.setRounds(Integer.parseInt(msgResult.mResult)); 
        
        return true;
    }
    
    protected boolean AuthorizeSet() throws Exception {
        // Get an authorize set command message from the monitor
        ParserHelper ph = new ParserHelper(mMtp);
        if ( !ph.parseToCommand("AUTHORIZE_SET") ) { 
            throw new Exception("Failed to get public key command message");
        }

        // Wait to receive the WAITING message from the monitor
        if ( !ph.parseToWait() ) {
            throw new Exception("Failed to get waiting message");
        }

        // Create an authorize set message
        MessageAuthorizeSet msgAuthSet = new MessageAuthorizeSet(mInitiator.getAuthorizeSet());
        
        // Send the authorize set message
        mMtp.send(msgAuthSet);

        // Get the SUBSET_A result message from the monitor
        MessageResult msgResult = (MessageResult) mMtp.recv();

        // Validate the result message
        if ( !msgResult.mCommand.equals("SUBSET_A") ) {
            throw new Exception("Failed to validate result message");
        }

        // Parse SUBSET_A result message from monitor, getting values
        MessageSubsetA msgSubA = new MessageSubsetA(msgResult.mResult);

        // Set the values of subset A internally
        mInitiator.setSubsetA(msgSubA.mSet.toArray(new String[msgSubA.mSet.size()])); 

        return true;
    }

    protected boolean SubsetK() throws Exception {
        // Get the SUBSET_K command message from the monitor
        ParserHelper ph = new ParserHelper(mMtp);
        if ( !ph.parseToCommand("SUBSET_K") ) { 
            throw new Exception("Failed to get public key command message");
        }

        // Wait to receive the WAITING message from the monitor
        if ( !ph.parseToWait() ) {
            throw new Exception("Failed to get waiting message");
        }

        // Create the SUBSET_K message
        MessageSubsetK msgSubK = new MessageSubsetK(mInitiator.getSubsetK());

        // Send the SUBSET_K message to the monitor
        mMtp.send(msgSubK);

        // NOTE: No results as of yet
        
        return true;
    }

    protected boolean SubsetJ() throws Exception {
        // Get the SUBSET_J command message from the monitor
        ParserHelper ph = new ParserHelper(mMtp);
        if ( !ph.parseToCommand("SUBSET_J") ) { 
            throw new Exception("Failed to get public key command message");
        }

        // Wait to receive the WAITING message from the monitor
        if ( !ph.parseToWait() ) {
            throw new Exception("Failed to get waiting message");
        }

        // Create the SUBSET_J message
        MessageSubsetJ msgSubJ = new MessageSubsetJ(mInitiator.getSubsetJ());

        // Send the SUBSET_J message to the monitor
        mMtp.send(msgSubJ);

        // NOTE: No results as of yet
        
        return true;
    }

    protected boolean InitiateTransfer() {

        // Create and send a transfer request message
        if ( !TransferRequest() ) {
            throw new Exception("Failed to send a transfer request message");
        }

        // Do initial set operation
        if ( !AuthorizeSet() ) {
            throw new Exception("Failed to do initial set operation");
        }

        // Do second set operation
        if ( !SubsetK() ) { 
            throw new Exception("Failed to do second set operation");
        }

        // Do third set operation
        if ( !SubsetJ() ) {
            throw new Exception("Failed to do third set operation");
        }

        return true;
    }

    public boolean Execute() throws Exception {
        return InitiateTransfer();
    }
}
