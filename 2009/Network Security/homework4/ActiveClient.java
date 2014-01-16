package homework4;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.awt.*;
import java.security.*;
import java.math.BigInteger;

class ActiveClient extends MessageParser implements Runnable
{

    //secure random numbers
    static SecureRandom sr = null;

    public static String MonitorName;
    Thread runner;
    Socket toMonitor = null;
    public static int MONITOR_PORT;
    public static int LOCAL_PORT;
    public int SleepMode;
    int DELAY = 60000;  //Interval after which a new Active Client is started
    long prevTime,present;

    public ActiveClient(String mname, int p, int lp, int sm, String name, String password, PrintWriter logWriter)
    {

        super(name, password, logWriter);

        try
        {

            SleepMode = sm;
            MonitorName = mname;
            MONITOR_PORT = p;
            LOCAL_PORT = lp;
            sr = new SecureRandom();

        }
        catch (NullPointerException n)
        {
            System.out.println("Active Client [Constructor]: TIMEOUT Error: " + n);
        }

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

        while(Thread.currentThread() == runner)
        {

            try
            {

                //controls encryption of SendIt()
                isEncrypted = false;

                System.out.print(IDENT + " Active Client: trying monitor: " + MonitorName + " port: " + MONITOR_PORT + "...");

                toMonitor = new Socket(MonitorName, MONITOR_PORT);
                System.out.println(" completed.\n");

                out = new PrintWriter(toMonitor.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(toMonitor.getInputStream()));
                HOSTNAME = toMonitor.getLocalAddress().getHostName();

                CType = 0;   //Indicates Client
                HOST_PORT = LOCAL_PORT;

                if (!Login())
                {
                   if (IsVerified == 0)
                   {
                       System.out.println(IDENT + " (Client): Login failed, exiting...\n");
                       System.exit(1);
                   }
                }

                System.out.println(IDENT + " (Client): Login() successful.\n");
                System.out.println("***************************\n");

                Random r = new Random();
                String newPass = Long.toString(Math.abs(r.nextLong()), 36);
                System.out.println("NewPass = " + newPass + "\n");
                ChangePassword(newPass);

                //change host port ever???

                if(!stopFreeflowPermanent)
                    MakeFreeFlowCommands();

                toMonitor.close();
                out.close();
                in.close();

                //Execute("SIGN_OFF");

                try
                {
                    runner.sleep(DELAY);
                }
                catch (Exception e) {}

            }
            catch (UnknownHostException e) {}
            catch (IOException e)
            {

                try
                {
                    toMonitor.close();
                    toMonitor = new Socket(MonitorName, MONITOR_PORT);
                }
                catch (IOException ioe) {}
                catch (NullPointerException n)
                {

                    try
                    {
                        toMonitor.close();
                        toMonitor = new Socket(MonitorName, MONITOR_PORT);
                    }
                    catch (IOException ioe) {}

                }

            }

        }

    }

    @Override
    public boolean Login()
    {

        boolean success = false;
        boolean fail = false;
        String msg = "";
        String [] splitMsg = null;
        String lastMsg = "";
        String cmd = "";
        String lastCmd = "";
        String expectedCmd = "";
        String expectedCmd2 = "";

        try
        {

            //client
            while(!success && !fail)
            {

                //get message from socket
                msg = GetMonitorMessage();

                //find what is REQUIRE'd
                cmd = GetNextCommand(msg, "");

                if(cmd == null)
                    cmd = "WAITING";

                //determine which command(s) is/are expected next
                if(lastCmd.equals(""))
                {
                    expectedCmd = "IDENT";
                    expectedCmd2 = "IDENT";
                }
                else if(lastCmd.equals("IDENT"))
                {
                    expectedCmd = "PASSWORD";
                    expectedCmd2 = "ALIVE";
                }
                else if(lastCmd.equals("PASSWORD"))
                {
                    expectedCmd = "HOST_PORT";
                    expectedCmd2 = "HOST_PORT";
                }
                else if(lastCmd.equals("ALIVE"))
                {
                    expectedCmd = "HOST_PORT";
                    expectedCmd2 = "WAITING"; //nothing REQUIRE'd, ready for freeflow commands
                }
                else if(lastCmd.equals("HOST_PORT"))
                {
                    expectedCmd = "WAITING"; //nothing REQUIRE'd, ready for freeflow commands
                    expectedCmd2 = "WAITING"; //nothing REQUIRE'd, ready for freeflow commands
                }

                if(DEBUG)
                    System.out.println(IDENT + " (Client): Expecting " + expectedCmd + " or " + expectedCmd2 + "... Recieved " + cmd + ".\n");

                //if current command is expected
                if(expectedCmd.equals(cmd) || expectedCmd2.equals(cmd))
                {

                    if(cmd.equals("IDENT"))
                    {

                        if(DEBUG)
                            System.out.println(IDENT + " (Client): IDENT requested, executing IDENT statement...\n");

                        Execute("IDENT");

                    }
                    else if(cmd.equals("PASSWORD"))
                    {

                        if(DEBUG)
                            System.out.println(IDENT + " (Client): PASSWORD requested, executing PASSWORD statement...\n");

                        Execute("PASSWORD");

                        //parse cookie from monitor response
                        msg = GetMonitorMessage();

                        cmd = GetNextCommand(msg, "");

                        try
                        {

                            String sDefault = "PASSWORD";

                            t = new StringTokenizer(msg, " ");

                            //Search for the PASSWORD directive
                            String temp = t.nextToken();

                            while (!(temp.trim().equals(sDefault.trim())))
                                temp = t.nextToken();

                            temp = t.nextToken();

                            //cookie found
                            COOKIE = temp;

                            if(DEBUG)
                                System.out.println(IDENT + " (Client): Cookie = " + temp + "\n");

                            //write password and cookie data to file
                            WritePersonalData(PASSWORD, COOKIE);

                            //next command should be HOST_PORT
                            if(cmd.equals("HOST_PORT"))
                            {

                                if(DEBUG)
                                    System.out.println(IDENT + " (Client): HOST_PORT requested, executing HOST_PORT statement...\n");

                                Execute("HOST_PORT");

                            }

                        }
                        catch (NoSuchElementException e)
                        {

                            //return null;

                        }

                    }
                    else if(cmd.equals("ALIVE"))
                    {

                        if(DEBUG)
                            System.out.println(IDENT + " (Client): ALIVE requested, executing ALIVE statement...\n");

                        Execute("ALIVE");

                    }
                    else if(cmd.equals("HOST_PORT"))
                    {

                        if(DEBUG)
                            System.out.println(IDENT + " (Client): HOST_PORT requested, executing HOST_PORT statement...\n");

                        Execute("HOST_PORT");

                    }
                    else if(cmd.equals("WAITING"))
                    {

                        //monitor is now waiting for our commands, login successful
                        if(DEBUG)
                            System.out.println(IDENT + " (Client): Login() success = true\n");


//success = true;
//IsVerified = 1;
                        
                        
                        boolean gcdPass = false;

                        while(!gcdPass)
                        {

                            //choose a secure random value S
                            sr = new SecureRandom();
                            secretS = new BigInteger(512, sr);
                            //secretS = new BigInteger("7");

                            //check that GCD(S, n) = 1
                            if(secretS.gcd(monitorNInt).equals(new BigInteger("1")))
                                gcdPass = true;

                        }

                        publicV = secretS.modPow(new BigInteger("2"), monitorNInt);

                        if(DEBUG)
                            System.out.println(IDENT + " (Client): Secret S and Public V computed, making certificate...\n");

                        try
                        {

                            SendIt("MAKE_CERTIFICATE " + publicV.toString(32) + " " + monitorNInt.toString(32), out);

                        }
                        catch(IOException e)
                        {

                            e.printStackTrace(System.out);

                        }

                        //get MAKE_CERTIFICATE response
                        msg = GetMonitorMessage();

                        //make sure the RESULT is a CERTIFICATE
                        if(GetNextCommand(msg, "RESULT").equals("CERTIFICATE"))
                        {

                            //convert certificate from radix 32 string to integer
                            myCert = new BigInteger(GetNextCommand(msg, IDENT.toUpperCase()), 32);

                            System.out.println(IDENT + " (Client): Certificate made.\n");

                            success = true;
                            IsVerified = 1;

                            if(DEBUG)
                                System.out.println(IDENT + " (Client): Certificate = " + myCert + "\n");
/*
                            System.out.println("Checking own certificate...\n");

                            //check own certificate
                            if(checkCertificate(myCert, publicV, monitorNInt))
                                System.out.println("Certificate is OK!\n");
                            else
                                System.out.println("Certificate fail!\n");
*/
                        }
                        else
                        {

                            //certificate was not made, abort login
                            System.out.println(IDENT + " (Client): Error: Certificate result not found.\n");

                            success = false;
                            fail = true;

                        }



                    }

                }
                else
                {

                    System.out.println(IDENT + " (Client): Login failed. Expected " + expectedCmd + " or " + expectedCmd2 + " from monitor, got " + cmd + ".\n");

                    fail = true;
                    success = false;

                }

                //update last vars for next message group
                lastMsg = msg;
                lastCmd = cmd;

            }

        }
        catch (NullPointerException n)
        {

            System.out.println(IDENT + " (Client) [Login]: null pointer error at login:\n " + n);
            n.printStackTrace(System.out);

            fail = true;
            success = false;

        }

        return success;

    }

    @Override
    public void initiateTransactionRequest(String filename, String sender, String recipient, String amount)
    {

        int numRounds = 54;
        String msg = "";
        String cmd = "";
        String lastMsg = "";
        String lastCmd = "";
        String expectedCmd = "";
        String transferAuthorizeSetString = "";
        String[] transferAuthorizeSet = new String[numAuthRounds];
        String transferSubsetAString = "";
        String[] transferSubsetA;
        String transferSubsetKString = "";
        String[] transferSubsetK;
        String transferSubsetJString = "";
        String[] transferSubsetJ;
        String transferRandomSetString = "";
        String[] transferRandomSet = new String[numAuthRounds];
        Boolean recipientStop = false;
        DHKey key;

        //recipient
        try
        {

            SendIt("TRANSFER_REQUEST " + recipient + " " + amount + " FROM " + sender, out);

            while(!recipientStop)
            {

                //get command and full message
                msg = GetMonitorMessage();
                cmd = GetNextCommand(msg, "");

                //determine which command(s) are expected based on last command
                if(lastCmd.equals(""))
                {
                    expectedCmd = "PUBLIC_KEY";
                }
                else if(lastCmd.equals("PUBLIC_KEY"))
                {
                    expectedCmd = "AUTHORIZE_SET";
                }
                else if(lastCmd.equals("AUTHORIZE_SET"))
                {
                    expectedCmd = "SUBSET_K";
                }
                else if(lastCmd.equals("SUBSET_K"))
                {
                    expectedCmd = "SUBSET_J";
                }

                if(cmd == null)
                {
                    System.out.println(IDENT + " (Client): Transfer error: " + msg + "\n");
                    recipientStop = true;
                }
                else if(!(cmd.equals(expectedCmd)))
                {

                    //unexpected messsage, abort transfer
                    System.out.println(IDENT + " (Client): Transfer aborted: Expected " + expectedCmd + ", received " + cmd + "\n");
                    recipientStop = true;

                }
                else if(cmd.equals("PUBLIC_KEY"))
                {

                    if(DEBUG)
                        System.out.println(IDENT + " (Client): Sending PUBLIC_KEY\n");

                    SendIt("PUBLIC_KEY " + publicV.toString(32) + " " + monitorNInt.toString(32), out);

                }
                else if(cmd.equals("AUTHORIZE_SET"))
                {

                    numRounds = new Integer(GetNextCommand(msg, "ROUNDS"));

                    if(DEBUG)
                        System.out.println(IDENT + " (Client): Received ROUNDS " + Integer.toString(numRounds) + "\n");

                    //minimum 50 rounds required
                    if(numRounds >= 49)
                    {

                        transferAuthorizeSetString = "";
                        BigInteger randomBigInt;

                        //for each round, square the random number, mod by n
                        for(int lcv = 0; lcv < numRounds; lcv++)
                        {

                            //choose a random number
                            sr = new SecureRandom();
                            randomBigInt = new BigInteger(512, sr);
                            //randomBigInt = new BigInteger(8, sr);
                            transferRandomSetString += randomBigInt + " ";

                            //square the number, mod by n
                            randomBigInt = randomBigInt.modPow(new BigInteger("2"), monitorNInt);

                            //build authorize set string
                            transferAuthorizeSetString += randomBigInt + " ";

                        }

                        if(DEBUG)
                        {
                            System.out.println(IDENT + " (Client): Random Set = " + transferRandomSetString + "\n");
                            System.out.println(IDENT + " (Client): Sending AUTHORIZE_SET = " + transferAuthorizeSetString + "\n");
                        }

                        //split into string array
                        transferRandomSet = transferRandomSetString.split(" ");
                        transferAuthorizeSet = transferAuthorizeSetString.split(" ");

                        //send authorize set
                        SendIt("AUTHORIZE_SET " + transferAuthorizeSetString, out);

                    }
                    else
                    {

                        if(DEBUG)
                            System.out.println(IDENT + " (Client): Minimum of 50 rounds required.\n");

                        recipientStop = true;

                    }

                }
                else if(cmd.equals("SUBSET_K"))
                {

                    //find end of SUBSET_A list
                    int endSubsetA = msg.indexOf(" REQUIRE:");

                    //get SUBSET_A string
                    if(endSubsetA != -1 && msg.length() > 16)
                        transferSubsetAString = msg.substring(16, endSubsetA) + " ";

                    if(DEBUG)
                        System.out.println(IDENT + " (Client): Received SUBSET_A = " + transferSubsetAString + "\n");

                    //split into string array
                    transferSubsetA = transferSubsetAString.split(" ");

                    //go through each AUTHORIZE_SET element
                    for(int lcv = 0; lcv < numRounds; lcv++)
                    {

                        String lcvString = Integer.toString(lcv);

                        //if lcv is in SUBSET_A
                        if(transferSubsetAString.indexOf(" " + lcvString + " ") != -1)
                        {

                            //compute and add to SUBSET_K
                            BigInteger kValue = new BigInteger(transferRandomSet[lcv]);

                            //K = (secret * authorizeset) mod n
                            kValue = kValue.multiply(secretS);
                            kValue = kValue.mod(monitorNInt);

                            transferSubsetKString += kValue + " ";

                        }
                        else
                        {

                            //compute and add to SUBSET_J
                            BigInteger jValue = new BigInteger(transferRandomSet[lcv]);

                            //J = authorizeset mod n
                            jValue = jValue.mod(monitorNInt);

                            transferSubsetJString += jValue + " ";

                        }

                    }

                    if(DEBUG)
                        System.out.println(IDENT + " (Client): Sending SUBSET_K = " + transferSubsetKString + "\n");

                    //send SUBSET_K
                    SendIt("SUBSET_K " + transferSubsetKString, out);

                }
                else if(cmd.equals("SUBSET_J"))
                {

                    if(DEBUG)
                        System.out.println(IDENT + " (Client): Sending SUBSET_J = " + transferSubsetJString + "\n");

                    //send SUBSET_J
                    SendIt("SUBSET_J " + transferSubsetJString, out);

                    //look for TRANSFER_RESPONSE result and output
                    String transferResult = GetNextCommand(GetMonitorMessage(), "TRANSFER_RESPONSE");
                    System.out.println(IDENT + " (Client): TRANSFER " + transferResult + "\n");

                    //transfer done
                    recipientStop = true;

                }

                lastMsg = msg;
                lastCmd = cmd;

            }

        }
        catch(IOException e)
        {
            System.out.println("TRANSFER_REQUEST IOException.\n");
            e.printStackTrace(System.out);
        }

    }

}
