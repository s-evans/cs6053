

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.security.*;
import java.math.BigInteger;

class Server implements Runnable
{

    ServerSocket s = null;
    public static int MONITOR_PORT;
    public static int LOCAL_PORT;
    Thread runner;
    String IDENT;
    String PASSWORD;
    PrintWriter LOGWRITER;

    public Server(int p, int lp, String name, String password, PrintWriter logWriter)
    {

        IDENT = name;
        PASSWORD = password;
        LOGWRITER = logWriter;

        try
        {

            s = new ServerSocket(p);
            MONITOR_PORT = p;
            LOCAL_PORT = lp;
            int i = 1;

        }
        catch (IOException e) {}

    }

    public void start()
    {

        if (runner == null)
        {
            runner = new Thread(this);
            runner.start();
        }

    }

    public void run()
    {

        try
        {

            int i = 1;

            for (;;)
            {
                Socket incoming =  s.accept();
                new ConnectionHandler(incoming, i, IDENT, PASSWORD, LOGWRITER).start();
                //Spawn a new thread for each new connection
                i++;
            }

        }
        catch (Exception e)
        {
            System.out.println(IDENT + " Server [run]: Error in Server: "  + e);
        }

    }

}

class ConnectionHandler extends MessageParser implements Runnable
{

    private Socket incoming;
    private int counter;
    Thread runner;
    static MessageDigest md = null;
    static String[] approvedTransferIdents;

    public ConnectionHandler (Socket i, int c, String name, String password, PrintWriter logWriter)
    {

        super(name, password, logWriter);
        incoming = i;
        counter = c;
        isEncrypted = false;
        
        //team idents
        approvedTransferIdents = new String[] {"NEO", "REBELLLAMA", "BINARYBEATNIK", "CPTEST7"};

    }

    public void run()
    {

        try
        {

            in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            out = new PrintWriter(incoming.getOutputStream(),true);

            boolean done = false;
            HOST_PORT = Server.LOCAL_PORT;
            CType = 1;  //Indicates Server

            System.out.println(IDENT + " Starting login from Server...\n");

            if (Login())
            {

                System.out.println(IDENT + " (Server): Login() successful.\n");

            }
            else
            {

                System.out.println(IDENT + " (Server): Login() failed.\n");
                if (IsVerified != 1) { }

            }

            incoming.close();

        }
        catch (IOException e) {}
        catch (NullPointerException n) {}

    }

    public void start()
    {

        if (runner == null)
        {
            runner = new Thread(this);
            runner.start();
        }

    }

    @Override
    public boolean Login()
    {

        boolean success = false;
        boolean fail = false;
        String participantPasswordChecksum = "";
        String shaPasswordHash = "";
        String msg = "";
        String lastMsg = "";
        String[] splitMsg;
        String cmd = "";
        String lastCmd = "";
        String expectedCmd = "";
        String expectedCmd2 = "";

        //transfer vars
        String transferRequestIdent = "";
        boolean transferIdentApproved = false;
        boolean transferCertificateApproved = false;
        String transferAuthorizeSetString = "";
        String[] transferAuthorizeSet = null;
        String transferSubsetAString = "";
        String[] transferSubsetA = new String[numAuthRounds];
        String transferSubsetZString = "";
        String[] transferSubsetZ = new String[numAuthRounds];
        String transferSubsetKString = "";
        String[] transferSubsetK = new String[numAuthRounds];
        String transferSubsetJString = "";
        String[] transferSubsetJ = new String[numAuthRounds];
        String transferVString = "";
        String transferNString = "";
        BigInteger transferVInt = null;
        BigInteger transferNInt = null;
        BigInteger expectedValue = null;
        BigInteger actualValue = null;

        try
        {

            //server
            while(!success && !fail)
            {

                //get message from socket
                msg = GetMonitorMessage();

                //find what is REQUIRE'd
                cmd = GetNextCommand(msg, "");

                //should never happen, server always receives directives
                if(cmd == null)
                    cmd = "NULL";

                //determine which command(s) is/are expected next
                if(lastCmd.equals(""))
                {
                    expectedCmd = "IDENT";
                    expectedCmd2 = "IDENT";
                }
                else if(lastCmd.equals("IDENT"))
                {
                    expectedCmd = "ALIVE";
                    expectedCmd2 = "ALIVE";
                }
                else if(lastCmd.equals("ALIVE"))
                {

                    expectedCmd = "QUIT";
                    expectedCmd2 = "QUIT";

                    //if registered, ROUNDS may be coming for a transfer
                    if(IsVerified == 1 && acceptingTransactionRequests == true)
                        expectedCmd2 = "ROUNDS";

                }
                else if(lastCmd.equals("ROUNDS"))
                {
                    expectedCmd = "SUBSET_A";
                    expectedCmd2 = "SUBSET_A";
                }
                else if(lastCmd.equals("SUBSET_A"))
                {
                    expectedCmd = "TRANSFER_RESPONSE";
                    expectedCmd2 = "TRANSFER_RESPONSE";
                }
                else if(lastCmd.equals("TRANSFER_RESPONSE"))
                {
                    expectedCmd = "QUIT";
                    expectedCmd2 = "QUIT";
                }

                if(DEBUG)
                    System.out.println(IDENT + " (Server): Expecting " + expectedCmd + " or " + expectedCmd2 + "... Recieved " + cmd + ".\n");

                //if current command is expected
                if(expectedCmd.equals(cmd) || expectedCmd2.equals(cmd))
                {

                    if(cmd.equals("IDENT"))
                    {

                        //get password checksum from message group
                        participantPasswordChecksum = GetNextCommand(msg, "PARTICIPANT_PASSWORD_CHECKSUM");

                        if(DEBUG)
                            System.out.println(IDENT + " PARTICIPANT_PASSWORD_CHECKSUM from Monitor: " + participantPasswordChecksum + "\n");

                        try
                        {

                            //create and update SHA hash
                            md = MessageDigest.getInstance("SHA");
                            md.update(PASSWORD.toUpperCase().getBytes());

                            //this is SHA hash of password
                            shaPasswordHash = (new BigInteger(1, md.digest())).toString(16);

                            if(DEBUG)
                                System.out.println(IDENT + " (Server): SHA hash of password: " + shaPasswordHash + "\n");

                        }
                        catch(NoSuchAlgorithmException e)
                        {
                            System.err.println("Yow! NoSuchAlgorithmException. Abandon all hope.\n");
                        }

                        //compare monitor checksum to computed checksum
                        if(shaPasswordHash.equals(participantPasswordChecksum))
                        {

                            if(DEBUG)
                                System.out.println(IDENT + " (Server): IDENT requested, executing IDENT statement...\n");

                            Execute("IDENT");

                        }
                        else
                        {

                            System.out.println(IDENT + " (Server): Password checksums do not match, sieze the imposters!\n");
                            //out.write("Nice try a$$h0le, but no cigar.");
                            //out.flush();

                            fail = true;
                            success = false;

                        }

                    }
                    else if(cmd.equals("ALIVE"))
                    {

                        if(DEBUG)
                            System.out.println(IDENT + " (Server): ALIVE requested, executing ALIVE statement...\n");

                        Execute("ALIVE");

                    }
                    else if(cmd.equals("QUIT"))
                    {

                        if(DEBUG)
                            System.out.println(IDENT + " (Server): QUIT requested, executing QUIT statement...\n");

                        Execute("QUIT");

                        if(DEBUG)
                            System.out.println(IDENT + " (Server): Login() success = true\n");

                        success = true;
                        IsVerified = 1;

                    }
                    else if(cmd.equals("ROUNDS"))
                    {

                        transferRequestIdent = GetNextCommand(msg, "TRANSFER");

                        System.out.println(IDENT + " (Server): TRANSFER_REQUEST to IDENT " + transferRequestIdent + ". Checking approved transfer ident list...\n");

                        //go through each approved transfer ident
                        for(int lcv = 0; lcv < approvedTransferIdents.length; lcv++)
                        {

                            //check if transfer request ident is in the approved list
                            if(transferRequestIdent.equals(approvedTransferIdents[lcv]))
                                transferIdentApproved = true;

                        }
                        
/*
                        //get certificate of initiator and check
                        try
                        {

                            //this is causing COMMAND_ERROR: Command, GET_CERTIFICATE, was not expected in this context, please send ROUNDS REQUIRE: ROUNDS WAITING:
                            SendIt("GET_CERTIFICATE " + transferRequestIdent);

                            String msg2 = GetMonitorMessage();



                        }
                        catch(IOException e)
                        {

                            System.out.println(IDENT + " (Server): Certificate of transfer IDENT could not be approved, declining transfer...\n");
                            e.printStackTrace(System.out);

                        }
*/

                        if(transferIdentApproved)
                        {

                            System.out.println(IDENT + " (Server): TRANSFER_REQUEST to IDENT " + transferRequestIdent + " approved, starting transfer procedure...\n");

                            if(DEBUG)
                                System.out.println(IDENT + " (Server): ROUNDS requested, executing ROUNDS statement...\n");

                            //look for PUBLIC_KEY results
                            transferVString = GetNextCommand(msg, "PUBLIC_KEY");
                            transferNString = GetNextCommand(msg, transferVString);

                            transferVInt = new BigInteger(transferVString, 32);
                            transferNInt = new BigInteger(transferNString, 32);

                            if(DEBUG)
                            {
                                System.out.println("transferV = " + transferVString + "\n");
                                System.out.println("transferN = " + transferNString + "\n");
                            }

                            Execute("ROUNDS");

                        }
                        else
                        {

                            System.out.println(IDENT + " (Server): TRANSFER_REQUEST to IDENT " + transferRequestIdent + " NOT approved, possible imposter!\n");
                            acceptingTransactionRequests = false;

                        }

                    }
                    else if(cmd.equals("SUBSET_A"))
                    {

                        //find end of AUTHORIZE_SET list
                        int endAuthorizeSet = msg.indexOf(" REQUIRE:");

                        //get AUTHORIZE_SET string
                        if(endAuthorizeSet != -1 && msg.length() > 22)
                            transferAuthorizeSetString = msg.substring(22, endAuthorizeSet);

                        if(DEBUG)
                            System.out.println(IDENT + " (Server): Received AUTHORIZE_SET = " + transferAuthorizeSetString + "\n");

                        //split into string array
                        transferAuthorizeSet = transferAuthorizeSetString.split(" ");

                        for(int lcv = 0; lcv < numAuthRounds; lcv++)
                        {

                            //50% chance to include lcv in the subset
                            if(Math.random() < .5)
                            {
                                transferSubsetAString += lcv + " ";
                            }
                            else
                            {
                                transferSubsetZString += lcv + " ";
                            }

                        }

                        if(DEBUG)
                            System.out.println(IDENT + " (Server): Sending SUBSET_A = " + transferSubsetAString + "\n");

                        transferSubsetA = transferSubsetAString.split(" ");
                        transferSubsetZ = transferSubsetZString.split(" ");

                        try
                        {
                            SendIt("SUBSET_A " + transferSubsetAString);
                        }
                        catch(IOException e)
                        {
                            e.printStackTrace(System.out);
                        }

                    }
                    else if(cmd.equals("TRANSFER_RESPONSE"))
                    {


                        //find end of SUBSET_K list
                        int endSubsetK = msg.indexOf(" RESULT: SUBSET_J");

                        //get SUBSET_K string
                        if(endSubsetK != -1 && msg.length() > 17)
                            transferSubsetKString = msg.substring(17, endSubsetK);

                        if(DEBUG)
                            System.out.println(IDENT + " (Server): Received SUBSET_K = " + transferSubsetKString + "\n");

                        //split into string array
                        transferSubsetK = transferSubsetKString.split(" ");

                        //find end of SUBSET_J list
                        int endSubsetJ = msg.indexOf(" REQUIRE:");

                        //get SUBSET_J string
                        if(endSubsetJ != -1 && msg.length() > (endSubsetK + 18))
                            transferSubsetJString = msg.substring(endSubsetK + 18, endSubsetJ);

                        if(DEBUG)
                            System.out.println(IDENT + " (Server): Received SUBSET_J = " + transferSubsetJString + "\n");

                        //split into string array
                        transferSubsetJ = transferSubsetJString.split(" ");

                        boolean passCheck = true;

System.out.println("OMGZ TEZTTTTTT: " + new BigInteger("5").modPow(new BigInteger("3"), new BigInteger("19")));
System.out.println("transferVInt = " + transferVInt + "|END");
System.out.println("transferNInt = " + transferNInt + "|END\n");

                        //check SUBSET_K against expected results
                        for(int lcv = 0; lcv < transferSubsetA.length; lcv++)
                        {
System.out.println("transferSubsetK[" + lcv + "] = " + transferSubsetK[lcv] + "|END");
System.out.println("authorizeSetIndex = " + Integer.parseInt(transferSubsetA[lcv]) + "|END");
System.out.println("authorizeSetValue = " + transferAuthorizeSet[Integer.parseInt(transferSubsetA[lcv])] + "|END");
                            expectedValue = transferVInt.multiply(new BigInteger(transferAuthorizeSet[Integer.parseInt(transferSubsetA[lcv])]));
                            //expectedValue = expectedValue.mod(transferNInt); //transferNInt ???
                            //actualValue = new BigInteger(transferSubsetK[lcv]).multiply(new BigInteger(transferSubsetK[lcv]));
                            actualValue = new BigInteger(transferSubsetK[lcv]).modPow(new BigInteger("2"), transferNInt);
System.out.println("expectedValue = " + expectedValue + "\n" + "actualValue = " + actualValue + "|END\n");

                            if(!(expectedValue.equals(actualValue)))
                                passCheck = false;

                        }

System.out.println("\n&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n");

                        //check SUBSET_J against expected results
                        for(int lcv = 0; lcv < transferSubsetZ.length; lcv++)
                        {
System.out.println("transferSubsetJ[" + lcv + "] = " + transferSubsetJ[lcv] + "|END");
System.out.println("authorizeSetIndex = " + Integer.parseInt(transferSubsetZ[lcv]) + "|END");
System.out.println("authorizeSetValue = " + transferAuthorizeSet[Integer.parseInt(transferSubsetZ[lcv])] + "|END");
                            expectedValue = new BigInteger(transferAuthorizeSet[Integer.parseInt(transferSubsetZ[lcv])]);
                            //expectedValue = expectedValue.mod(transferNInt); //transferNInt ???
                            //actualValue = new BigInteger(transferSubsetJ[lcv]).multiply(new BigInteger(transferSubsetJ[lcv]));
                            actualValue = new BigInteger(transferSubsetJ[lcv]).modPow(new BigInteger("2"), transferNInt);
System.out.println("expectedValue = " + expectedValue + "\n" + "actualValue = " + actualValue + "|END\n");

                            if(!(expectedValue.equals(actualValue)))
                                passCheck = false;

                        }

                        try
                        {

                            //if results matched expected, accept transfer
                            if(passCheck)
                            {

                                System.out.println(IDENT + " (Server): Fiat-Shamir zero knowledge authentication passed, accepting transfer...\n");

                                SendIt("TRANSFER_RESPONSE ACCEPT");

                            }
                            else
                            {

                                //else decline transfer
                                System.out.println(IDENT + " (Server): Fiat-Shamir zero knowledge authentication failed, declining transfer...\n");

                                SendIt("TRANSFER_RESPONSE DECLINE");
                                
                            }

                        }
                        catch(IOException e)
                        {
                            e.printStackTrace(System.out);
                        }

                        //no longer accepting transactions
                        acceptingTransactionRequests = false;

                    }

                }
                else
                {

                    System.out.println(IDENT + " (Server): Login failed. Expected " + expectedCmd + " or " + expectedCmd2 + " from monitor, got " + cmd + ".\n");

                    fail = true;
                    success = false;
                    acceptingTransactionRequests = false;

                }

                //update last vars for next message group
                lastMsg = msg;
                lastCmd = cmd;

            }

        }
        catch (NullPointerException n)
        {

            System.out.println(IDENT + " (Server) [Login]: null pointer error at login:\n " + n);
            n.printStackTrace(System.out);

            fail = true;
            success = false;

        }

        return success;

    }

}
