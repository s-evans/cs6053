import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.StringTokenizer;

class ConnectionHandler extends MessageParserServer
  implements Runnable
{
  Socket incoming;
  int counter;
  Thread runner;

  public ConnectionHandler(Socket paramSocket, int paramInt, String paramString1, String paramString2)
  {
    super(paramString1, paramString2);
    this.incoming = paramSocket;
    this.counter = paramInt;
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
    try {
      BufferedReader localBufferedReader;
      this.in = new BufferedReader(new InputStreamReader(this.incoming.getInputStream()));

      this.sout = new PrintStream(this.incoming.getOutputStream(), true);

      System.out.println("ConnectionHandler [run]: connecting");
      int i = 0;
      this.HOST_PORT = PassiveServer.LOCAL_PORT;
      this.CType = 1;
      if (Login()) {
        System.out.println("Passive Server [run]: success - Logged In!");
        System.out.println("***************************");
        localBufferedReader = new BufferedReader(new InputStreamReader(System.in));
      }

      try
      {
        String str1;
        int j;
        String str4;
        String str5;
        localBufferedReader = new BufferedReader(new InputStreamReader(System.in));
        while ((str1 = getCommand(localBufferedReader)) == null);
        StringTokenizer localStringTokenizer = new StringTokenizer(str1, " ");
        String str2 = localStringTokenizer.nextToken().toUpperCase();
        if (str2.equals("SUBSET_A")) {
          j = str1.indexOf(" ", 0);
          str4 = str1.substring(j).trim();
          if (Execute("SUBSET_A", str4))
            str5 = GetMonitorMessage();
        }
        else if (str2.equals("SUBSET_K")) {
          j = str1.indexOf(" ", 0);
          str4 = str1.substring(j).trim();
          if (Execute("SUBSET_K", str4))
            str5 = GetMonitorMessage();
        }
        else if (str2.equals("SUBSET_J")) {
          j = str1.indexOf(" ", 0);
          str4 = str1.substring(j).trim();
          if (Execute("SUBSET_J", str4))
            str5 = GetMonitorMessage();
        }
        else if (str2.equals("AUTHORIZE_SET")) {
          j = str1.indexOf(" ", 0);
          str4 = str1.substring(j).trim();
          if (Execute("AUTHORIZE_SET", str4))
            str5 = GetMonitorMessage();
        }
        else if (str2.equals("MAKE_CERTIFICATE")) {
          j = str1.indexOf(" ", 0);
          str4 = str1.substring(j).trim();
          if (Execute("MAKE_CERTIFICATE", str4))
            str5 = GetMonitorMessage();
        }
        else if (str2.equals("GET_CERTIFICATE")) {
          j = str1.indexOf(" ", 0);
          str4 = str1.substring(j).trim();
          if (Execute("GET_CERTIFICATE", str4))
            str5 = GetMonitorMessage();
        }
        else if (str2.equals("PARTICIPANT_HOST_PORT")) {
          j = str1.indexOf(" ", 0);
          str4 = str1.substring(j).trim();
          if (Execute("PARTICIPANT_HOST_PORT", str4))
            str5 = GetMonitorMessage();
        }
        else if (str2.equals("PUBLIC_KEY")) {
          j = str1.indexOf(" ", 0);
          str4 = str1.substring(j).trim();
          if (Execute("PUBLIC_KEY", str4))
            str5 = GetMonitorMessage();
        }
        else if (str2.equals("ROUNDS")) {
          j = str1.indexOf(" ", 0);
          str4 = str1.substring(j).trim();
          if (Execute("ROUNDS", str4))
            str5 = GetMonitorMessage();
        }
        else if (str2.equals("TRANSFER_REQUEST")) {
          j = str1.indexOf(" ", 0);
          str4 = str1.substring(j).trim();
          if (Execute("TRANSFER_REQUEST", str4))
            str5 = GetMonitorMessage();
        }
        else if (str2.equals("TRANSFER_RESPONSE")) {
          j = str1.indexOf(" ", 0);
          str4 = str1.substring(j).trim();
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
      } catch (Exception localException) {
        //while (true) { 
            System.out.println("Null input string");
        //}

        System.out.println("ConnectionHandler [run]: Login failed!");
        System.out.println("ConnectionHandler [run]: verif status=" + IsVerified);

        if (IsVerified != 1) {
          System.out.println("ConnectionHandler [run]: failed monitor verification");

          this.incoming.close();
        }

        this.incoming.close(); }
    } catch (IOException localIOException) {
      System.out.println(localIOException);
    } catch (NullPointerException localNullPointerException) {
      System.out.println(localNullPointerException);
    }
  }

  public void start() {
    if (this.runner == null) {
      this.runner = new Thread(this);
      this.runner.start();
    }
  }
}