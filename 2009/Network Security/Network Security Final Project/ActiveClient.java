package homework4;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.awt.*;

class ActiveClient extends MessageParser implements Runnable
{

    public static String MonitorName;
    Thread runner;
    Socket toMonitor = null;
    public static int MONITOR_PORT;
    public static int LOCAL_PORT;
    public int SleepMode;
    int DELAY = 30000;  //Interval after which a new Active Client is started
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
                       System.out.println("Exiting due to IsVerified");
                       System.exit(1);
                   }
                }

                System.out.println(IDENT + " (Client): Login() successful.\n");
                System.out.println("***************************\n");

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

                        success = true;
                        IsVerified = 1;

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

}
