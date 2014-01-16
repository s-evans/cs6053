import java.io.PrintStream;

class StartServer
{
  public static String MONITOR_NAME = "helios.ececs.uc.edu";
  public static int MONITOR_PORT = 8170;
  public static int HOST_PORT = 20000;
  public static int MAX = 5;
  PassiveServer s;

  public StartServer(String paramString1, String paramString2)
  {
    System.out.println("Passive Server started:\n\t Monitor: " + MONITOR_NAME + " monitor port: " + MONITOR_PORT + " host port: " + HOST_PORT);

    this.s = new PassiveServer(MONITOR_PORT, HOST_PORT, paramString1, paramString2);
  }

  public static void main(String[] paramArrayOfString) {
    if (paramArrayOfString.length != 4) {
      System.out.println("Usage: java StartServer monitor-host monitor-port host-port ident");
    }
    else {
      MONITOR_NAME = new String(paramArrayOfString[0]);
      MONITOR_PORT = Integer.parseInt(paramArrayOfString[1]);
      HOST_PORT = Integer.parseInt(paramArrayOfString[2]);
      StartServer localStartServer = new StartServer(paramArrayOfString[3], "-----");
      localStartServer.s.start();
    }
  }
}