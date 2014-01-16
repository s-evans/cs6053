import java.io.PrintStream;

class StartClient
{
  public static String MONITOR_NAME = "helios.ececs.uc.edu";
  public static int MONITOR_PORT = 8170;
  public static int HOST_PORT = 20000;
  public static int MAX = 5;
  ActiveClient ac;

  public StartClient(String paramString1, String paramString2)
  {
    System.out.println("Client started:\n\tMonitor: " + MONITOR_NAME + " monitor port: " + MONITOR_PORT + " port: " + HOST_PORT);

    this.ac = new ActiveClient(MONITOR_NAME, MONITOR_PORT, HOST_PORT, 0, paramString1, paramString2);
  }

  public static void main(String[] paramArrayOfString) {
    if ((paramArrayOfString.length < 4) || (paramArrayOfString.length > 5)) {
      System.out.println("Usage: java StartClient monitor-host monitor-port host-port ident");
    } else {
      MONITOR_NAME = new String(paramArrayOfString[0]);
      MONITOR_PORT = Integer.parseInt(paramArrayOfString[1]);
      HOST_PORT = Integer.parseInt(paramArrayOfString[2]);
      StartClient localStartClient = new StartClient(paramArrayOfString[3], "-----");
      int i = 1;
      try { String str = paramArrayOfString[4]; } catch (Exception localException) { i = 0; }
      localStartClient.ac.prevent_encryption = i;
      localStartClient.ac.start();
    }
  }
}