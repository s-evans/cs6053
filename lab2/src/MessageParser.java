import java.util.*;
import java.io.*;

public class MessageParser {
    // Monitor Handling Declarations
    int COMMAND_LIMIT = 25;
    public int CType;
    public static String HOSTNAME;
    PrintWriter out = null;
    BufferedReader in = null;
    String mesg, sentmessage;
    String filename;
    StringTokenizer t;
    String IDENT = "Skipper";  // TODO
    String PASSWORD = "franco";  // TODO
    static String COOKIE = "bkuhn";  // TODO
    String PPCHECKSUM = "";  // TODO
    int HOST_PORT;
    public static int IsVerified;

    // File I/O Declarations
    BufferedReader fIn = null;
    PrintWriter fOut = null;
    static String InputFileName = "Input.dat";
    static String ResourceFileName = "Resources.dat";
    String[] cmdArr = new String[COMMAND_LIMIT];

    static String MyKey;
    String MonitorKey;
    String first;
    ObjectInputStream oin = null;
    ObjectOutputStream oout = null;

    public MessageParser() {
        filename = "passwd.dat";
        GetIdentification(); // Gets Password and Cookie from 'passwd.dat' file
    }

    public MessageParser(String ident, String password) {
        filename = ident + ".dat";
        PASSWORD = password;
        IDENT = ident;
        GetIdentification(); // Gets Password and Cookie from 'passwd.dat' file
    }

    public String GetMonitorMessage() {
        String sMesg = "", decrypt = "";
        try {
            String temp = in.readLine();
            first = temp; // 1st
            sMesg = temp;
            decrypt = temp;

            // After IDENT has been sent-to handle partially encrypted msg group
            while (!(decrypt.trim().equals("WAITING:"))) {
                temp = in.readLine();
                sMesg = sMesg.concat(" ");
                decrypt = temp;
                sMesg = sMesg.concat(decrypt);
            } // sMesg now contains the Message Group sent by the Monitor
        } catch (IOException e) {
            System.out.println("MessageParser [GetMonitorMessage]: IOException:\n\t" + e + this);
            sMesg = "";
        } catch (NullPointerException n) {
            System.out.println("MessageParser [GetMonitorMessage]: NullPointerException:\n\t" + n + this);
            sMesg = "";
        } catch (NumberFormatException o) {
            System.out.println("MessageParser [GetMonitorMessage]: NumberFormatException:\n\t" + o + this);
            sMesg = "";
        } catch (NoSuchElementException ne) {
            System.out.println("MessageParser [GetMonitorMessage]: NoSuchElementException:\n\t" + ne + this);
            sMesg = "";
        } catch (ArrayIndexOutOfBoundsException ae) {
            System.out.println("MessageParser [GetMonitorMessage]: ArrayIndexOutOfBoundsException:\n\t" + ae + this);
            sMesg = "";
        }
        return sMesg;
    }

    // Handling Cookie and PPChecksum
    public String GetNextCommand(String mesg, String sCommand) {
        try {
            String sDefault = "REQUIRE";
            if (!(sCommand.equals("")))
                sDefault = sCommand;
            t = new StringTokenizer(mesg, " :\n");
            // Search for the REQUIRE Command
            String temp = t.nextToken();
            while (!(temp.trim().equals(sDefault.trim())))
                temp = t.nextToken();
            temp = t.nextToken();
            System.out.println("MessageParser [GetNextCommand]: returning " + temp);
            return temp; // returns what the monitor wants
        } catch (NoSuchElementException e) {
            System.out.println("MessageParser [GetNextCommand]: NoSuchElementException:\n\t" + e + this);
            return null;
        }
    }

    public boolean Login() {
        boolean success = false;
        try {
            // TODO
        } catch (NullPointerException n) {
            System.out.println("MessageParser [Login]: NullPointerException:\n\t" + n + this);
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
                SendIt(sentmessage);
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
        boolean success = false;
        try {
            if (sentmessage.trim().equals("IDENT")) {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(IDENT);
                SendIt(sentmessage);
                success = true;
            } else if (sentmessage.trim().equals("PASSWORD")) {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(PASSWORD);
                SendIt(sentmessage.trim());
                success = true;
            } else if (sentmessage.trim().equals("HOST_PORT")) {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(HOSTNAME);// hostname
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(String.valueOf(HOST_PORT));
                SendIt(sentmessage);
                success = true;
            } else if (sentmessage.trim().equals("ALIVE")) {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(COOKIE);
                SendIt(sentmessage);
                success = true;
            } else if (sentmessage.trim().equals("QUIT")) {
                SendIt(sentmessage);
                success = true;
            } else if (sentmessage.trim().equals("SIGN_OFF")) {
                SendIt(sentmessage);
                success = true;
            } else if (sentmessage.trim().equals("GET_GAME_IDENTS")) {
                SendIt(sentmessage);
                success = true;
            } else if (sentmessage.trim().equals("PARTICIPANT_STATUS")) {
                SendIt(sentmessage);
                success = true;
            } else if (sentmessage.trim().equals("RANDOM_PARTICIPANT_HOST_PORT")) {
                SendIt(sentmessage);
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

    public void SendIt(String message) throws IOException {
        try {
            System.out.println("MessageParser [SendIt]: sent message:\n\t" + message);
            out.println(message);
            if (out.checkError() == true)
                throw (new IOException());
            out.flush();
            if (out.checkError() == true)
                throw (new IOException());
        } catch (IOException e) {
            System.out.println("MessageParser [SendIt]: IOException:\n\t" + e + this);
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
                    SendIt("SYNTHESIZE WEAPONS");
                    mesg = GetMonitorMessage();
                    SendIt("SYNTHESIZE COMPUTERS");
                    mesg = GetMonitorMessage();
                    SendIt("SYNTHESIZE VEHICLES");
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
                pout = new PrintWriter(new FileWriter(filename));
                
                pout.println("PASSWORD");
                pout.println(Passwd);
                pout.flush();

                if ((Cookie != null) && !(Cookie.equals(""))) {
                    pout.println("COOKIE");
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

    // Check whether the Monitor is Authentic
    public boolean Verify(String passwd, String chksum) {
        // TODO
        return false;
    }

    public boolean IsMonitorAuthentic(String MonitorMesg) {
        // TODO
        return false;
    }
}
