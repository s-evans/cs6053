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

public class MessageParserClient
{
  boolean prevent_encryption = false;
  int COMMAND_LIMIT = 25;
  public int CType;
  public static String HOSTNAME;
  PrintStream out = null;
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

  public MessageParserClient(String paramString1, String paramString2)
  {
    this.IDENT = paramString1;
    passwordFileName = new String("passwd.dat." + paramString1);
    System.out.println("Message Parser Client [Constructor]: called by: " + this + " Name: " + this.IDENT + " Password: " + this.PASSWORD);

    GetIdentification();
    if (sr == null) {
      System.out.println("Message Parser Client [Constructor]: generating secure random number");

      sr = new SecureRandom();
    }
    String str = GeneratePassword(sr);
    if ((str.trim().equals("")) || (str.trim().equals(null)))
      this.RandomPassword = str.trim();
  }

  public String GetMonitorMessage()
  {
    System.out.println("Message Parser Client:");
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
        System.out.println("Message Parser Client [getMonitorMessage]: monitor key:\n\t" + this.MonitorKey);

        dhe.setExchangeKey(this.MonitorKey);
        BigInteger localBigInteger = dhe.getSharedKey();
        System.out.println("Message Parser Client [getMonitorMessage]: shared key:\n\t" + localBigInteger);

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
      System.out.println("Message Parser Client [getMonitorMessage]: error in GetMonitorMessage:\n\t" + localIOException + this);

      localObject1 = "";
    }
    catch (NullPointerException localNullPointerException) {
      localObject1 = "";
    }
    catch (NumberFormatException localNumberFormatException) {
      System.out.println("Message Parser Client [getMonitorMessage]: number format error:\n\t" + localNumberFormatException + this);

      localObject1 = "";
    } catch (NoSuchElementException localNoSuchElementException) {
      System.out.println("Message Parser Client [getMonitorMessage]: no such element exception occurred:\n\t" + this);
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException) {
      System.out.println("Message Parser Client [getMonitorMessage]: Arr Idx Out of Bounds EXCEPTION!\n\t" + this);

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
      if (this.HOST_PORT <= 0) return false;
      this.mesg = GetMonitorMessage();

      if (this.CType == 1) {
        System.out.println("Message Parser Client [Login]: checking monitor authenticity!");

        if (!(IsMonitorAuthentic(this.mesg))) return i;
        
      }
      IsVerified = 1;
      String str = GetNextCommand(this.mesg, "");

      if (str == null) return false;

      if (str.trim().equals("IDENT")) {
        if ((!(IsEncrypted)) && (!(this.prevent_encryption))) {
          System.out.println("Message Parser Client [Login]: starting encryption");

          if (!(dheOn)) SetEncryption();
          StartedEncryption = true;
          dheOn = true;
          System.out.println("Message Parser Client [Login]: encryption dhe started");
        }

        if (Execute(str)) {
          System.out.println("Message Parser Client [Login]: trying..");

          this.mesg = GetMonitorMessage();

          str = GetNextCommand(this.mesg, "");
          System.out.println("Message Parser Client [Login]: token received after Ident:\n\t" + str);

          if (str == null) return false;

          if (str.trim().equals("PASSWORD")) {
            System.out.println("Message Parser Client [Login]: received PASSWORD");

            if (Execute(str)) {
              str = "";
              this.mesg = GetMonitorMessage();

              COOKIE = GetNextCommand(this.mesg, "PASSWORD");
              System.out.println("Message Parser Client [Login]: got cookie:\n\t" + COOKIE);

              System.out.println("Message Parser Client [Login]: about to write personal data");

              //if (!(WritePersonalData(this.PASSWORD, COOKIE))) return i;
              if (!(WritePersonalData(this.PASSWORD, COOKIE))) return false;
              
              str = GetNextCommand(this.mesg, "");
              if (str == null) return false;
              if ((str.trim().equals("HOST_PORT")) && 
                (Execute(str))) {
                this.mesg = GetMonitorMessage();
                i = true;
                System.out.println("Message Parser Client [Login]: IsEncrypted and StartedEncryption after HP= " + IsEncrypted + StartedEncryption);

                System.out.println("Message Parser Client [Login]: launching active client:\n\t" + this.mesg);
              }
            }

          }
          else if (str.trim().equals("ALIVE")) {
            System.out.println("Message Parser Client [Login]: received ALIVE");

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
                    System.out.println("Message Parser Client [Login]: after HP - IsEncrypted:" + IsEncrypted + " StartedEncryption:" + StartedEncryption);

                    System.out.println("Message Parser Client [Login]: launching active client:\n\t" + this.mesg);
                  }
                }
                else
                  i = true;
              } else if (this.CType == 1)
              {
                if (str.trim().equals("QUIT"))
                  if (Execute(str)) {
                    System.out.println("Message Parser Client [Login]: server quit normally");

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
            System.out.println("Message Parser Client [Login]: received TRANSFER_RESPONSE");

            if (Execute(str)) {
              System.out.println("Message Parser Client [Login]: transfer request processed");

              this.mesg = GetMonitorMessage();
              i = true;
            }
          }
          else if (str.trim().equals("WAR_DEFEND")) {
            System.out.println("Message Parser Client [Login]: received WAR_DEFEND");

            if (Execute(str)) {
              System.out.println("Message Parser Client [Login]: someone declared war!!");

              this.mesg = GetMonitorMessage();
              i = true;
            }
          }
        }
      }
    } catch (NullPointerException localNullPointerException) {
      System.out.println("Message Parser Client [Login]: null pointer error at login:\n\t" + localNullPointerException);

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
        System.out.println("Message Parser Client [Execute]: received IDENT\n\t--->Use MyKey");

        paramString = paramString.concat(" ");
        paramString = paramString.concat(this.IDENT);
        paramString = paramString.concat(" ");
        paramString = paramString.concat(MyKey);

        System.out.print("Message Parser Client [Execute]: sent IDENT:\n\t" + paramString + "\n");

        SendIt(paramString);

        i = true;
      } else if (paramString.trim().equals("PASSWORD")) {
        System.out.println("Message Parser Client [Execute]: received PASSWORD");

        paramString = paramString.concat(" ");
        paramString = paramString.concat(this.PASSWORD);
        System.out.println("Message Parser Client [Execute]: sent PASSWORD:\n\t" + paramString);

        SendIt(paramString.trim());
        i = true;
      } else if (paramString.trim().equals("HOST_PORT")) {
        System.out.println("Message Parser Client [Execute]: received HOST_PORT");

        paramString = paramString.concat(" ");
        paramString = paramString.concat(HOSTNAME);
        paramString = paramString.concat(" ");
        paramString = paramString.concat(String.valueOf(this.HOST_PORT));
        System.out.println("Message Parser Client [Execute]: sent HOST_PORT:\n\t" + paramString);

        SendIt(paramString);
        i = true;
      } else if (paramString.trim().equals("ALIVE")) {
        System.out.println("Message Parser Client [Execute]: received ALIVE");

        paramString = paramString.concat(" ");
        paramString = paramString.concat(COOKIE);
        SendIt(paramString);
        i = true;
      } else if (paramString.trim().equals("QUIT")) {
        System.out.println("Message Parser Client [Execute]: received QUIT");

        SendIt(paramString);
        i = true;
      } else if (paramString.trim().equals("PARTICIPANT_STATUS")) {
        System.out.println("Message Parser Client [Execute]: received PARTICIPANT_STATUS");

        SendIt(paramString);
        i = true;
      } else if (paramString.trim().equals("GET_GAME_IDENTS")) {
        SendIt(paramString);
        i = true;
      } else if (paramString.trim().equals("RANDOM_PARTICIPANT_HOST_PORT")) {
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
    try {
      System.out.println("Message Parser Client (sent):\n\t" + paramString);
      if (IsEncrypted) {
        String str = this.cipher.encrypt(paramString);
        paramString = str;
      }
      this.out.println(paramString);
      if (this.out.checkError() == true) throw new IOException();
      this.out.flush();
      if (this.out.checkError() == true) throw new IOException(); 
    } catch (IOException localIOException) {
    }
  }

  public void ChangePassword() {
    System.out.println("Message Parser Client [ChangePassword]:");
    GetIdentification();
    this.RandomPassword = GeneratePassword(sr);
    System.out.println("Message Parser Client [ChangePassword]: new random password:\n\t" + this.RandomPassword);

    String str = "CHANGE_PASSWORD " + this.PASSWORD + " " + this.RandomPassword.trim();
    UpdatePassword(str);
  }

  public void UpdatePassword(String paramString)
  {
    System.out.println("Message Parser Client [UpdatePassword]:");
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
      System.out.println("Message Parser Client [UpdatePassword]: error in updating password file:\n\t" + localIOException);
      
      //localObject.close();
    } catch (NoSuchElementException localNoSuchElementException) {
      System.out.println("Message Parser Client [UpdatePassword]: error in getting token for updating password!");
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
      System.out.println("Message Parser Client [GetMonitorPublicKey]: io exception error:\n\t" + localIOException);

      //localObject.close();
    } catch (NoSuchElementException localNoSuchElementException) {
      System.out.println("Message Parser Client [GetMonitorPublicKey]: error in getting monitor's public key!");
    }
  }

  public String GeneratePassword(SecureRandom paramSecureRandom)
  {
    System.out.println("Message Parser Client [GeneratePassword]:");
    BigInteger localBigInteger = new BigInteger(128, paramSecureRandom);
    System.out.println("Message Parser Client [GeneratePassword]: random password:\n\t" + localBigInteger.abs().toString(16));

    return localBigInteger.abs().toString(16);
  }

  public void GetIdentification() {
    System.out.println("Message Parser Client [GetIndentification]:");
    BufferedReader localBufferedReader = null;
    try {
      localBufferedReader = new BufferedReader(new FileReader(passwordFileName));
      String str = localBufferedReader.readLine();
      if (str.equalsIgnoreCase("PASSWORD")) {
        str = localBufferedReader.readLine();
        if ((str != null) && (!(str.equals(""))))
          System.out.println("Message Parser Client [GetIndentification]: got PASSWORD from file: " + str);

        this.PASSWORD = str.trim();
        str = localBufferedReader.readLine();
        if (str.equalsIgnoreCase("COOKIE"))
          str = localBufferedReader.readLine();
        if ((str != null) && (!(str.equals(""))))
          System.out.println("Message Parser Client [GetIndentification]: got COOKIE from file: " + str);

        COOKIE = str.trim();
      }
      localBufferedReader.close();
    } catch (IOException localIOException) {
      System.out.println("Message Parser Client [GetIndentification]: error getting data from password file:\n\t" + localIOException);
    }
  }

  public boolean WritePersonalData(String paramString1, String paramString2)
  {
    System.out.println("Message Parser Client [WritePersonalData]:");
    boolean i = false;
    PrintWriter localPrintWriter = null;
    try {
      if ((paramString1 != null) && (!(paramString1.equals("")))) {
        System.out.println("Message Parser Client [WritePersonalData]: NEW PASSWORD = " + paramString1);

        localPrintWriter = new PrintWriter(new FileWriter(passwordFileName));
        localPrintWriter.println("PASSWORD");
        localPrintWriter.println(paramString1);
      }
      if ((paramString2 != null) && (!(paramString2.equals("")))) {
        System.out.println("Message Parser Client [WritePersonalData]: NEW COOKIE = " + paramString2);

        localPrintWriter.println("COOKIE");
        localPrintWriter.flush();
        localPrintWriter.println(paramString2);
        localPrintWriter.flush();
      }
      localPrintWriter.close();
      i = true;
      System.out.println("Message Parser Client [WritePersonalData]: success");
    }
    catch (IOException localIOException) {
      System.out.println("Message Parser Client [WritePersonalData]: error writing data password file:\n\t" + localIOException);

      localPrintWriter.close();
      return i;
    } catch (NumberFormatException localNumberFormatException) {
      System.out.println("Message Parser Client [WritePersonalData]: number format error:\n\t" + localNumberFormatException);
    }

    return i;
  }

  public boolean Verify(String paramString1, String paramString2)
    throws NoSuchAlgorithmException
  {
    System.out.println("Message Parser Client [Verify]:");
    boolean i = false;
    try {
      MessageDigest localMessageDigest = MessageDigest.getInstance("SHA");
      paramString1 = paramString1.toUpperCase();
      byte[] arrayOfByte = paramString1.getBytes();
      localMessageDigest.update(arrayOfByte);
      BigInteger localBigInteger = new BigInteger(1, localMessageDigest.digest());
      System.out.println("Message Parser Client [Verify]: original chksum: " + paramString2);

      System.out.println("Message Parser Client [Verify]: calculated chksum from password: " + localBigInteger.toString(16));

      return (localBigInteger.toString(16).equals(paramString2.trim()));
    } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
    }
    return false;
  }

  public boolean IsMonitorAuthentic(String paramString)
  {
    System.out.println("Message Parser Client [IsMonitorAuthentic]:");
    boolean i = false;
    this.PPCHECKSUM = GetNextCommand(paramString, "PARTICIPANT_PASSWORD_CHECKSUM");
    System.out.println("Message Parser Client [IsMonitorAuthentic]: got checksum: " + this.PPCHECKSUM);
    try
    {
      if (this.PPCHECKSUM != null)
        if (!(Verify(this.PASSWORD, this.PPCHECKSUM))) {
          System.out.println("Message Parser Client [IsMonitorAuthentic]: MONITOR NOT AUTHORIZED!!! QUITING");

          IsVerified = 0;
          i = false;
        } else {
          System.out.println("Message Parser Client [IsMonitorAuthentic]: monitor verified!");

          i = true;
        }
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
      System.out.println("Message Parser Client [IsMonitorAuthentic]: error verifying:\n\t" + localNoSuchAlgorithmException);

      IsVerified = 0;
    }
    return i;
  }

  public void SetEncryption() {
    System.out.println("Message Parser Client [SetEncryption]: start encrypt dhe");
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
      System.out.println("Message Parser Client [SetEncryption]: my key:\n\t" + MyKey);
    }
    catch (Exception localException) {
      System.out.println("Message Parser Client [SetEncryption]: error:\n\t" + localException);
    }
  }
}