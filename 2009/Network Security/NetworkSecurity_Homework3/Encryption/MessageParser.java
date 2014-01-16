package homework2;

import java.util.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.math.*;

public class MessageParser
{

    //Monitor Handling Declarations
    int COMMAND_LIMIT = 25;
    public  int CType;
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

    public MessageParser()
    {

        filename = "passwd.dat";
        GetIdentification(); // Gets Password and Cookie from 'passwd.dat' file

    }

    public MessageParser(String ident, String password)
    {

        filename = ident + ".dat";
        PASSWORD = password;
        IDENT = ident;
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

            String temp;

            if(isEncrypted)
                temp = karn.decrypt(in.readLine());
            else
                temp = in.readLine();

            first = temp; // 1st

            if(first.substring(0, 13).equals("RESULT: IDENT"))
            {

                //get encryption key from monitor response
                MonitorKey = first.substring(14);
                karn = new Karn(DHExchange.getSecret(MonitorKey, out));
                isEncrypted = true;

            }

            sMesg = temp;

            //if(isEncrypted)
               //decrypt = karn.decrypt(temp);
            //else
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
            
        }
        catch (IOException e)
        {

            System.out.println("MessageParser [getMonitorMessage]: error in GetMonitorMessage:\n\t" + e + this);
            sMesg="";

        }
        catch (NullPointerException n)
        {

            sMesg = "";

        }
        catch (NumberFormatException o)
        {

            System.out.println("MessageParser [getMonitorMessage]: number format error:\n\t" + o + this);
            sMesg="";

        }
        catch (NoSuchElementException ne)
        {

            System.out.println("MessageParser [getMonitorMessage]: no such element exception occurred:\n\t" + this);

        }
        catch (ArrayIndexOutOfBoundsException ae)
        {

            System.out.println("MessageParser [getMonitorMessage]: AIOB EXCEPTION!\n\t" + this);
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

            System.out.println("MessageParser [getNextCommand]: returning:\n\t" + temp);

            return temp;  //returns what the monitor wants

        }
        catch (NoSuchElementException e)
        {

            return null;

        }

    }

    public boolean Login()
    {

        boolean success = false;
        String msg = "";

        try
        {

            if(CType == 1)
            {

                //server
                while((msg = GetNextCommand(GetMonitorMessage(), "")) != null)
                {

                    if(msg.equals("IDENT"))
                    {
                        Execute("IDENT");
                    }
                    else if(msg.equals("ALIVE"))
                    {
                        Execute("ALIVE");
                    }
                    else if(msg.equals("QUIT"))
                    {
                        Execute("QUIT");
                        success = true;
                    }

                }

            }
            else
            {

                //client
                while((msg = GetNextCommand(GetMonitorMessage(), "")) != null)
                {

                    if(msg.equals("IDENT"))
                    {
                        Execute("IDENT");
                        System.out.println("Waiting for IDENT response...\n");
                    }
                    else if(msg.equals("PASSWORD"))
                    {

                        Execute("PASSWORD");

                        //parse cookie from monitor response
                        msg = GetMonitorMessage();

                        try
                        {

                            String sDefault = "PASSWORD";

                            t = new StringTokenizer(msg, " ");

                            //Search for the REQUIRE Command
                            String temp = t.nextToken();

                            while (!(temp.trim().equals(sDefault.trim())))
                                temp = t.nextToken();

                            temp = t.nextToken();

                            //System.out.println("Cookie:\n\t" + temp);

                            COOKIE = temp;

                            WritePersonalData(PASSWORD, COOKIE);

                            if(GetNextCommand(msg, "").equals("HOST_PORT"))
                            {
                                if(Execute("HOST_PORT"))
                                    success = true;

                            }
                        }
                        catch (NoSuchElementException e)
                        {

                            //return null;

                        }

                    }
                    else if(msg.equals("ALIVE"))
                    {
                        Execute("ALIVE");
                    }
                    else if(msg.equals("HOST_PORT"))
                    {
                        if(Execute("HOST_PORT"))
                            success = true;
                    }

                }

            }

        }
        catch (NullPointerException n)
        {

            System.out.println("MessageParser [Login]: null pointer error at login:\n\t" + n);
            success = false;

        }

        System.out.println("Success Value Login = " + success);
        return success;

    }

    //Handle Directives and Execute appropriate commands with one argument
    public boolean Execute (String sentmessage, String arg)
    {

        boolean success = false;

        try
        {

            if (sentmessage.trim().equals("PARTICIPANT_HOST_PORT"))
            {

                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(arg);
                SendIt(sentmessage);
                success = true;

            }

        }
        catch (IOException e)
        {

            System.out.println("IOError:\n\t" + e);
            success = false;

        }
        catch (NullPointerException n)
        {

            System.out.println("Null Error has occured");
            success=false;

        }

        return success;

    }

    //Handle Directives and Execute appropriate commands
    public boolean Execute (String sentmessage)
    {

        boolean success = false;

        try
        {

            if (sentmessage.trim().equals("IDENT"))
            {

                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(IDENT);
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(pubKey.toString(32));
                SendIt (sentmessage);
                success = true;

            }
            else if (sentmessage.trim().equals("PASSWORD"))
            {

                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(PASSWORD);
                sentmessage = karn.encrypt(sentmessage);
                SendIt (sentmessage.trim());
                success = true;

            }
            else if (sentmessage.trim().equals("HOST_PORT"))
            {

                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(HOSTNAME);//hostname
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(String.valueOf(HOST_PORT));
                sentmessage = karn.encrypt(sentmessage);
                SendIt (sentmessage);
                success = true;

            }
            else if (sentmessage.trim().equals("ALIVE"))
            {

                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(COOKIE);
                sentmessage = karn.encrypt(sentmessage);
                SendIt (sentmessage);
                success = true;

            }
            else if (sentmessage.trim().equals("QUIT"))
            {

                sentmessage = karn.encrypt(sentmessage);
                SendIt(sentmessage);
                success = true;

            }
            else if (sentmessage.trim().equals("SIGN_OFF"))
            {

                sentmessage = karn.encrypt(sentmessage);
                SendIt(sentmessage);
                success = true;

            }
            else if (sentmessage.trim().equals("GET_GAME_IDENTS"))
            {

                sentmessage = karn.encrypt(sentmessage);
                SendIt(sentmessage);
                success = true;

            }
            else if (sentmessage.trim().equals("PARTICIPANT_STATUS"))
            {

                sentmessage = karn.encrypt(sentmessage);
                SendIt(sentmessage);
                success = true;

            }
            else if (sentmessage.trim().equals("RANDOM_PARTICIPANT_HOST_PORT"))
            {

                sentmessage = karn.encrypt(sentmessage);
                SendIt(sentmessage);
                success = true;

            }

        }
        catch (IOException e)
        {

            System.out.println("IOError:\n\t"+e);
            success = false;

        }
        catch (NullPointerException n)
        {

            System.out.println("Null Error has occured");
            success=false;

        }

        return success;

    }

    public void SendIt (String message) throws IOException
    {

        try
        {

            System.out.println("MessageParser [SendIt]: sent:\n\t"+message);
            out.println(message);
            if (out.checkError() == true) throw (new IOException());
            out.flush();
            if(out.checkError() == true) throw (new IOException());

        }
        catch (IOException e) {} //Bubble the Exception upwards

    }

    //In future send parameters here so that diff commands are executed
    public boolean ProcessExtraMessages()
    {

        boolean success = false;

        System.out.println("MessageParser [ExtraCommand]: received:\n\t" + mesg.trim());

        if ((mesg.trim().equals("")) || (mesg.trim().equals(null)))
        {

            mesg = GetMonitorMessage();
            System.out.println("MessageParser [ExtraCommand]: received (2):\n\t" + mesg.trim());

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
                    SendIt("SYNTHESIZE WEAPONS");
                    mesg = GetMonitorMessage();
                    SendIt("SYNTHESIZE COMPUTERS");
                    mesg = GetMonitorMessage();
                    SendIt("SYNTHESIZE VEHICLES");
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

    public void HandleTradeResponse(String cmd) throws IOException
    {

    }

    public boolean IsTradePossible(String TradeMesg)
    {
        return false;
    }

    public int GetResource(String choice) throws IOException
    {
        return 0;
    }

    public void HandleWarResponse(String cmd) throws IOException
    {

    }

    public void DoTrade(String cmd)  throws IOException
    {

    }

    public void DoWar(String cmd)  throws IOException
    {

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
            System.err.println("Error: " + e.getMessage());
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

    //Check whether the Monitor is Authentic
    public boolean Verify(String passwd,String chksum)
    {
        return false;
    }

    public boolean IsMonitorAuthentic(String MonitorMesg)
    {
        return false;
    }

}
