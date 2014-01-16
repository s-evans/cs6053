import java.io.PrintStream;

class Homework
{
  public static String MONITOR_NAME = "helios.ececs.uc.edu";
  public static int MONITOR_PORT = 8170;
  public static int HOST_PORT = 20000 + (int)(Math.random() * 1000.0D);
  public static int MAX = 5;
  ActiveClient ac;
  PassiveServer ps;

  public Homework(String paramString1, String paramString2)
  {
    System.out.println("Project Begin:\n\t Monitor: " + MONITOR_NAME + " random host port: " + HOST_PORT + " monitor port: " + MONITOR_PORT);

    this.ac = new ActiveClient(MONITOR_NAME, MONITOR_PORT, HOST_PORT, 0, paramString1, paramString2);
    this.ps = new PassiveServer(MONITOR_PORT, HOST_PORT, paramString1, paramString2);
  }

  public static void main(String[] paramArrayOfString) {
    if (paramArrayOfString.length != 3) {
      System.out.println("Usage: java Homework monitor monitor-port ident");
    } else {
      MONITOR_NAME = new String(paramArrayOfString[0]);
      MONITOR_PORT = Integer.parseInt(paramArrayOfString[1]);
      Homework localHomework = new Homework(paramArrayOfString[2], "-----");
      localHomework.ac.start();
      localHomework.ps.start();
    }
  }
}