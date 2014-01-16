
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

    public ConnectionHandler (Socket i, int c, String name, String password, PrintWriter logWriter)
    {
        super(name, password, logWriter);
        incoming = i;  counter = c;
        isEncrypted = false;
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
        String cmd = "";
        String lastCmd = "";
        String expectedCmd = "";
        String expectedCmd2 = "";

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

                }
                else
                {

                    System.out.println(IDENT + " (Server): Login failed. Expected " + expectedCmd + " or " + expectedCmd2 + " from monitor, got " + cmd + ".\n");

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

            System.out.println(IDENT + " (Server) [Login]: null pointer error at login:\n " + n);
            n.printStackTrace(System.out);

            fail = true;
            success = false;

        }


        return success;

    }

}
