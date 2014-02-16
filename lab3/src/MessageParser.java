import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.*;
import java.math.BigInteger;

//TODO: Remove all unused functions and variables
public class MessageParser {
    // Monitor Handling Declarations
    private final static int COMMAND_LIMIT = 25;
    public int CType;
    public static String hostName;

    //out and in are initialized by implementors
    PrintWriter out = null;
    BufferedReader in = null;
    String mesg, sentmessage;
    String FILENAME;
    StringTokenizer t;
    String IDENT;
    String PASSWORD;
    private static String cookie;  // TODO - Make this read from file/database/constructor (like username/password?)
    String PPCHECKSUM = "";
    int hostPort;
    public static int IsVerified;
    private boolean encryptionStarted = false;

    // File I/O Declarations
    BufferedReader fIn = null;
    PrintWriter fOut = null;
    static String InputFileName = "Input.dat";  // TODO
    static String ResourceFileName = "Resources.dat";  // TODO
    String[] cmdArr = new String[COMMAND_LIMIT];

    static String MyKey;
    String monitorKey;
    String first;
    ObjectInputStream oin = null;
    ObjectOutputStream oout = null;


    private final DiffieHellmanExchange diffieHellmanExchange;
    private Karn karnProcessor;
    private BigInteger publicKey;


    public MessageParser(String ident, String password) throws IOException {
        FILENAME = ident + ".dat";  // TODO
        PASSWORD = password;
        IDENT = ident;
        GetIdentification(); // Gets Password and Cookie from 'passwd.dat' file

        //Init DH Exchange data
        PlantDHKey.plantKey();
        diffieHellmanExchange = new DiffieHellmanExchange();
    }

    // function taken from Dr. Franco's monitor source code
    public static final String performSHA(String input) throws NoSuchAlgorithmException {
        /*
         * This method is: Copyright(C) 1998 Robert Sexton. Use it any way you
         * wish. Just leave my name on.
         */

        MessageDigest md;
        byte target[];

        md = MessageDigest.getInstance("SHA");
        target = input.toUpperCase().getBytes();
        md.update(target);

        return (new BigInteger(1, md.digest())).toString(16);
    }

    //Retrieve the next message from
    public String GetMonitorMessage() {

        String finalMsg = "";

        //Read socket input from monitor until "WAITING:" is found
        try {
            String currentLine;
            do {
                //Read the next line. decrypt if necessary
                currentLine = in.readLine();
                if (encryptionStarted) {
                    currentLine = karnProcessor.decrypt(currentLine);
                }

                finalMsg = finalMsg.concat(currentLine);
                finalMsg = finalMsg.concat(" ");
            } while (!currentLine.trim().equals("WAITING:"));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return finalMsg;
        }
    }

    // Handling Cookie and PPChecksum
    public String GetNextCommand(String mesg, String sCommand) {
        String sDefault = "REQUIRE";
        if (!(sCommand.equals("")))
            sDefault = sCommand;

        try {
            t = new StringTokenizer(mesg, " :\n");

            // Search for the sDefault Command
            String temp = t.nextToken();
            while (!(temp.trim().equals(sDefault.trim())))
                temp = t.nextToken();
            temp = t.nextToken();

            System.out.println("MessageParser [GetNextCommand]: returning " + temp);
            return temp; // returns what the monitor wants
        } catch (NoSuchElementException e) {
            // didn't find 'sDefault' string in 'mesg'
            return null;
        }
    }

    //Performs the DiffieHellman exchange common to Active Client and Server
    public boolean Login() {
        boolean success;
        try {
            String monBanner = GetMonitorMessage();
            String nextCmd = GetNextCommand(monBanner, "");
            if (!monBanner.trim().startsWith("COMMENT: Monitor Version 2.2.1")) {
                throw new Exception("MessageParser [Login]: Monitor may not be legit.  Banner = " + monBanner);
            }

            //TODO: Validate Participant Password Checksum

            publicKey = diffieHellmanExchange.getDHParmMakePublicKey("DHKey");

            //Send the DH public key
            if (Execute("IDENT") != true) {
                throw new Exception("MessageParser [Login]: IDENT failed");
            }

            //TODO: Add monitor authentication

            //Get the monitor DH exchange reply and use to start encryption
            //only the first line is unencrypted
            String nextMsg = in.readLine();
            if (nextMsg.startsWith("RESULT: IDENT")) {
                String[] msgParts = nextMsg.split(" ");

                if (msgParts.length == 3) {
                    monitorKey = msgParts[2];
                    karnProcessor = new Karn(diffieHellmanExchange.getSecret(monitorKey));
                    encryptionStarted = true;
                } else {
                    throw new Exception("MessageParser [Login]: Monitor may not be legit.  Received: " + nextMsg);
                }
            } else {
                throw new Exception("MessageParser [Login]: Monitor may not be legit.  DH exchange response: " + nextCmd
                        + " instead of RESULT:IDENT");
            }

            nextMsg = GetMonitorMessage();
            nextCmd = GetNextCommand(nextMsg, "");

            //The client expects PASSWORD and the server expects ALIVE
            if (!nextCmd.trim().equals("PASSWORD") && !nextCmd.trim().equals("ALIVE")) {
                throw new Exception("MessageParser [Login]: Monitor may not be legit.  Asking for " + nextCmd
                        + " instead of PASSWORD or ALIVE");
            }

            //Send our password encrypted
            if (nextCmd.trim().equals("PASSWORD")) {
                success = Execute("PASSWORD");
            } else {
                success = Execute("ALIVE");
            }

            nextMsg = GetMonitorMessage();
            nextCmd = GetNextCommand(nextMsg, "");

            if (!nextMsg.trim().startsWith("RESULT: PASSWORD") && !nextMsg.trim().startsWith("RESULT: ALIVE Identity has been verified")) {
                throw new Exception("MessageParser [Login]: Monitor may not be legit.  Banner = " + nextMsg);
            }

            //TODO: Validate the password checksum

            //Parse the monitors password from the response if it is an initial login
            if (nextMsg.trim().startsWith("RESULT: PASSWORD")) {
                String[] msgParts = nextMsg.split(" ");
                if (msgParts.length < 3) {
                    throw new Exception("MessageParser [Login]: Monitor may not be legit.  Banner = " + nextMsg);
                }
                cookie = msgParts[2];
                //TODO: Write the cookie to file if this session is going to work again after restart

                if (!nextCmd.trim().equals("HOST_PORT")) {
                    System.out.println("ActiveClient [run]: Monitor may not be legit.  Asking for " + nextCmd + " instead of HOST_PORT");
                    System.exit(1);
                }
            }


        } catch (Exception e) {
            System.out.println("MessageParser [Login]: Exception:\n\t" + e + this);
            monitorKey = null;
            encryptionStarted = false;
            success = false;
        }

        System.out.println("MessageParser [Login]: returning " + success);
        return success;
    }

    // Handle Directives and Execute appropriate commands with one argument
    public boolean Execute(String sentmessage, String arg) {
        boolean success = false;
        try {
            if (sentmessage.trim().equals("PARTICIPANT_HOST_PORT")) {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(arg);
                SendIt(sentmessage, true);
                // TODO: validate result before considering this success?
                success = true;
            } else {
                System.out.println("MessageParser [Execute]: " + sentmessage + " not implemented");
                success = false;
            }
        } catch (IOException e) {
            System.out.println("MessageParser [Execute]: IOException:\n\t" + e + this);
            success = false;
        } catch (NullPointerException n) {
            System.out.println("MessageParser [Execute]: NullPointerException:\n\t" + n + this);
            success = false;
        }
        return success;
    }

    // Handle Directives and Execute appropriate commands
    public boolean Execute(String sentmessage) {
        //TODO: This would look so much nicer with an enum and switch statement
        boolean success = false;
        try {
            if (sentmessage.trim().equals("IDENT")) {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(IDENT);
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(publicKey.toString(32));
                //Do not encrypt since Ident is used before encryption is started
                SendIt(sentmessage, false);
                // TODO: validate result before considering this success?
                success = true;
            } else if (sentmessage.trim().equals("PASSWORD")) {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(PASSWORD);
                SendIt(sentmessage.trim(), true);
                // TODO: validate result before considering this success?
                success = true;
            } else if (sentmessage.trim().equals("HOST_PORT")) {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(hostName);// hostname
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(String.valueOf(hostPort));
                SendIt(sentmessage, true);
                // TODO: validate result before considering this success?
                success = true;
            } else if (sentmessage.trim().equals("ALIVE")) {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(cookie);
                SendIt(sentmessage, true);
                // TODO: validate result before considering this success?
                success = true;
            } else if (sentmessage.trim().equals("QUIT")) {
                SendIt(sentmessage, true);
                // TODO: validate result before considering this success?
                success = true;
            } else if (sentmessage.trim().equals("SIGN_OFF")) {
                SendIt(sentmessage, true);
                // TODO: validate result before considering this success?
                success = true;
            } else if (sentmessage.trim().equals("GET_GAME_IDENTS")) {
                SendIt(sentmessage, true);
                // TODO: validate result before considering this success?
                success = true;
            } else if (sentmessage.trim().equals("PARTICIPANT_STATUS")) {
                SendIt(sentmessage, true);
                success = true;
            } else if (sentmessage.trim().equals("RANDOM_PARTICIPANT_HOST_PORT")) {
                SendIt(sentmessage, true);
                // TODO: validate result before considering this success?
                success = true;
            } else {
                System.out.println("MessageParser [Execute]: " + sentmessage + " not implemented");
                success = false;
            }
        } catch (IOException e) {
            System.out.println("MessageParser [Execute]: IOException:\n\t" + e + this);
            success = false;
        } catch (NullPointerException n) {
            System.out.println("MessageParser [Execute]: NullPointerException:\n\t" + n + this);
            success = false;
        } catch (SecurityException e) {
            System.out.println("MessageParser [Execute]: SecurityException:\n\t" + e + this);
            success = false;
        }
        return success;
    }

    public void SendIt(String message, boolean shouldEncrypt) throws IOException {
        String finalMessage = message;

        try {
            System.out.println("MessageParser [SendIt]: plaintext message:\n\t" + finalMessage);
            if (shouldEncrypt && encryptionStarted) {
                finalMessage = karnProcessor.encrypt(message);
                System.out.println("MessageParser [SendIt]: encrypted message:\n\t" + finalMessage);
            } else if (shouldEncrypt) {
                throw new SecurityException("Attempted to send encrypted message before DH Exchange completed! Investigate!");
            }
            out.println(finalMessage);
            if (out.checkError() == true)
                throw (new IOException());
            out.flush();
            if (out.checkError() == true)
                throw (new IOException());
        } catch (Exception e) {
            System.out.println("MessageParser [SendIt]: Exception:\n\t" + e + this);
        } // Bubble the Exception upwards
    }

    // In future send parameters here so that diff commands are executed
    public boolean ProcessExtraMessages() {
        boolean success = false;
        System.out.println("MessageParser [ProcessExtraMessages]: received message:\n\t" + mesg.trim());

        if ((mesg.trim().equals("")) || (mesg.trim().equals(null))) {
            mesg = GetMonitorMessage();
            System.out.println("MessageParser [ProcessExtraMessages]: received (2):\n\t" + mesg.trim());
        }

        String id = GetNextCommand(mesg, "");

        if (id == null) { // No Require, can Launch Free Form Commands Now
            if (Execute("PARTICIPANT_STATUS")) { // Check for Player Status
                mesg = GetMonitorMessage();
                success = true;
                try {
                    SaveResources(mesg); // Save the data to a file
                    SendIt("SYNTHESIZE WEAPONS", true);
                    mesg = GetMonitorMessage();
                    SendIt("SYNTHESIZE COMPUTERS", true);
                    mesg = GetMonitorMessage();
                    SendIt("SYNTHESIZE VEHICLES", true);
                    mesg = GetMonitorMessage();
                    if (Execute("PARTICIPANT_STATUS")) { // Check for Player
                        // Status
                        mesg = GetMonitorMessage();
                        success = true;
                        SaveResources(mesg);// Save the data to a file
                    }
                } catch (IOException e) {
                    System.out.println("MessageParser [ProcessExtraMessages]: IOException:\n\t" + e + this);
                    success = false;
                }
            }
        } else {
            mesg = GetMonitorMessage();
            System.out.println("MessageParser [ProcessExtraMessages]: failed " + "extra message parse");
            success = false;
        }
        return success;
    }

    public void MakeFreeFlowCommands() throws IOException {
        // TODO
    }

    public void SaveResources(String res) throws IOException {
        System.out.println("MessageParser [SaveResources]:");
        try { // If an error occurs then don't update the Resources File
            String temp = GetNextCommand(res, "COMMAND_ERROR");
            if ((temp == null) || (temp.equals(""))) {
                fOut = new PrintWriter(new FileWriter(ResourceFileName));
                t = new StringTokenizer(res, " :\n");
                try {
                    temp = t.nextToken();
                    temp = t.nextToken();
                    temp = t.nextToken();
                    System.out.println("MessageParser [SaveResources]: got " + "token before write: " + temp);
                    for (int i = 0; i < 20; i++) {
                        fOut.println(temp);
                        fOut.flush();
                        temp = t.nextToken();
                    }
                } catch (NoSuchElementException ne) {
                    System.out.println("MessageParser [SaveResources]: NoSuchElementException:\n\t" + ne + this);
                    temp = "";
                    fOut.close();
                }
            }
            fOut.close();
        } catch (IOException e) {
            System.out.println("MessageParser [SaveResources]: IOException:\n\t" + e + this);
            fOut.close();
        }
    }

    public void ChangePassword(String newpassword) {
        GetIdentification(); // Gives the previous values of cookie and password
        String quer = "CHANGE_PASSWORD " + PASSWORD + " " + newpassword;
        UpdatePassword(quer, newpassword);
    }

    // Update Password
    // throws IOException
    public void UpdatePassword(String cmd, String newpassword) {
        // TODO
    }

    public void GetIdentification() {
        // TODO
    }

    // Write Personal data such as Password and Cookie
    public boolean WritePersonalData(String Passwd, String Cookie) {
        boolean success = false;
        PrintWriter pout = null;
        try {
            if ((Passwd != null) && !(Passwd.equals(""))) {
                pout = new PrintWriter(new FileWriter(FILENAME));

                pout.println("PASSWORD");
                pout.println(Passwd);
                pout.flush();

                if ((Cookie != null) && !(Cookie.equals(""))) {
                    pout.println("cookie");
                    pout.println(Cookie);
                    pout.flush();
                }

                pout.close();
            }
            success = true;
        } catch (IOException e) {
            System.out.println("MessageParser [WritePersonalData]: IOException:\n\t" + e + this);
            return success;
        } catch (NumberFormatException n) {
            System.out.println("MessageParser [WritePersonalData]: NumberFormatException:\n\t" + n + this);
        }
        return success;
    }
}