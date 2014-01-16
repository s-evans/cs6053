import java.io.*;
import java.net.*;

public class Server implements Runnable
{
    GameBoard gb;
    Util logger;
    ServerSocket s = null;
    public static int MONITOR_PORT;
    public static int LOCAL_PORT; 
    Thread runner;
    String IDENT;
    String PASSWORD;
    public boolean connected = false;
    boolean mbServerRunning = false;

    public Server(GameBoard gb, int p, int lp, String name, String password)
    {
        this.gb = gb;
        logger = new Util(gb.serverLog);
        IDENT = name;
        PASSWORD = password;
        try
        {
            s = new ServerSocket(lp);
            MONITOR_PORT = p;
            LOCAL_PORT = lp;
            gb.hPortArg2.setText(Integer.toString(LOCAL_PORT));
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            if ( gb != null )
            {
                gb.appGlobalMessage.setText( e.toString() );
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

    public void run()
    {
        mbServerRunning = true;
        
        try
        {
            int i = 1;
            s.setSoTimeout( 2000 );  // server exit timeout
            
            while ( mbServerRunning == true )
            {
                try
                {
                    Socket incoming = s.accept();
                    new ConnectionHandler(gb, incoming,i,IDENT,PASSWORD).start();
                    //Spawn a new thread for each new connection
                    i++;
                }
                catch ( SocketTimeoutException e )
                {
                    // nothing here, just loop around.  this allows us to kill the server.
                }
            }
        }
        catch ( Exception e )
        {
            logger.Print(DbgSub.SERVER, "[run]: Error in Server: "  + e);
            e.printStackTrace();
            if ( gb != null )
            {
                gb.appGlobalMessage.setText( e.toString() );
            }
        }
        
        try
        {
            s.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        // in case we excepted earlier
        mbServerRunning = false;
        
        gb.appGlobalMessage.setText( "Server exited" );
    }
}

