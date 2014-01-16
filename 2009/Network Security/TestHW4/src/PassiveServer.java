import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PassiveServer
  implements Runnable
{
  ServerSocket s = null;
  public static int MONITOR_PORT;
  public static int LOCAL_PORT;
  Thread runner;
  String IDENT;
  String PASSWORD;

  public PassiveServer(int paramInt1, int paramInt2, String paramString1, String paramString2)
  {
    this.IDENT = paramString1;
    this.PASSWORD = paramString2;
    try {
      this.s = new ServerSocket(paramInt1);
      MONITOR_PORT = paramInt1;
      LOCAL_PORT = paramInt2;
      int i = 1;
      System.out.println("Passive Server [Constructor]: listening monitor port:" + MONITOR_PORT + " local port:" + LOCAL_PORT);
    }
    catch (IOException localIOException) {
      System.out.println("Passive Server [Constructor]: can't listen: " + localIOException);
    }
  }

  public void start() {
    if (this.runner == null) {
      this.runner = new Thread(this);
      this.runner.start();
    }
  }

  public void run() {
    System.out.println("Passive Server [run]: started");
    try {
      int i = 1;

      Socket localSocket = this.s.accept();
      System.out.println("Passive Server [run]: New connection handler  started");

      new ConnectionHandler(localSocket, i, this.IDENT, this.PASSWORD).start();
      ++i;
    }
    catch (Exception localException) {
      System.out.println("Passive Server [run]: Error in Server: " + localException);
    }
  }
}