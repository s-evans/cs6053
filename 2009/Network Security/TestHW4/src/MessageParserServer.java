import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class MessageParserServer
{
  int COMMAND_LIMIT = 25;
  public int CType;
  public static String HOSTNAME;
  PrintStream sout = null;
  BufferedReader in = null;
  String mesg;
  String sentmessage;
  StringTokenizer t;
  String IDENT = "Skipper";
  String PASSWORD = "franco";
  static String COOKIE = "bkuhn";
  String PPCHECKSUM = "";
  int HOST_PORT;
  public static int IsVerified;
  static SecureRandom sr = null;
  String RandomPassword;
  BufferedReader fIn = null;
  PrintWriter fOut = null;
  static String passwordFileName = "passwd.dat";
  String[] cmdArr = new String[this.COMMAND_LIMIT];
  static String MyKey;
  String MonitorKey;
  static boolean IsEncrypted;
  static boolean StartedEncryption;
  static boolean dheOn;
  String first;
  Karn cipher = null;
  static DHE dhe = null;
  static DHKey dhk = null;
  ObjectInputStream oin = null;
  ObjectOutputStream oout = null;

  public MessageParserServer(String paramString1, String paramString2)
  {
    this.IDENT = paramString1;
    passwordFileName = new String("passwd.dat." + paramString1);
    System.out.println("Message Parser Server [Constructor]: called by: " + this + " Name: " + this.IDENT + " Password: " + this.PASSWORD);

    GetIdentification();
    if (sr == null) {
      System.out.println("Message Parser Server [Constructor]: generating secure random number");

      sr = new SecureRandom();
    }
    String str = GeneratePassword(sr);
    if ((str.trim().equals("")) || (str.trim().equals(null)))
      this.RandomPassword = str.trim();
  }

  public String GetMonitorMessage()
  {
    System.out.println("Message Parser Server:");
    Object localObject1 = ""; Object localObject2 = "";
    try {
      String str = this.in.readLine();
      this.first = str;
      if ((IsEncrypted) && (StartedEncryption)) {
        if ((!(str.trim().equals(""))) && (str != null))
          localObject2 = this.cipher.decrypt(str.trim());
        else
          localObject2 = "";

        localObject1 = localObject2;
        System.out.println("\t" + ((String)localObject2));
      } else {
        localObject1 = str;
        System.out.println("\t" + str);
        localObject2 = str;
      }

      if ((StartedEncryption) && (!(IsEncrypted))) {
        this.MonitorKey = GetNextCommand(this.first, "IDENT");
        System.out.println("Message Parser Server [getMonitorMessage]: monitor key:\n\t" + this.MonitorKey);

        dhe.setExchangeKey(this.MonitorKey);
        BigInteger localBigInteger = dhe.getSharedKey();
        System.out.println("Message Parser Server [getMonitorMessage]: shared key:\n\t" + localBigInteger);

        this.cipher = new Karn(localBigInteger);
        IsEncrypted = true; }
      while (true) {
        while (true) {
          if (((String)localObject2).trim().equals("WAITING:")) break;
          str = this.in.readLine();
          localObject1 = ((String)localObject1).concat(" ");
          if ((!(IsEncrypted)) || (!(StartedEncryption))) break;
          if ((!(str.trim().equals(""))) && (str != null))
            localObject2 = this.cipher.decrypt(str.trim());
          else
            localObject2 = "";
          System.out.println("\t" + ((String)localObject2));
          localObject1 = ((String)localObject1).concat((String)localObject2);
        }
        localObject2 = str;
        System.out.println("\t" + str);
        label391: localObject1 = ((String)localObject1).concat((String)localObject2);
      }

    }
    catch (IOException localIOException)
    {
      System.out.println("Message Parser Server [getMonitorMessage]: error in GetMonitorMessage:\n\t" + localIOException + this);

      localObject1 = "";
    }
    catch (NullPointerException localNullPointerException) {
      localObject1 = "";
    }
    catch (NumberFormatException localNumberFormatException) {
      System.out.println("Message Parser Server [getMonitorMessage]: number format error:\n\t" + localNumberFormatException + this);

      localObject1 = "";
    } catch (NoSuchElementException localNoSuchElementException) {
      System.out.println("Message Parser Server [getMonitorMessage]: no such element exception occurred:\n\t" + this);
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException) {
      System.out.println("Message Parser Server [getMonitorMessage]: Arr Idx Out of Bounds Excpt!\n\t" + this);

      localObject1 = "";
    }
    return ((String)(String)localObject1);
  }

  public String GetNextCommand(String paramString1, String paramString2)
  {
    try {
      String str1 = "REQUIRE";
      if (!(paramString2.equals(""))) str1 = paramString2;
      this.t = new StringTokenizer(paramString1, " :\n");

      String str2 = this.t.nextToken();
      for (; !(str2.trim().equals(str1.trim())); str2 = this.t.nextToken());
      str2 = this.t.nextToken();
      return str2; } catch (NoSuchElementException localNoSuchElementException) {
    }
    return null;
  }

  public boolean Login()
  {
    boolean i = false;
    IsEncrypted = false;
    StartedEncryption = false;
    try {
      if (this.HOST_PORT <= 0) return i;
      this.mesg = GetMonitorMessage();

      if (this.CType == 1) {
        System.out.println("Message Parser Server [Login]: checking monitor authenticity!");

        if (!(IsMonitorAuthentic(this.mesg))) return i;
      }
      IsVerified = 1;
      String str = GetNextCommand(this.mesg, "");

      if (str == null) return false;

      if (str.trim().equals("IDENT")) {
        if (!(IsEncrypted)) {
          System.out.println("Message Parser Server [Login]: starting encryption");

          if (!(dheOn)) SetEncryption();
          StartedEncryption = true;
          dheOn = true;
          System.out.println("Message Parser Server [Login]: encryption dhe started");
        }

        if (Execute(str)) {
          System.out.println("Message Parser Server [Login]: trying..");

          this.mesg = GetMonitorMessage();

          str = GetNextCommand(this.mesg, "");
          System.out.println("Message Parser Server [Login]: token received after Ident:\n\t" + str);

          if (str == null) return false;

          if (str.trim().equals("PASSWORD")) {
            System.out.println("Message Parser Server [Login]: received PASSWORD");

            if (Execute(str)) {
              str = "";
              this.mesg = GetMonitorMessage();

              COOKIE = GetNextCommand(this.mesg, "PASSWORD");
              System.out.println("Message Parser Server [Login]: got cookie:\n\t" + COOKIE);

              System.out.println("Message Parser Server [Login]: about to write personal data");

              if (!(WritePersonalData(this.PASSWORD, COOKIE))) return i;

              str = GetNextCommand(this.mesg, "");
              if (str == null) return false;
              if ((str.trim().equals("HOST_PORT")) && 
                (Execute(str))) {
                this.mesg = GetMonitorMessage();
                i = true;
                System.out.println("Message Parser Server [Login]: IsEncrypted and StartedEncryption after HP= " + IsEncrypted + StartedEncryption);

                System.out.println("Message Parser Server [Login]: launching active client:\n\t" + this.mesg);
              }
            }

          }
          else if (str.trim().equals("ALIVE")) {
            System.out.println("Message Parser Server [Login]: received ALIVE");

            if (Execute(str)) {
              this.mesg = GetMonitorMessage();
              str = GetNextCommand(this.mesg, "");

              System.out.println();

              if (this.CType == 0) {
                if (str == null) return true;
                if (str.trim().equals("HOST_PORT"))
                {
                  if (Execute(str)) {
                    this.mesg = GetMonitorMessage();
                    i = true;
                    System.out.println("Message Parser Server [Login]: after HP - IsEncrypted:" + IsEncrypted + " StartedEncryption:" + StartedEncryption);

                    System.out.println("Message Parser Server [Login]: launching active client:\n\t" + this.mesg);
                  }

                }
                else
                  i = true;
              } else if (this.CType == 1)
              {
                if (str.trim().equals("QUIT"))
                  if (Execute(str)) {
                    System.out.println("Message Parser Server [Login]: passive server quit normally");

                    this.mesg = GetMonitorMessage();
                    i = true;
                  }
                else i = true;
              }
            } else {
              str = GetNextCommand(this.mesg, "");
            }
          }
          else if (str.trim().equals("TRANSFER_RESPONSE")) {
            System.out.println("Message Parser Server [Login]: received TRANSFER_RESPONSE");

            if (Execute(str)) {
              System.out.println("Message Parser Server [Login]: transfer request processed");

              this.mesg = GetMonitorMessage();
              i = true;
            }
          }
          else if (str.trim().equals("WAR_DEFEND")) {
            System.out.println("Message Parser Server [Login]: received WAR_DEFEND");

            if (Execute(str)) {
              System.out.println("Message Parser Server [Login]: someone declared war!!");

              this.mesg = GetMonitorMessage();
              i = true;
            }
          } else {
            System.out.println("In Login - id=" + str.trim());
          }
        }
      }
    } catch (NullPointerException localNullPointerException) {
      System.out.println("Message Parser Server [Login]: null pointer error at login:\n\t" + localNullPointerException);

      i = false;
    }

    return i;
  }

  public boolean Execute(String paramString1, String paramString2) {
    boolean i = false;
    try {
      if (paramString1.trim().equals("PARTICIPANT_HOST_PORT")) {
        paramString1 = paramString1.concat(" ");
        paramString1 = paramString1.concat(paramString2);
        SendIt(paramString1);
        i = true;
      } else if (paramString1.trim().equals("TRANSFER_REQUEST")) {
        paramString1 = paramString1.concat(" ");
        paramString1 = paramString1.concat(paramString2);
        SendIt(paramString1);
        i = true;
      } else if (paramString1.trim().equals("TRANSFER_RESPONSE")) {
        paramString1 = paramString1.concat(" ");
        paramString1 = paramString1.concat(paramString2);
        SendIt(paramString1);
        i = true;
      } else if (paramString1.trim().equals("ROUNDS")) {
        paramString1 = paramString1.concat(" ");
        paramString1 = paramString1.concat(paramString2);
        SendIt(paramString1);
        i = true;
      } else if (paramString1.trim().equals("PUBLIC_KEY")) {
        paramString1 = paramString1.concat(" ");
        paramString1 = paramString1.concat(paramString2);
        SendIt(paramString1);
        i = true;
      } else if (paramString1.trim().equals("PARTICIPANT_HOST_PORT")) {
        paramString1 = paramString1.concat(" ");
        paramString1 = paramString1.concat(paramString2);
        SendIt(paramString1);
        i = true;
      } else if (paramString1.trim().equals("GET_CERTIFICATE")) {
        paramString1 = paramString1.concat(" ");
        paramString1 = paramString1.concat(paramString2);
        SendIt(paramString1);
        i = true;
      } else if (paramString1.trim().equals("MAKE_CERTIFICATE")) {
        paramString1 = paramString1.concat(" ");
        paramString1 = paramString1.concat(paramString2);
        SendIt(paramString1);
        i = true;
      } else if (paramString1.trim().equals("AUTHORIZE_SET")) {
        paramString1 = paramString1.concat(" ");
        paramString1 = paramString1.concat(paramString2);
        SendIt(paramString1);
        i = true;
      } else if (paramString1.trim().equals("SUBSET_J")) {
        paramString1 = paramString1.concat(" ");
        paramString1 = paramString1.concat(paramString2);
        SendIt(paramString1);
        i = true;
      } else if (paramString1.trim().equals("SUBSET_K")) {
        paramString1 = paramString1.concat(" ");
        paramString1 = paramString1.concat(paramString2);
        SendIt(paramString1);
        i = true;
      } else if (paramString1.trim().equals("SUBSET_A")) {
        paramString1 = paramString1.concat(" ");
        paramString1 = paramString1.concat(paramString2);
        SendIt(paramString1);
        i = true;
      }
    } catch (IOException localIOException) {
      System.out.println("IOError:\n\t" + localIOException);
      i = false;
    } catch (NullPointerException localNullPointerException) {
      System.out.println("Null Error has occured");
      i = false;
    }
    return i;
  }

  public boolean Execute(String paramString)
  {
    boolean i = false;
    try {
      if (paramString.trim().equals("IDENT")) {
        System.out.println("Message Parser Server [Execute]: received IDENT\n\t--->Use MyKey");

        paramString = paramString.concat(" ");
        paramString = paramString.concat(this.IDENT);
        paramString = paramString.concat(" ");
        paramString = paramString.concat(MyKey);

        System.out.println("Message Parser Server [Execute]: sent IDENT:\n\t" + paramString + "\n");

        SendIt(paramString);

        i = true;
      } else if (paramString.trim().equals("PASSWORD")) {
        System.out.println("Message Parser Server [Execute]: received PASSWORD");

        paramString = paramString.concat(" ");
        paramString = paramString.concat(this.PASSWORD);
        System.out.println("Message Parser Server [Execute]: sent PASSWORD:\n\t" + paramString);

        SendIt(paramString.trim());
        i = true;
      } else if (paramString.trim().equals("HOST_PORT")) {
        System.out.println("Message Parser Server [Execute]: received HOST_PORT");

        paramString = paramString.concat(" ");
        paramString = paramString.concat(HOSTNAME);
        paramString = paramString.concat(" ");
        paramString = paramString.concat(String.valueOf(this.HOST_PORT));
        System.out.println("Message Parser Server [Execute]: sent HOST_PORT:\n\t" + paramString);

        SendIt(paramString);
        i = true;
      } else if (paramString.trim().equals("ALIVE")) {
        System.out.println("Message Parser Server [Execute]: received ALIVE");

        paramString = paramString.concat(" ");
        paramString = paramString.concat(COOKIE);
        SendIt(paramString);
        i = true;
      } else if (paramString.trim().equals("QUIT")) {
        System.out.println("Message Parser Server [Execute]: received QUIT");

        SendIt(paramString);
        i = true;
      } else if (paramString.trim().equals("PARTICIPANT_STATUS")) {
        System.out.println("Message Parser Server [Execute]: received PARTICIPANT_STATUS");

        SendIt(paramString);
        i = true;
      } else if (paramString.trim().equals("GET_GAME_IDENTS")) {
        SendIt(paramString);
        i = true;
      } else if (paramString.trim().equals("RANDOM_PARTICIPANT_HOST_PORT")) {
        SendIt(paramString);
        i = true;
      } else {
        SendIt(paramString);
        i = true;
      }
    } catch (IOException localIOException) {
      System.out.println("IOError:\n\t" + localIOException);
      i = false;
    } catch (NullPointerException localNullPointerException) {
      System.out.println("Null Error has occured");
      i = false;
    }
    return i;
  }

  public void SendIt(String paramString) throws IOException {
    String str1 = paramString;
    System.out.println("Message Parser Server (sent):\n\t" + paramString + "\n");
    if (IsEncrypted) {
      String str2 = this.cipher.encrypt(paramString);
      paramString = str2;
    }
    this.sout.println(paramString);
    try {
      if (str1.substring(0, 5).equals("ROUNDS"))
        this.sout.println(paramString);
    } catch (Exception localException) {
    }
    for (; this.sout.checkError(); this.sout.println(paramString));
  }

  public void ChangePassword() {
    System.out.println("Message Parser Server [ChangePassword]:");
    GetIdentification();
    this.RandomPassword = GeneratePassword(sr);
    System.out.println("Message Parser Server [ChangePassword]: new random password:\n\t" + this.RandomPassword);

    String str = "CHANGE_PASSWORD " + this.PASSWORD + " " + this.RandomPassword.trim();
    UpdatePassword(str);
  }

  public void UpdatePassword(String paramString)
  {
    System.out.println("Message Parser Server [UpdatePassword]:");
    Object localObject = null;
    try {
      if ((paramString != null) && (!(paramString.equals("")))) {
        SendIt(paramString);
        this.mesg = GetMonitorMessage();
        this.t = new StringTokenizer(this.mesg, " :\n");
        String str1 = this.t.nextToken();
        if (str1.equals("RESULT")) {
          String str2 = GetNextCommand(this.mesg, "CHANGE_PASSWORD");
          if ((str2 != null) && (!(str2.equals(""))) && 
            (WritePersonalData(this.RandomPassword, str2))) {
            COOKIE = str2;
            this.PASSWORD = this.RandomPassword;
          }
        }
      }
    }
    catch (IOException localIOException) {
      System.out.println("Message Parser Server [UpdatePassword]: error in updating password file:\n\t" + localIOException);

      //localObject.close();
    } catch (NoSuchElementException localNoSuchElementException) {
      System.out.println("Message Parser Server [UpdatePassword]: error in getting token for updating password!");
    }
  }

  public void GetMonitorPublicKey(String paramString)
  {
    Object localObject = null;
    try {
      if ((paramString != null) && (!(paramString.equals("")))) {
        SendIt(paramString);
        this.mesg = GetMonitorMessage();
        this.t = new StringTokenizer(this.mesg, " :\n");
        String str1 = this.t.nextToken();
        if (str1.equals("RESULT")) {
          String str2 = GetNextCommand(this.mesg, "MONITOR_KEY");
          if ((str2 != null) && (!(str2.equals(""))));
        }
      }
    }
    catch (IOException localIOException) {
      System.out.println("Message Parser Server [GetMonitorPublicKey]: io exception error:\n\t" + localIOException);

      //localObject.close();
    } catch (NoSuchElementException localNoSuchElementException) {
      System.out.println("Message Parser: Server [GetMonitorPublicKey]: error in getting monitor's public key!");
    }
  }

  public String GeneratePassword(SecureRandom paramSecureRandom)
  {
    System.out.println("Message Parser Server [GeneratePassword]:");
    BigInteger localBigInteger = new BigInteger(128, paramSecureRandom);
    System.out.println("Message Parser Server [GeneratePassword]: random password:\n\t" + localBigInteger.abs().toString(16));

    return localBigInteger.abs().toString(16);
  }

  public void GetIdentification() {
    System.out.println("Message Parser Server [GetIndentification]:");
    BufferedReader localBufferedReader = null;
    try {
      localBufferedReader = new BufferedReader(new FileReader(passwordFileName));
      String str = localBufferedReader.readLine();
      if (str.equalsIgnoreCase("PASSWORD")) {
        str = localBufferedReader.readLine();
        if ((str != null) && (!(str.equals(""))))
          System.out.println("Message Parser Server [GetIndntification]: got PASSWORD from file: " + str);

        this.PASSWORD = str.trim();
        str = localBufferedReader.readLine();
        if (str.equalsIgnoreCase("COOKIE"))
          str = localBufferedReader.readLine();
        if ((str != null) && (!(str.equals(""))))
          System.out.println("Message Parser Server [GetIndntification]: got COOKIE from file: " + str);

        COOKIE = str.trim();
      }
      localBufferedReader.close();
    } catch (IOException localIOException) {
      System.out.println("Message Parser Server [GetIndentification]: error getting data from password file:\n\t" + localIOException);
    }
  }

  public boolean WritePersonalData(String paramString1, String paramString2)
  {
    System.out.println("Message Parser Server [WritePersonalData]:");
    boolean i = false;
    PrintWriter localPrintWriter = null;
    try {
      if ((paramString1 != null) && (!(paramString1.equals("")))) {
        System.out.println("Message Parser Server [WritePersonalData]: NEW PASSWORD = " + paramString1);

        localPrintWriter = new PrintWriter(new FileWriter(passwordFileName));
        localPrintWriter.println("PASSWORD");
        localPrintWriter.println(paramString1);
      }
      if ((paramString2 != null) && (!(paramString2.equals("")))) {
        System.out.println("Message Parser Server [WritePersonalData]: NEW COOKIE = " + paramString2);

        localPrintWriter.println("COOKIE");
        localPrintWriter.flush();
        localPrintWriter.println(paramString2);
        localPrintWriter.flush();
      }
      localPrintWriter.close();
      i = true;
      System.out.println("Message Parser Server [WritePersonalData]: success");
    }
    catch (IOException localIOException) {
      System.out.println("Message Parser Server [WritePersonalData]: error writing data password file:\n\t" + localIOException);

      localPrintWriter.close();
      return i;
    } catch (NumberFormatException localNumberFormatException) {
      System.out.println("Message Parser Server [WritePersonalData]: number format error:\n\t" + localNumberFormatException);
    }

    return i;
  }

  public boolean Verify(String paramString1, String paramString2)
    throws NoSuchAlgorithmException
  {
    System.out.println("Message Parser Server [Verify]:");
    int i = 0;
    try {
      MessageDigest localMessageDigest = MessageDigest.getInstance("SHA");
      paramString1 = paramString1.toUpperCase();
      byte[] arrayOfByte = paramString1.getBytes();
      localMessageDigest.update(arrayOfByte);
      BigInteger localBigInteger = new BigInteger(1, localMessageDigest.digest());
      System.out.println("Message Parser Server [Verify]: original chksum: " + paramString2);

      System.out.println("Message Parser Server [Verify]: calculated chksum from password: " + localBigInteger.toString(16));

      return (localBigInteger.toString(16).equals(paramString2.trim()));
    } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
    }
    return false;
  }

  public boolean IsMonitorAuthentic(String paramString)
  {
    System.out.println("Message Parser Server [IsMonitorAuthentic]:");
    boolean i = false;
    this.PPCHECKSUM = GetNextCommand(paramString, "PARTICIPANT_PASSWORD_CHECKSUM");
    System.out.println("Message Parser Server [IsMonitorAuthentic]: got checksum: " + this.PPCHECKSUM);
    try
    {
      if (this.PPCHECKSUM != null)
        if (!(Verify(this.PASSWORD, this.PPCHECKSUM))) {
          System.out.println("Message Parser Server [IsMonitorAuthntic]:MONITOR NOT AUTHORIZED!!! QUITING");

          IsVerified = 0;
          i = false;
        } else {
          System.out.println("Message Parser Server [IsMonitorAuthntic]:monitor verified!");

          i = true;
        }
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
      System.out.println("Message Parser Server [IsMonitorAuthentic]: error verifying:\n\t" + localNoSuchAlgorithmException);

      IsVerified = 0;
    }
    return i;
  }

  public void SetEncryption() {
    System.out.println("Message Parser Server [SetEncryption]: start encrypt dhe");
    try
    {
      if (dhk == null) {
        System.out.println("\t--->Get DH Key Object for first time.");

        this.oin = new ObjectInputStream(new FileInputStream("DHKey"));
        dhk = (DHKey)this.oin.readObject();
        this.oin.close();
      }
      if (dhe == null) {
        System.out.println("\t--->Get dhe object for first time using DH Key Object.");

        dhe = new DHE(dhk);
      }
      System.out.println("\t--->Get MyKey from key exchange using dhe.");
      MyKey = dhe.getExchangeKey();
      System.out.println("Message Parser Server [SetEncryption]: my key:\n\t" + MyKey);
    }
    catch (Exception localException) {
      System.out.println("Message Parser Server [SetEncryption]: error:\n\t" + localException);
    }
  }
}