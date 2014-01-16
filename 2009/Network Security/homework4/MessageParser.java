package homework4;

import java.util.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.math.*;

public class MessageParser
{

    public static boolean matchChecksum = true;
    public static String spoofIdent = "";
    public static String spoofCookie = "";
    public static boolean stopFreeflowPermanent = false;

    //monitor public key information
    String monitorNString = "hm06lgk3bh9ubmrkoi0iidjkeis4deshgmubofbn3sjnktsqo8dhatb3n11ojpa4206nrp29i20c89g44gkvdsiu893mko6o1kcns1";
    BigInteger monitorNInt = new BigInteger(monitorNString, 32);
    BigInteger monitorEInt = new BigInteger("65537");
    //BigInteger monitorNInt = new BigInteger("23");

    //monitor certificate information
    String monitorCertString = "eqkgqdb4ul308qgkc2bdh5urab2hfkqtvpr99bncqarhpsoqt551onqt9f6mf24m9tg7107eedul89ep0rsattspju6vtenhgse74l";
    BigInteger monitorCertInt = new BigInteger(monitorCertString, 32);

    //view debug messages in stdout
    boolean DEBUG = true;

    //transfer vars
    public static boolean acceptingTransactionRequests = false;
    int numAuthRounds = 50;
    
    //certificate vars
    public static BigInteger secretS = null;
    public static BigInteger publicV = null;
    public static BigInteger myCert = null;

    //Monitor Handling Declarations
    int COMMAND_LIMIT = 25;
    public int CType; //used to determine client or server
    public static String HOSTNAME;
    PrintWriter out = null;
    BufferedReader in = null;
    String mesg,sentmessage;
    String filename;
    StringTokenizer t;
    String IDENT = "Skipper";
    String PASSWORD = "franco";
    static String COOKIE ="bkuhn";
    String PPCHECKSUM="";
    int HOST_PORT;
    public static int IsVerified;
    boolean isEncrypted = false;

    //File I/O Declarations
    BufferedReader fIn = null;
    PrintWriter fOut = null;
    static String InputFileName = "Input.dat";
    static String ResourceFileName = "Resources.dat";
    String[] cmdArr = new String[COMMAND_LIMIT];

    static String MyKey;
    String MonitorKey;
    BigInteger pubKey;
    BigInteger monKey;
    BigInteger sharedSecret;
    String first;
    ObjectInputStream oin = null;
    ObjectOutputStream oout = null;
    DiffieHellmanExchange DHExchange;
    Karn karn;

    String logfile = null;
    //FileWriter logfileFile = null;
    PrintWriter logfileWriter = null;

    public MessageParser(String ident, String password, PrintWriter logWriter)
    {

        logfile = ident + ".log"; //place timestamp in log file name
        filename = ident + ".dat";
        PASSWORD = password;
        IDENT = ident;

        logfileWriter = logWriter;

        GetIdentification();

        PlantDHKey initDHKey = new PlantDHKey();

        DHExchange = new DiffieHellmanExchange();

        try
        {
            pubKey = DHExchange.getDHParmMakePublicKey("DHKey");
        }
        catch(Exception e)
        {
            System.out.println("Cannot read from DHKey file.");
        }

    }

    public String GetMonitorMessage()
    {

        String sMesg="", decrypt="";

        try
        {

            String temp = "";
            String log = "";

            //build log file string
            log = "Incoming Monitor Message ";

            if(isEncrypted)
            {
                temp = karn.decrypt(in.readLine());
                log += "[E] ";
            }
            else
            {
                temp = in.readLine();
                log += "[D] ";
            }

            if(CType == 0)
                log += "to client:\n";
            else
                log += "to server:\n";

            first = temp; // 1st

            if(first.length() >= 13 && first.substring(0, 13).equals("RESULT: IDENT"))
            {

                //get encryption key from monitor response
                MonitorKey = first.substring(14);
                karn = new Karn(DHExchange.getSecret(MonitorKey, out));
                isEncrypted = true;

            }

            sMesg = temp;
            decrypt = temp;

            //After IDENT has been sent-to handle partially encrypted msg group
            while(!(decrypt.trim().equals("WAITING:")))
            {

                temp = in.readLine();
                sMesg = sMesg.concat(" ");

                if(isEncrypted)
                    decrypt = karn.decrypt(temp);
                else
                    decrypt = temp;

                sMesg = sMesg.concat(decrypt);

            } //sMesg now contains the Message Group sent by the Monitor

            log += sMesg + "\n\n";

            //write message group to log file
            logfileWriter.println(log);
            logfileWriter.flush();

        }
        catch (IOException e)
        {

            System.out.println("MessageParser [getMonitorMessage]: error in GetMonitorMessage:\n " + e + this);
            e.printStackTrace(System.out);

            sMesg="";

        }
        catch (NullPointerException n)
        {

            sMesg = "";

        }
        catch (NumberFormatException o)
        {

            System.out.println("MessageParser [getMonitorMessage]: number format error:\n " + o.getLocalizedMessage() + this);
            o.printStackTrace(System.out);

            sMesg="";

        }
        catch (NoSuchElementException ne)
        {

            System.out.println("MessageParser [getMonitorMessage]: no such element exception occurred:\n " + this);
            ne.printStackTrace(System.out);

        }
        catch (ArrayIndexOutOfBoundsException ae)
        {

            System.out.println("MessageParser [getMonitorMessage]: AIOB EXCEPTION!\n " + this);
            ae.printStackTrace(System.out);

            sMesg="";

        }

        return sMesg;

    }

    //Handling Cookie and PPChecksum
    public String GetNextCommand (String mesg, String sCommand)
    {

        try
        {

            String sDefault = "REQUIRE";

            if (!(sCommand.equals("")))
                sDefault = sCommand;

            t = new StringTokenizer(mesg, " :\n");

            //Search for the REQUIRE Command
            String temp = t.nextToken();

            while (!(temp.trim().equals(sDefault.trim())))
                temp = t.nextToken();

            temp = t.nextToken();

            if(DEBUG)
                System.out.println(IDENT + " [getNextCommand] returning: " + temp + "\n");

            return temp;  //returns what the monitor wants

        }
        catch (NoSuchElementException e)
        {

            return null;

        }

    }

    //function override in Server and ActiveClient classes
    public boolean Login()
    {

        return false;

    }

    //Handle Directives and Execute appropriate commands
    public boolean Execute (String sentmessage)
    {

        boolean success = false;

        try
        {

            if(sentmessage.trim().equals("IDENT"))
            {

                sentmessage = sentmessage.concat(" ");
                if(spoofIdent.equals(""))
                    sentmessage = sentmessage.concat(IDENT);
                else
                    sentmessage = sentmessage.concat(spoofIdent);
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(pubKey.toString(32));

                if(DEBUG)
                    System.out.println(IDENT + " sending unencrypted message: " + sentmessage + "\n");

                SendIt (sentmessage, out);
                success = true;

            }
            else if(sentmessage.trim().equals("PASSWORD"))
            {

                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(PASSWORD);

                if(DEBUG)
                    System.out.println(IDENT + " sending encrypted message: " + sentmessage + "\n");

                SendIt (sentmessage.trim(), out);
                success = true;

            }
            else if(sentmessage.trim().equals("HOST_PORT"))
            {

                sentmessage = sentmessage.concat(" ");
                //sentmessage = sentmessage.concat(HOSTNAME);//hostname
                sentmessage = sentmessage.concat("216.196.244.34");
                //sentmessage = sentmessage.concat("10.24.144.223");
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(String.valueOf(HOST_PORT));

                if(DEBUG)
                    System.out.println(IDENT + " sending encrypted message: " + sentmessage + "\n");

                SendIt (sentmessage, out);
                success = true;

            }
            else if(sentmessage.trim().equals("ALIVE"))
            {

                sentmessage = sentmessage.concat(" ");
                if(spoofCookie.equals(""))
                    sentmessage = sentmessage.concat(COOKIE);
                else
                    sentmessage = sentmessage.concat(spoofCookie);

                if(DEBUG)
                    System.out.println(IDENT + " sending encrypted message: " + sentmessage + "\n");

                SendIt (sentmessage, out);
                success = true;

            }
            else if(sentmessage.trim().equals("QUIT"))
            {

                if(DEBUG)
                    System.out.println(IDENT + " sending encrypted message: " + sentmessage + "\n");

                SendIt(sentmessage, out);
                success = true;

            }
            else if(sentmessage.trim().equals("SIGN_OFF"))
            {

                if(DEBUG)
                    System.out.println(IDENT + " sending encrypted message: " + sentmessage + "\n");

                SendIt(sentmessage, out);
                success = true;

            }
            else if(sentmessage.trim().equals("GET_GAME_IDENTS"))
            {

                if(DEBUG)
                    System.out.println(IDENT + " sending encrypted message: " + sentmessage + "\n");

                SendIt(sentmessage, out);
                success = true;

            }
            else if(sentmessage.trim().equals("PARTICIPANT_STATUS"))
            {

                if(DEBUG)
                    System.out.println(IDENT + " sending encrypted message: " + sentmessage + "\n");

                SendIt(sentmessage, out);
                success = true;

            }
            else if(sentmessage.trim().equals("RANDOM_PARTICIPANT_HOST_PORT"))
            {

                if(DEBUG)
                    System.out.println(IDENT + " sending encrypted message: " + sentmessage + "\n");

                SendIt(sentmessage, out);
                success = true;

            }
            else if(sentmessage.trim().equals("ROUNDS"))
            {

                if(DEBUG)
                    System.out.println(IDENT + " sending encrypted message: " + sentmessage + "\n");

                SendIt(sentmessage + " " + numAuthRounds, out);
                success = true;

            }
            else if(sentmessage.trim().equals("GET_MONITOR_KEY"))
            {

                if(DEBUG)
                    System.out.println(IDENT + " sending encrypted message: " + sentmessage + "\n");

                SendIt(sentmessage, out);
                success = true;

            }

        }
        catch (IOException e)
        {

            System.out.println(IDENT + " IOError:\n " + e);
            e.printStackTrace(System.out);

            success = false;

        }
        catch (NullPointerException n)
        {

            System.out.println(IDENT + " Null Error has occured.\n");
            n.printStackTrace(System.out);

            success=false;

        }

        return success;

    }

    public void SendIt (String message, PrintWriter pwOut) throws IOException
    {

        String log;
        String unencryptedMessage;

        try
        {

            //build log message
            log = "Outgoing message ";
            unencryptedMessage = message;

            //encrypt message if appropriate
            if(isEncrypted)
            {
                log += "[E] ";
                message = karn.encrypt(message);
            }
            else
                log += "[D] ";

            if(CType == 0)
                log += "from client:\n";
            else
                log += "from server:\n";

            log += unencryptedMessage + "\n\n";

            //write message to log
            logfileWriter.println(log);
            logfileWriter.flush();

            if(DEBUG)
                System.out.println(IDENT + " [SendIt] sent: " + message + "\n");

            pwOut.println(message);
            if (pwOut.checkError() == true) throw (new IOException());
            pwOut.flush();
            if(pwOut.checkError() == true) throw (new IOException());

        }
        catch (IOException e) 
        {

            e.printStackTrace(System.out);

        } //Bubble the Exception upwards

    }

    //In future send parameters here so that diff commands are executed
    public boolean ProcessExtraMessages()
    {

        boolean success = false;

        System.out.println("MessageParser [ExtraCommand]: received:\n " + mesg.trim());

        if ((mesg.trim().equals("")) || (mesg.trim().equals(null)))
        {

            mesg = GetMonitorMessage();
            System.out.println("MessageParser [ExtraCommand]: received (2):\n " + mesg.trim());

        }

        String id = GetNextCommand (mesg, "");

        if (id == null)
        { // No Require, can Launch Free Form Commands Now

            if (Execute("PARTICIPANT_STATUS"))
            { //Check for Player Status

                mesg = GetMonitorMessage();
                success = true;

                try
                {

                    SaveResources(mesg);  //Save the data to a file
                    SendIt("SYNTHESIZE WEAPONS", out);
                    mesg = GetMonitorMessage();
                    SendIt("SYNTHESIZE COMPUTERS", out);
                    mesg = GetMonitorMessage();
                    SendIt("SYNTHESIZE VEHICLES", out);
                    mesg = GetMonitorMessage();

                    if (Execute("PARTICIPANT_STATUS"))
                    { //Check for Player Status

                        mesg = GetMonitorMessage();
                        success = true;
                        SaveResources(mesg);//Save the data to a file

                    }

                } catch (IOException e) {}

            }

        }
        else
        {

            mesg = GetMonitorMessage();
            System.out.println("MessageParser [ExtraCommand]: failed extra message parse");

        }

        return success;

    }

    public void MakeFreeFlowCommands() throws IOException
    {

        String consoleInput;
        Boolean input = true;

        while(input)
        {

            consoleInput = System.console().readLine("Ready for freeflow command: ");

            if(consoleInput.equals("STOP_FREEFLOW"))
                input = false;
            else if(consoleInput.equals("STOP_FREEFLOW_PERMANENT"))
            {
                input = false;
                stopFreeflowPermanent = true;
            }
            else if(consoleInput.equals("INITIATE"))
            {

                //get transfer input info
                String transferIdent = System.console().readLine("To whom would you like to initiate a transfer with? ");
                String transferAmount = System.console().readLine("How many points would you like to request from " + transferIdent + "? ");

                initiateTransactionRequest("DHKey", transferIdent, IDENT, transferAmount);

            }
            else if(consoleInput.equals("ACCEPT"))
            {
                acceptTransactionRequest();
            }
            else if(consoleInput.equals("CHANGE_PASSWORD"))
            {

                consoleInput = System.console().readLine("Enter new password: ");
                ChangePassword(consoleInput);

            }
            else if(consoleInput.equals("ATTACK"))
            {

                attackVictim();

            }
            else if(consoleInput.equals("SPOOF_SERVER"))
            {

                //if we are spoofing, we do not care about the PARTICIPANT_PASSWORD_CHECKSUM matching
                matchChecksum = false;
                spoofIdent = System.console().readLine("Whose IDENT would you like to spoof? ");
                spoofCookie = System.console().readLine("What is " + spoofIdent + "'s cookie? ");

            }
            else
            {

                SendIt(consoleInput, out);
                System.out.println(IDENT + " Monitor response: " + GetMonitorMessage() + "\n");

            }

        }

    }

    public void SaveResources(String res) throws IOException
    {

        System.out.println("MessageParser [SaveResources]:");

        try
        {  // If an error occurs then don't update the Resources File

            String temp = GetNextCommand (res, "COMMAND_ERROR");

            if ((temp == null) || (temp.equals("")))
            {

                fOut = new PrintWriter(new FileWriter(ResourceFileName));
                t = new StringTokenizer(res," :\n");

                try
                {

                       temp = t.nextToken();
                       temp = t.nextToken();
                       temp = t.nextToken();

                       System.out.println("MessageParser [SaveResources]: got token before write: " + temp);

                       for (int i=0 ; i < 20 ; i++)
                       {

                            fOut.println(temp);
                            fOut.flush();
                            temp = t.nextToken();

                       }

                }
                catch (NoSuchElementException ne)
                {

                    temp = "";
                    fOut.close();

                }

            }
            fOut.close();

        }
        catch (IOException e)
        {
            fOut.close();
        }

    }
    
    public void ChangePassword(String newpassword)
    {

        GetIdentification(); //Gives u the previous values of Cookie and Password
        String quer = "CHANGE_PASSWORD " + PASSWORD + " " + newpassword;
        UpdatePassword(quer, newpassword);

    }

    //Update Password
    //throws IOException
    public void UpdatePassword(String cmd, String newpassword)
    {

        String result = "";
        String command = "";
        String newCookie = "";

        //execute string cmd, update password in file if successful
        try
        {

            SendIt(cmd, out);

            result = GetMonitorMessage();
            System.out.println("result = " + result);
            
            //check for command error
            if((command = GetNextCommand(result, "COMMAND_ERROR")) != null)
            {

                if(command.equals("Old"))
                {
                    System.out.println("COMMAND_ERROR: Old player password is invalid.\n");
                }
                else if(command != null)
                {
                    System.out.println("COMMAND_ERROR: Password not changed.\n");
                }

            }
            else if((command = GetNextCommand(result, "RESULT")) != null)
            {

                //check for change password result
                if(command.equals("CHANGE_PASSWORD"))
                {

                    //get new cookie from response
                    newCookie = result.substring(24, result.length() - 9);

                    if(DEBUG)
                        System.out.println("newCookie = " + newCookie);

                    PASSWORD = newpassword;
                    COOKIE = newCookie;

                    //write new password and cookie to dat file
                    if(WritePersonalData(newpassword, newCookie))
                        System.out.println(IDENT + " password successfully changed!\n");

                }

            }

        }
        catch(IOException e)
        {

        }

    }

    public void GetIdentification()
    {

        try
        {

            FileInputStream fstream = new FileInputStream(filename);

            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;
            int lineCount = 0;

            while((strLine = br.readLine()) != null)
            {

                if(lineCount == 1)
                    PASSWORD = strLine;
                else if(lineCount == 3)
                    COOKIE = strLine;

                lineCount++;

            }

            in.close();
            br.close();
            fstream.close();

        }
        catch (Exception e)
        {
            System.err.println(IDENT + " Error: " + e.getMessage());
        }

        //System.out.println("GetIdentification: " + PASSWORD + " " + COOKIE);

    }

    // Write Personal data such as Password and Cookie
    public boolean  WritePersonalData(String Passwd,String Cookie)
    {

        boolean success = false;
        PrintWriter pout = null;

        try
        {

            if ((Passwd != null) && !(Passwd.equals("")))
            {

                pout = new PrintWriter(new FileWriter(filename));
                pout.println("PASSWORD");
                pout.println(Passwd); //(PASSWORD);

            }

            if ((Cookie != null) && !(Cookie.equals("")))
            {

                pout.println("COOKIE");
                pout.flush();
                pout.println(Cookie);
                pout.flush();

            }

            pout.close();
            success = true;

        }
        catch (IOException e)
        {
            pout.close();
            return success;
        }
        catch (NumberFormatException n)
        {

        }

        return success;

    }

    public void acceptTransactionRequest()
    {

        //reset this to false after TRANSFER_RESPONSE sent
        acceptingTransactionRequests = true;
        System.out.println(IDENT + ": Ready to accept a transfer request...\n");

    }

    //overridden in activeclient
    public void initiateTransactionRequest(String filename, String sender, String recipient, String amount)
    {

    }

    public boolean checkCertificate(BigInteger cert, BigInteger publicKeyV, BigInteger publicKeyN)
    {

        BigInteger chk = cert.modPow(new BigInteger("65537"), monitorNInt);

        byte[] result = chk.toByteArray();

        try
        {

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.reset();

            md.update(publicKeyV.toByteArray());
            md.update(publicKeyN.toByteArray());

            byte[] result2 = md.digest();

            for(int lcv = 0; lcv < result2.length; lcv++)
            {

                //weird... sometimes result[0] is zero, in which case it should be ignored
                if(result[0] == 0)
                {

                    if(result[lcv+1] != result2[lcv])
                        return false;

                }
                else
                {

                    if(result[lcv] != result2[lcv])
                        return false;

                }

            }

        }
        catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace(System.out);
            return false;
        }

        return true;

    }

    public void attackVictim()
    {

        try
        {

            boolean attacking = true;
            String attackInput = "";
            String response = "";

            String connectHost = System.console().readLine("What is the hostname? ");
            String connectPort = System.console().readLine("What is the port? ");

            System.out.print(IDENT + " (Client): Connecting to victim at " + connectHost + ":" + connectPort + "...");
            Socket toVictim = new Socket(connectHost, Integer.parseInt(connectPort));
            System.out.println(" completed.\n");

            PrintWriter victimOut = new PrintWriter(toVictim.getOutputStream(), true);
            BufferedReader victimIn = new BufferedReader(new InputStreamReader(toVictim.getInputStream()));

            while(attacking)
            {

                attackInput = System.console().readLine("Ready for attack command: ");

                if(attackInput.equals("STOP_ATTACK"))
                {

                    attacking = false;

                }
                else
                {

                    //send input to victim
                    SendIt(attackInput + "\n", victimOut);

                    //get victim's response
                    response = victimIn.readLine();
                    System.out.println("Attack response: " + response + "\n");

                }

            }

            toVictim.close();
            victimOut.close();
            victimIn.close();

        }
        catch(Exception e)
        {

            e.printStackTrace();

        }

    }

}
