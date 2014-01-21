import java.awt.Color;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;
import javax.swing.JTextArea;

public class ActiveClient extends MessageParser implements Runnable
{
    GameBoard gb;
    JTextArea log;
    public static String MonitorName;
    Thread runner;
    Socket toMonitor = null;
    public static int MONITOR_PORT;
    public static int LOCAL_PORT;
    public int SleepMode;
    int DELAY = 90000;  //Interval after which a new Active Client is started 
    long prevTime,present;
    public boolean connected = false;
    public boolean running = true;
    String guiPassword;

    public ActiveClient()
    {
        super("[no-name]", "[no-password]");
        logger = new Util(null);
        parentSub = DbgSub.ACTIVE_CLIENT;
        MonitorName="";
        toMonitor = null;
        MONITOR_PORT=0;
        LOCAL_PORT=0;
    }

    public ActiveClient(GameBoard gb, String mname, int p, int lp, int sm,
                        String name, String password)
    {
        super(name, password);
        this.gb = gb;
        this.log = gb.clientLog;
        logger = new Util(log);
        parentSub = DbgSub.ACTIVE_CLIENT;
        guiPassword = password;
        try
        {
            SleepMode = sm;
            MonitorName = mname; 
            MONITOR_PORT = p; 
            LOCAL_PORT = lp;
        }
        catch ( NullPointerException n )
        {
            logger.Print(DbgSub.ACTIVE_CLIENT, "[Constructor] TIMEOUT Error: "+n);
            if ( gb != null )
            {
                gb.appGlobalMessage.setText( "[Constructor] TIMEOUT Error: "+n );
            }
        }
    }

    public void start()
    {
        if ( runner == null )
        {
            runner = new Thread(this);
            runner.start();
        }
    }

    public void stop()
    {
        running = false;
    }

    public void ProcessResult()
    {
        //TODO: case results processing for each type of command we can issue
        if ( comment != null && !comment.equals("none") ) {
            StringTokenizer st = new StringTokenizer(comment);
            if ( st.hasMoreTokens() ) {
                if (st.nextToken().equalsIgnoreCase("Timeout") ) {
                    debug.Print(DbgSub.ACTIVE_CLIENT, "Received a timeout, stopping...");
                    running = false;
                    return;
                }
            }
        }
        if ( result == null || result.equals("none") ) {
            return;
        }
        
        BetterStringTokenizer st = new BetterStringTokenizer(result);
        if ( st.hasMoreTokens() ) {
            String resultCommand = st.nextToken();
            if ( resultCommand.equalsIgnoreCase(lastCommandSent) ) {
                if ( lastCommandSent.equalsIgnoreCase("QUIT") || lastCommandSent.equalsIgnoreCase("SIGN_OFF") ) {
                    running = false;
                } else if ( lastCommandSent.equalsIgnoreCase("PASSWORD") || lastCommandSent.equalsIgnoreCase("CHANGE_PASSWORD") ) {
                    GlobalData.SetCookie(st.nextToken());
                    GlobalData.SetPassword(gb.passwordBlank.getText());
                    storage.WritePersonalData(GlobalData.GetPassword(), GlobalData.GetCookie());
                }
            } else if (resultCommand.equals("CERTIFICATE")) {
                try {
                    if ( encrypting ){
                    String ident = st.nextToken();
                    String cert = st.nextToken();
                    MessageDigest mdsha = MessageDigest.getInstance("SHA-1");
                    mdsha.update(myKey.publicKey().getExponent().toByteArray());
                    mdsha.update(myKey.publicKey().getModulus().toByteArray());

                    BigInteger m = new BigInteger(1, mdsha.digest());
                    BigInteger p = new BigInteger(cert, 32);
                    BigInteger certNumber = monCert.getPublicKey().encrypt(p);
                    if (m.compareTo(certNumber) != 0) {
                        // we need to explode here.  the monitor is not authentic.
                        System.out.println("danger danger monitor not authentic");
                    }
                    }
                } catch (NoSuchAlgorithmException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void run()
    {
        try
        {
            logger.Print(DbgSub.ACTIVE_CLIENT, "trying monitor: "+MonitorName+
                             " port: "+MONITOR_PORT+"...");
            toMonitor = new Socket(MonitorName, MONITOR_PORT);
            logger.Print(DbgSub.ACTIVE_CLIENT, "completed.");
            plainOut = new PrintWriter(toMonitor.getOutputStream(), true);
            plainIn = new BufferedReader(new InputStreamReader(toMonitor.getInputStream()));
            
            out = plainOut;
            in = plainIn;

            HOSTNAME = toMonitor.getLocalAddress().getHostName();
            CType = 0;   //Indicates Client 
            HOST_PORT = LOCAL_PORT;

            //Get the Initial Monitor comment message
            GetMonitorMessage();
            if ( GlobalData.GetPassword() == null ) {
                GlobalData.SetPassword(guiPassword);
            }
            //TODO: (done?) Loop here and wait for commands from GUI
            while ( running ) {
                //TODO: (done?) run commands here
                Execute(gb.GetCommand(gb.ch.CreateCommand()));
                GetMonitorMessage();
                if ( require != null && !require.equals("none") ) {
                    debug.Print(DbgSub.ACTIVE_CLIENT, "REQUIRE: " + require);
                }
                ProcessResult();
                //Update the GUI with password, hostport, etc.
                if ( GlobalData.GetPassword() != null ) {
                    gb.passwordBlank.setText(GlobalData.GetPassword());
                    gb.loginPasswordArg.setText(GlobalData.GetPassword());
                }
            }

            System.out.println("Client shutting down");
            //Disconnect client and update game board
            connected = false;
            gb.clientConnectButton.setBackground(Color.red);
            toMonitor.close(); 
            out.close(); 
            in.close();
            gb.ac = null;
//            try
//            {
//                Thread.sleep(DELAY);
//            }
//            catch ( Exception e )
//            {
//            	e.printStackTrace();
//                if ( gb != null )
//                {
//                    gb.appGlobalMessage.setText( e.toString() );
//                }
//            }

        }
        catch ( UnknownHostException e )
        {
            e.printStackTrace();
            if ( gb != null )
            {
                gb.appGlobalMessage.setText( e.toString() );
            }
        }
        catch ( IOException e )
        {
            try
            {
                toMonitor.close();  
                //toMonitor = new Socket(MonitorName,MONITOR_PORT);
            }
            catch ( IOException ioe )
            {
            	e.printStackTrace();
                if ( gb != null )
                {
                    gb.appGlobalMessage.setText( ioe.toString() );
                }
            }
            catch ( NullPointerException n )
            {
                try
                {
                    if ( toMonitor != null )
                    {
                        toMonitor.close();
                    }
                    //toMonitor = new Socket(MonitorName,MONITOR_PORT);
                }
                catch ( IOException ioe )
                {
                	e.printStackTrace();
                    if ( gb != null )
                    {
                        gb.appGlobalMessage.setText( ioe.toString() );
                    }
                }
            }
        }
    }
}

