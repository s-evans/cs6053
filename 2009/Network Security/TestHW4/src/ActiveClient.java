import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

public class ActiveClient extends MessageParserClient
  implements Runnable
{
  public static String MonitorName;
  Thread runner;
  Socket toMonitor = null;
  public static int MONITOR_PORT;
  public static int LOCAL_PORT;
  public int SleepMode;
  int DELAY = 600000;
  long prevTime;
  long present;

  public ActiveClient()
  {
    super("[no-name]", "[no-password]");
    MonitorName = "";
    this.toMonitor = null;
    MONITOR_PORT = 0;
    LOCAL_PORT = 0;
  }

  public ActiveClient(String paramString1, int paramInt1, int paramInt2, int paramInt3, String paramString2, String paramString3)
  {
    super(paramString2, paramString3);
    try {
      this.SleepMode = paramInt3;
      MonitorName = paramString1; MONITOR_PORT = paramInt1; LOCAL_PORT = paramInt2;
      System.out.println("Active Client [Constructor]: " + MonitorName + " (" + MONITOR_PORT + ":" + LOCAL_PORT + ")");
    }
    catch (NullPointerException localNullPointerException) {
      System.out.println("Active Client [Constructor]: TIMEOUT Error: " + localNullPointerException);
    }
  }

  public void start() {
    if (this.runner == null) {
      this.runner = new Thread(this);
      this.runner.start();
    }
  }

  String getCommand(BufferedReader paramBufferedReader) {
    String str = null;
    try {
      System.out.print("command> ");
      str = paramBufferedReader.readLine(); } catch (Exception localException) {
    }
    return str;
  }

  public void run() {
    System.out.println("Active Client [run]: up");
    if (Thread.currentThread() == this.runner)
      try {
        this.toMonitor = new Socket(MonitorName, MONITOR_PORT);
        System.out.println("Active Client [run]: socket to " + MonitorName + ":" + MONITOR_PORT + " established");

        this.out = new PrintStream(this.toMonitor.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.toMonitor.getInputStream()));

        HOSTNAME = this.toMonitor.getLocalAddress().getHostName();
        this.CType = 0;
        this.HOST_PORT = LOCAL_PORT;
        System.out.println("Active Client [run]: connect " + HOSTNAME + ":" + this.HOST_PORT);

        if (Login()) {
          System.out.println("Active Client [run]: success - Logged In!");
          this.present = System.currentTimeMillis();
          if (this.present - this.prevTime > 2L * this.DELAY) {
            this.prevTime = this.present;
            System.out.println("***************************");
            BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while (true)
            {
              String str1;
              int i;
              String str4;
              String str5;
              while ((str1 = getCommand(localBufferedReader)) == null);
              StringTokenizer localStringTokenizer = new StringTokenizer(str1, " ");
              String str2 = localStringTokenizer.nextToken().toUpperCase();
              if (str2.equals("SUBSET_A")) {
                i = str1.indexOf(" ", 0);
                str4 = str1.substring(i).trim();
                if (Execute("SUBSET_A", str4))
                  str5 = GetMonitorMessage();
              }
              else if (str2.equals("SUBSET_K")) {
                i = str1.indexOf(" ", 0);
                str4 = str1.substring(i).trim();
                if (Execute("SUBSET_K", str4))
                  str5 = GetMonitorMessage();
              }
              else if (str2.equals("SUBSET_J")) {
                i = str1.indexOf(" ", 0);
                str4 = str1.substring(i).trim();
                if (Execute("SUBSET_J", str4))
                  str5 = GetMonitorMessage();
              }
              else if (str2.equals("AUTHORIZE_SET")) {
                i = str1.indexOf(" ", 0);
                str4 = str1.substring(i).trim();
                if (Execute("AUTHORIZE_SET", str4))
                  str5 = GetMonitorMessage();
              }
              else if (str2.equals("MAKE_CERTIFICATE")) {
                i = str1.indexOf(" ", 0);
                str4 = str1.substring(i).trim();
                if (Execute("MAKE_CERTIFICATE", str4))
                  str5 = GetMonitorMessage();
              }
              else if (str2.equals("GET_CERTIFICATE")) {
                i = str1.indexOf(" ", 0);
                str4 = str1.substring(i).trim();
                if (Execute("GET_CERTIFICATE", str4))
                  str5 = GetMonitorMessage();
              }
              else if (str2.equals("PARTICIPANT_HOST_PORT")) {
                i = str1.indexOf(" ", 0);
                str4 = str1.substring(i).trim();
                if (Execute("PARTICIPANT_HOST_PORT", str4))
                  str5 = GetMonitorMessage();
              }
              else if (str2.equals("PUBLIC_KEY")) {
                i = str1.indexOf(" ", 0);
                str4 = str1.substring(i).trim();
                if (Execute("PUBLIC_KEY", str4))
                  str5 = GetMonitorMessage();
              }
              else if (str2.equals("ROUNDS")) {
                i = str1.indexOf(" ", 0);
                str4 = str1.substring(i).trim();
                if (Execute("ROUNDS", str4))
                  str5 = GetMonitorMessage();
              }
              else if (str2.equals("TRANSFER_REQUEST")) {
                i = str1.indexOf(" ", 0);
                str4 = str1.substring(i).trim();
                if (Execute("TRANSFER_REQUEST", str4))
                  str5 = GetMonitorMessage();
              }
              else if (str2.equals("TRANSFER_RESPONSE")) {
                i = str1.indexOf(" ", 0);
                str4 = str1.substring(i).trim();
                if (Execute("TRANSFER_RESPONSE", str4))
                  str5 = GetMonitorMessage();
              }
              else if (str2.equals("CHANGE_PASSWORD")) {
                ChangePassword();
              } else if (str2.equals("GET_MONITOR_KEY")) {
                GetMonitorPublicKey("GET_MONITOR_KEY");
              } else if (str2.equals("HELP")) {
                System.out.print("\tPARTICIPANT_STATUS\n\tGET_GAME_IDENTS\n\tRANDOM_PARTICIPANT_HOST_PORT\n\tGET_MONITOR_KEY\n\tQUIT\n\tSIGN_OFF\n\tTRANSFER_RESPONSE [accept | decline]\n\tTRANSFER_REQUEST <recipient> <points> FROM <sender>\n\tCHANGE_PASSWORD <old> <new>\n\tALIVE <cookie>\n\tPARTICIPANT_HOST_PORT <player>\n\tPUBLIC_KEY <ZeroKnow-v-value> <ZeroKnow-n-value>\n\tROUNDS <number>\n\tAUTHORIZE_SET <r1> ... <rn>\n\tSUBSET_A <a1> ... <ao>\n\tSUBSET_K <a1> ... <ao>\n\tSUBSET_J <a1> ... <ap>\n\tGET_CERTIFICATE <player>\n\tMAKE_CERTIFICATE <ZK-v-value> <ZK-n-value>\n");
              }
              else if (Execute(str2)) {
                String str3 = GetMonitorMessage();
              }
            }
          }
          System.out.println("Active Client [run]: NOT YET TIME TO EXECUTE ANY OTHER COMMAND!");
        }
        else
        {
          System.out.println("Active Client [run]: verification status = " + IsVerified);

          System.out.println("Active Client [run]: Login failed!");
          if (IsVerified == 0) {
            System.out.println("Active Client [run]: verification by client failed! QUITTING");

            System.exit(1);
          }
        }
        System.out.println("Active Client [run]: disconnecting");
        this.toMonitor.close(); this.out.close(); this.in.close();
        Thread.sleep(this.DELAY);
      }
      catch (UnknownHostException localUnknownHostException) {
        System.err.println("Active Client [run]: unknown host: " + localUnknownHostException);
      } catch (IOException localIOException1) {
        System.err.println("Active Client [run]: failed I/O for the connection to: " + localIOException1);
        try
        {
          this.toMonitor.close();
        }
        catch (IOException localIOException2) {
          System.out.println(localIOException2);
        }
      } catch (NullPointerException localNullPointerException) {
        System.out.println("Active Client [run]: TIMEOUT" + localNullPointerException);
        try {
          System.out.println("Active Client [run]: starting new session with client");

          this.toMonitor.close();
        } catch (IOException localIOException3) {
        }
      } catch (InterruptedException localInterruptedException) {
      } catch (Exception localException) {
        System.out.println("Reset due to " + localException.toString());
      }
  }
}