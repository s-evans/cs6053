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

    // out and in are initialized by implementors
    PrintWriter out = null;
    BufferedReader in = null;
    String mesg, sentmessage;
    String FILENAME;
    StringTokenizer t;
    String IDENT;
    String PASSWORD;
    private String cookie;
    String PPCHECKSUM = "";
    int hostPort;
    public static int IsVerified;
    private boolean encryptionStarted = false;

    // File I/O Declarations
    BufferedReader fIn = null;
    PrintWriter fOut = null;
    static String ResourceFileName = "Resources.dat"; // TODO
    String[] cmdArr = new String[COMMAND_LIMIT];

    static String MyKey;
    String monitorKey;
    String first;
    ObjectInputStream oin = null;
    ObjectOutputStream oout = null;

    private final DiffieHellmanExchange diffieHellmanExchange;
    private Karn karnProcessor;
    private BigInteger publicKey;

    public MessageParser(String ident) throws IOException {
        IDENT = ident;
        GetIdentification(); // Gets Password and Cookie from file

        // Init DH Exchange data
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

    // Retrieve the next message from
    @SuppressWarnings("finally")
    public String GetMonitorMessage() {

        String finalMsg = "";

        // Read socket input from monitor until "WAITING:" is found
        try {
            String currentLine;
            do {
                // Read the next line. decrypt if necessary
                currentLine = in.readLine();
                // TODO: This IsEncrypted check is just a shortcut. When the
                // connection is re-established, encryption also needs to be
                // re-initialized.
                if (encryptionStarted && Karn.IsEncrypted(currentLine)) {
                    currentLine = karnProcessor.decrypt(currentLine);
                }

                finalMsg = finalMsg.concat(currentLine);
                finalMsg = finalMsg.concat(" ");
            } while (!currentLine.trim().equals("WAITING:"));

        } catch (Exception e) {
            System.out.println("MessageParser [GetMonitorMessage]: Exception:\n\t" + e + " " + this);
        } finally {
            System.out.println("MessageParser [GetMonitorMessage]: Returning " + finalMsg);
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

    // Performs the DiffieHellman exchange common to Active Client and Server
    public boolean Login(boolean bIsServer) {
        boolean success;
        try {
            String monBanner = GetMonitorMessage();
            String nextCmd = GetNextCommand(monBanner, "");
            String expectedBanner;

            if (bIsServer) {
                expectedBanner = "COMMENT: Monitor Version 2.2.1 PARTICIPANT_PASSWORD_CHECKSUM:  "
                        + performSHA(PASSWORD) + " REQUIRE: IDENT WAITING:";
            } else {
                expectedBanner = "COMMENT: Monitor Version 2.2.1 REQUIRE: IDENT WAITING:";
            }
            if (!monBanner.trim().equals(expectedBanner) || !nextCmd.trim().equals("IDENT")) {
                throw new Exception("MessageParser [Login]: Monitor may not be legit.\nActual   = " + monBanner
                        + "\nExpected = " + expectedBanner);
            }

            publicKey = diffieHellmanExchange.getDHParmMakePublicKey("DHKey");

            // Send the DH public key
            if (Execute("IDENT") != true) {
                throw new Exception("MessageParser [Login]: IDENT failed");
            }

            // Get the monitor DH exchange reply and use to start encryption
            // only the first line is unencrypted
            String nextMsg = in.readLine();
            if (!nextMsg.startsWith("RESULT: IDENT")) {
                throw new Exception("MessageParser [Login]: Monitor may not be legit.  DH exchange response: "
                        + nextCmd + " instead of RESULT: IDENT");
            }

            String[] msgParts = nextMsg.split(" ");
            if (msgParts.length == 3) {
                monitorKey = msgParts[2];
                karnProcessor = new Karn(diffieHellmanExchange.getSecret(monitorKey));
                encryptionStarted = true;
            } else {
                throw new Exception("MessageParser [Login]: Monitor may not be legit.  Received: " + nextMsg);
            }

            nextMsg = GetMonitorMessage();
            nextCmd = GetNextCommand(nextMsg, "");

            if (!nextCmd.trim().equals("ALIVE")) {
                throw new Exception("MessageParser [Login]: Monitor may not be legit.  Asking for " + nextCmd
                        + " instead of ALIVE");
            }
            success = Execute("ALIVE");

            nextMsg = GetMonitorMessage();
            nextCmd = GetNextCommand(nextMsg, "");

            if (nextCmd != null) {
                if (!bIsServer) {
                    // client

                    if (!nextCmd.trim().equals("HOST_PORT")) {
                        throw new Exception("MessageParser [Login]: Monitor may not be legit.  Asking for " + nextCmd
                                + " instead of HOST_PORT");
                    }

                    if (nextCmd.trim().equals("HOST_PORT")) {
                        success = Execute("HOST_PORT");
                    }
                } else {
                    // server

                    if (!nextCmd.trim().equals("QUIT") && !nextMsg.trim().contains(" TRANSFER: ")) {
                        throw new Exception("MessageParser [Login]: Monitor may not be legit.  Asking for " + nextCmd
                                + " instead of QUIT, and doesn't appear to be a TRANSFER");
                    }

                    if (nextCmd.trim().equals("QUIT")) {
                        success = Execute("QUIT");
                    } else if (nextMsg.trim().contains(" TRANSFER: ")) {
                        success = EvaluateTransfer(nextMsg);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("MessageParser [Login]: Exception:\n\t" + e + " " + this);
            monitorKey = null;
            encryptionStarted = false;
            success = false;
        }

        System.out.println("MessageParser [Login]: returning " + success);
        return success;
    }

    // Handle Directives and Execute appropriate commands
    public boolean Execute(String sentmessage) {
        // TODO: This would look so much nicer with an enum and switch statement
        boolean success = false;
        try {
            if (sentmessage.trim().equals("IDENT")) {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(IDENT);
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(publicKey.toString(32));
                // Do not encrypt since Ident is used before encryption is
                // started
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
            System.out.println("MessageParser [Execute]: IOException:\n\t" + e + " " + this);
            success = false;
        } catch (NullPointerException n) {
            System.out.println("MessageParser [Execute]: NullPointerException:\n\t" + n + " " + this);
            success = false;
        } catch (SecurityException e) {
            System.out.println("MessageParser [Execute]: SecurityException:\n\t" + e + " " + this);
            success = false;
        }
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
            System.out.println("MessageParser [Execute]: IOException:\n\t" + e + " " + this);
            success = false;
        } catch (NullPointerException n) {
            System.out.println("MessageParser [Execute]: NullPointerException:\n\t" + n + " " + this);
            success = false;
        }
        return success;
    }

    // Handle Directives and Execute appropriate commands with one argument
    public boolean Execute(String sentmessage, String arg1, String arg2, String arg3) {
        boolean success = false;
        try {
            if (sentmessage.trim().equals("TRANSFER_REQUEST")) {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(arg1);
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(arg2);
                sentmessage = sentmessage.concat(" FROM ");
                sentmessage = sentmessage.concat(arg3);
                SendIt(sentmessage, true);
                // TODO: validate result before considering this success?
                success = true;
            } else {
                System.out.println("MessageParser [Execute]: " + sentmessage + " not implemented");
                success = false;
            }
        } catch (IOException e) {
            System.out.println("MessageParser [Execute]: IOException:\n\t" + e + " " + this);
            success = false;
        } catch (NullPointerException n) {
            System.out.println("MessageParser [Execute]: NullPointerException:\n\t" + n + " " + this);
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
                throw new SecurityException(
                        "Attempted to send encrypted message before DH Exchange completed! Investigate!");
            }
            out.println(finalMessage);
            if (out.checkError() == true)
                throw (new IOException());
            out.flush();
            if (out.checkError() == true)
                throw (new IOException());
        } catch (Exception e) {
            System.out.println("MessageParser [SendIt]: Exception:\n\t" + e + " " + this);
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
                    System.out.println("MessageParser [ProcessExtraMessages]: IOException:\n\t" + e + " " + this);
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
        System.out.println("MessageParser [SaveResources]");
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
                    System.out.println("MessageParser [SaveResources]: NoSuchElementException:\n\t" + ne + " " + this);
                    temp = "";
                    fOut.close();
                }
            }
            fOut.close();
        } catch (IOException e) {
            System.out.println("MessageParser [SaveResources]: IOException:\n\t" + e + " " + this);
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

    /**
     * Retrieves the password and cookie identification data from persistent
     * storage
     */
    public void GetIdentification() {
        String identFile = "users/" + IDENT;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(identFile));
            String line = bufferedReader.readLine();
            String[] identEntry = line.split("[:,]"); // passwords cannot
                                                      // contain ':' or ','
                                                      // characters, or this
                                                      // will break
            if (identEntry.length != 6) {
                System.out.println("MessageParser [GetIdentification]: Invalid formatted line");
            } else {
                // Check to make sure that identity matches current identity
                if (identEntry[0].trim().equals("IDENT") && identEntry[1].trim().equals(IDENT)) {
                    // Set the password if non-empty
                    if (identEntry[2].trim().equals("PASSWORD")) {
                        String tempPassword = identEntry[3].trim();
                        if (tempPassword.length() != 0)
                            PASSWORD = tempPassword;
                    }
                    // Set the cookie if non-empty
                    if (identEntry[4].trim().equals("COOKIE")) {
                        String tempCookie = identEntry[5].trim();
                        if (tempCookie.length() != 0)
                            cookie = tempCookie;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("MessageParser [GetIdentification]: FileNotFoundException:\n\t" + e + " " + this);
            // Nothing to do, the user maybe hasn't been used yet
        } catch (IOException e) {
            System.out.println("MessageParser [GetIdentification]: IOException:\n\t" + e + " " + this);
        }
    }

    /**
     * Write Personal data such as Password and Cookie
     * 
     * @return success of writing data to persistent storage
     */
    public boolean WritePersonalData() {
        boolean success = false;
        PrintWriter pout;
        String Passwd = PASSWORD;
        String Cookie = cookie;
        try {
            if (Passwd == null) {
                Passwd = "";
            }

            if (Cookie == null) {
                Cookie = "";
            }
            String identFile = "users/" + IDENT;
            pout = new PrintWriter(new FileWriter(identFile));
            pout.printf("IDENT: %s, PASSWORD: %s, COOKIE: %s\n", IDENT, Passwd, Cookie);
            pout.flush();
            pout.close();

            success = true;
        } catch (IOException e) {
            System.out.println("MessageParser [WritePersonalData]: IOException:\n\t" + e + " " + this);
        }

        return success;
    }

    /**
     * Initiate a transfer
     * 
     * @return success of initiating transfer
     */
    public boolean InitiateTransfer(String recipient, Integer amount, String sender) {
        boolean success = false;
        Initiator initiator = new Initiator();

        try {
            if (Execute("TRANSFER_REQUEST", recipient, amount.toString(), sender) == false) {
                throw new Exception("Execute failed");
            }

            String monMsg = GetMonitorMessage();
            String nextCmd = GetNextCommand(monMsg, "");
            if (!nextCmd.trim().equals("PUBLIC_KEY")) {
                throw new Exception("Monitor may not be legit.  Asking for " + nextCmd + " instead of PUBLIC_KEY");
            }

            SendIt(initiator.getPublicKey(), true);
            // TODO: validate result before considering this success?

            monMsg = GetMonitorMessage();
            nextCmd = GetNextCommand(monMsg, "");
            if (!monMsg.trim().startsWith("RESULT: ROUNDS ")) {
                throw new Exception("Monitor may not be legit.  Unexpected message: " + monMsg);
            }
            if (!nextCmd.trim().equals("AUTHORIZE_SET")) {
                throw new Exception("Monitor may not be legit.  Asking for " + nextCmd + " instead of AUTHORIZE_SET");
            }

            initiator.saveRounds(monMsg);

            SendIt(initiator.getAuthorizeSet(), true);
            // TODO: validate result before considering this success?

            monMsg = GetMonitorMessage();
            nextCmd = GetNextCommand(monMsg, "");
            if (!monMsg.trim().startsWith("RESULT: SUBSET_A ")) {
                throw new Exception("Monitor may not be legit.  Unexpected message: " + monMsg);
            }
            if (!nextCmd.trim().equals("SUBSET_K")) {
                throw new Exception("Monitor may not be legit.  Asking for " + nextCmd + " instead of SUBSET_K");
            }

            initiator.saveSubsetA(monMsg);

            SendIt(initiator.getSubsetK(), true);
            // TODO: validate result before considering this success?

            monMsg = GetMonitorMessage();
            nextCmd = GetNextCommand(monMsg, "");
            if (!nextCmd.trim().equals("SUBSET_J")) {
                throw new Exception("Monitor may not be legit.  Asking for " + nextCmd + " instead of SUBSET_J");
            }

            SendIt(initiator.getSubsetJ(), true);
            // TODO: validate result before considering this success?

            success = true;

        } catch (Exception e) {
            System.out.println("MessageParser [InitiateTransfer]: Exception:\n\t" + e + " " + this);
        }

        return success;
    }

    public boolean EvaluateTransfer(String nextMsg) {
        boolean success = false;
        Sender sender = new Sender();
        Integer rounds = 64;
        String recipientName;
        Integer amount;
        String senderName;

        try {
            StringTokenizer s = new StringTokenizer(nextMsg, "TRANSFER: ");
            s.nextToken(); // skip past "RESULT: ALIVE " section
            StringTokenizer t = new StringTokenizer(s.nextToken(), " ");
            recipientName = t.nextToken();
            amount = new Integer(t.nextToken());
            senderName = t.nextToken();

            if (!ShouldWeConsiderThisTransfer(recipientName, amount, senderName)) {
                throw new Exception("Not even considering transferring " + amount + " from " + senderName + " to "
                        + recipientName);
            }

            String monMsg = GetMonitorMessage();
            String nextCmd = GetNextCommand(monMsg, "");
            if (!monMsg.trim().startsWith("RESULT: PUBLIC_KEY ")) {
                throw new Exception("Monitor may not be legit.  Unexpected message: " + monMsg);
            }
            if (!nextCmd.trim().equals("ROUNDS")) {
                throw new Exception("Monitor may not be legit.  Asking for " + nextCmd + " instead of ROUNDS");
            }

            sender.savePublicKey(monMsg);

            SendIt(sender.getRounds(rounds), true);
            // TODO: validate result before considering this success?

            monMsg = GetMonitorMessage();
            nextCmd = GetNextCommand(monMsg, "");
            if (!monMsg.trim().startsWith("RESULT: AUTHORIZE_SET ")) {
                throw new Exception("Monitor may not be legit.  Unexpected message: " + monMsg);
            }
            if (!nextCmd.trim().equals("SUBSET_A")) {
                throw new Exception("Monitor may not be legit.  Asking for " + nextCmd + " instead of SUBSET_A");
            }

            sender.saveAuthorizeSet(monMsg);

            SendIt(sender.getSubsetA(), true);
            // TODO: validate result before considering this success?

            monMsg = GetMonitorMessage();
            nextCmd = GetNextCommand(monMsg, "");
            if (!monMsg.trim().startsWith("RESULT: SUBSET_K ")) {
                throw new Exception("Monitor may not be legit.  Unexpected message: " + monMsg);
            }
            if (!monMsg.trim().contains("RESULT: SUBSET_J ")) {
                throw new Exception("Monitor may not be legit.  Unexpected message: " + monMsg);
            }
            if (!nextCmd.trim().equals("TRANSFER_RESPONSE")) {
                throw new Exception("Monitor may not be legit.  Asking for " + nextCmd
                        + " instead of TRANSFER_RESPONSE");
            }

            if (!sender.checkSubsetK(monMsg)) {
                throw new Exception("checkSubsetK() failed");
            }
            if (!sender.checkSubsetJ(monMsg)) {
                throw new Exception("checkSubsetJ() failed");
            }

            SendIt(sender.response(), true);
            // TODO: validate result before considering this success?

            success = true;

        } catch (Exception e) {
            System.out.println("MessageParser [EvaluateTransfer]: Exception:\n\t" + e + " " + this);
        }

        return success;
    }

    private static boolean ShouldWeConsiderThisTransfer(String recipientName, Integer amount, String senderName) {
        boolean senderInOurGroup = IsUserInOurGroup(senderName);
        boolean recipientInOurGroup = IsUserInOurGroup(recipientName);

        if (senderInOurGroup && !recipientInOurGroup) {
            // transfer is leaving our group
            return false;
        } else if (!senderInOurGroup && recipientInOurGroup) {
            // transfer is entering our group
            return true;
        } else if (senderInOurGroup && recipientInOurGroup) {
            // transfer is completely within our group
            return true;
        } else {
            // transfer is completely outside of our group
            return false;
        }
    }

    public static boolean IsUserInOurGroup(String username) {
        String user = username.toUpperCase();

        return (user == "ALIVE" || user == "IDENT" || user == "PASSWORD");
    }
}
