import java.awt.Color;
import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

class ConnectionHandler extends MessageParser implements Runnable
{
    GameBoard gb;
    private Socket incoming;
    //private int counter;
    Thread runner;
    Integer RESPONSE_DELAY=10000; //10 seconds

    public ConnectionHandler (GameBoard gb, Socket i, int c, String name, String password)
    {
        super(name, password);
        this.gb = gb;
        logger = new Util(gb.serverLog);
        parentSub = DbgSub.CONNECTION_HANDLER;
        incoming = i;
        //counter = c;
    }

    public boolean Login()
    {
        GetMonitorMessage();

        ProcessUntilQuit();
        return ProcessResult();
    }

    public synchronized void ProcessUntilQuit()
    {
        while (!require.equals("none") ) {
            ProcessResult();
            if ( require.equalsIgnoreCase("ALIVE") ) {
                Execute(require + " " + GlobalData.GetCookie());
            } else if ( require.equalsIgnoreCase("WAR_DEFEND") ) {
                Color temp = gb.warDefendButton.getBackground();
                gb.warDefendButton.setBackground(Color.yellow);
                try {
                    wait(RESPONSE_DELAY);
                } catch(Exception e) {}
                if ( gb.warDefendWeaponsArg.getText() != null && gb.warDefendVehiclesArg.getText() != null ) {
                    Execute(require + " " + gb.warDefendWeaponsArg.getText() + " " + gb.warDefendVehiclesArg.getText());
                } else {
                    Execute(require + " 0 0");
                }
                gb.warDefendButton.setBackground(temp);
            } else if ( require.equalsIgnoreCase("TRADE_RESPONSE") ) {
                Color temp = gb.tradeResponseButton.getBackground();
                gb.tradeResponseButton.setBackground(Color.yellow);
                try {
                    wait(RESPONSE_DELAY);
                } catch (Exception e) {}
                Execute(require + " " + gb.tradeResponseBox.getSelectedItem().toString());
                gb.tradeResponseButton.setBackground(temp);
            } else if ( require.equalsIgnoreCase("WAR_TRUCE_RESPONSE") ) {
                Color temp = gb.warTruceResponseButton.getBackground();
                gb.warTruceResponseButton.setBackground(Color.yellow);
                try {
                    wait(RESPONSE_DELAY);
                } catch (Exception e) {}
                Execute(require + " " + gb.truceResponseBox.getSelectedItem().toString());
                gb.warTruceResponseButton.setBackground(temp);
            } else {
                Execute(require);
            }
            GetMonitorMessage();
        }
    }

    public boolean ProcessResult()
    {
        if ( result.equals("none") ) {
            return false;
        }
        StringTokenizer st = new StringTokenizer(result);
        if ( st.hasMoreTokens() ) {
            if ( st.nextToken().equalsIgnoreCase(lastCommandSent) ) {
                return true;
            }
        }
        return false;
    }

    public void run()
    {
        try
        {
            in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            out = new PrintWriter(incoming.getOutputStream(),true);
            plainIn = in;
            plainOut = out;
            HOST_PORT = Server.LOCAL_PORT;
            CType = 1;  //Indicates Server
            logger.Print(DbgSub.CONNECTION_HANDLER, "Starting login from Server..");
            if ( Login() )
            {
                logger.Print(DbgSub.CONNECTION_HANDLER, "[run]: success Logged In!");
            }
            else
            {
                logger.Print(DbgSub.CONNECTION_HANDLER, "Server could not log in.");
                if ( IsVerified != 1 )
                {
                }
            }
            incoming.close();
        }
        catch ( IOException e )
        {
        	e.printStackTrace();
            if ( gb != null )
            {
                gb.appGlobalMessage.setText( e.toString() );
            }
        }
        catch ( NullPointerException n )
        {
        	n.printStackTrace();
            if ( gb != null )
            {
                gb.appGlobalMessage.setText( n.toString() );
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
}
