import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.*;
import java.sql.Timestamp;

class Homework
{

    public static String MONITOR_NAME = "helios.ececs.uc.edu";
    public static int MONITOR_PORT = 8160;
    public static int HOST_PORT = 20000 + (int)(Math.random() * 1000);
    public static int MAX = 5;

    String logfile = null;
    FileWriter logfileFile = null;
    PrintWriter logfileWriter = null;

    ActiveClient ac;
    Server s;

    public Homework(String name, String password)
    {

        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String safeTS = ts.toString().replace(":", "-");
        System.out.println("TIMESTAMP = " + safeTS);
        logfile = name + " " + safeTS + ".log";
        //logfile = name + ".log";

        try
        {
            logfileWriter = new PrintWriter(new FileWriter(logfile));
        }
        catch(IOException e)
        {
            logfileWriter.close();
            System.out.println("ERROR: Log file could not be opened: " + e.getLocalizedMessage());
        }

        System.out.println("Project Begin:\tMonitor: " + MONITOR_NAME + " random port: " + HOST_PORT + " monitor port: " + MONITOR_PORT + "\n");
        ac = new ActiveClient(MONITOR_NAME, MONITOR_PORT, HOST_PORT, 0, name, password, logfileWriter);
        s = new Server(HOST_PORT, HOST_PORT, name, password, logfileWriter);

    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {


        if (args.length != 3)
        {
            System.out.println("Usage: java Homework monitor monitor-port ident");
        }
        else
        {

            MONITOR_NAME = new String(args[0]);
            MONITOR_PORT = Integer.parseInt(args[1]);
            Homework hw = new Homework(args[2], "mynewpassword");
            hw.ac.start(); //Start the Active Client
            hw.s.start();  //Start the Server

        }


/*
            MONITOR_NAME = new String("helios.ececs.uc.edu");
            MONITOR_PORT = Integer.parseInt("8160");
            Homework hwInitiator = new Homework("kmurph01Initiator", "asdf;lkj");
            hwInitiator.ac.start(); //Start the Active Client
            hwInitiator.s.start();  //Start the Server
*/

    }

}
