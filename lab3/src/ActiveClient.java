import java.io.*;
import java.net.*;

public class ActiveClient extends MessageParser implements Runnable {

    public static String MonitorName;
    Thread runner;
    Socket toMonitor = null;
    public static int MONITOR_PORT;
    public static int LOCAL_PORT;
    public int SleepMode;
    int DELAY = 90000; // Interval after which a new Active Client is started
    long prevTime, present;

    public ActiveClient() {
        super("[no-name]", "[no-password]");
        MonitorName = "";
        toMonitor = null;
        MONITOR_PORT = 0;
        LOCAL_PORT = 0;
    }

    public ActiveClient(String mname, int p, int lp, int sm, String name, String password) {
        super(name, password);
        try {
            SleepMode = sm;
            MonitorName = mname;
            MONITOR_PORT = p;
            LOCAL_PORT = lp;
        } catch (NullPointerException n) {
            System.out.println("ActiveClient [ActiveClient]: NullPointerException:\n\t" + n + this);
        }
    }

    public void start() {
        if (runner == null) {
            runner = new Thread(this);
            runner.start();
        }
    }

    public void run() {
        while (Thread.currentThread() == runner) {
            try {
                System.out.print("Active Client: trying monitor: " + MonitorName + " port: " + MONITOR_PORT + "...");
                toMonitor = new Socket(MonitorName, MONITOR_PORT);
                System.out.println("completed.");
                
                out = new PrintWriter(toMonitor.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(toMonitor.getInputStream()));

                HOSTNAME = toMonitor.getLocalAddress().getHostName();
                CType = 0; // Indicates Client
                
                HOST_PORT = LOCAL_PORT;
                if (!Login()) {
                    System.out.println("ActiveClient [run]: Login failed");
                    System.exit(1);
                }
                
                try {
                    String monMsg = GetMonitorMessage();
                    String nextCmd = GetNextCommand(monMsg,"");
                    if (!monMsg.trim().startsWith("RESULT: PASSWORD")) {
                        throw new Exception("MessageParser [Login]: Monitor may not be legit.  Banner = " + monMsg);
                    }

                    //TODO: Validate the password checksum

                    //TODO: This is where I left off...

                    if (!nextCmd.trim().equals("RESULT: ALIVE Identity has been verified. WAITING:")) {
                        monMsg = GetMonitorMessage();
                        while (!monMsg.trim().equals("")) {
                            nextCmd = GetNextCommand(monMsg,"");
                            if (nextCmd == null) {
                                // we may have reached a success state
                                break;
                            }
                            if (!nextCmd.trim().equals("HOST_PORT")) {
                                throw new Exception("ActiveClient [run]: Monitor may not be legit.  Asking for " + nextCmd + " instead of HOST_PORT");
                            }
                            
                            if (!Execute("HOST_PORT")) {
                                System.out.println("ActiveClient [run]: HOST_PORT failed");
                                monMsg = GetMonitorMessage();
                                continue;
                            }
                            
                            break;
                        }
                        System.out.println("ActiveClient [run]: Exited HOST_PORT loop");
                    }
                } catch (Exception e) {
                    System.out.println("ActiveClient [run]: Exception:\n\t" + e + this);
                    System.exit(1);
                }
                
                // at this point, we think we're legit 
                System.out.println("ActiveClient [run]: Login succeeded");
                IsVerified = 1;
                
                System.out.println("***************************");
                /*
                // TODO
                if (Execute("GET_GAME_IDENTS")) {
                    String msg = GetMonitorMessage();
                    System.out.println("ActiveClient [GET_GAME_IDENTS]:\n\t" + msg);
                }
                if (Execute("RANDOM_PARTICIPANT_HOST_PORT")) {
                    String msg = GetMonitorMessage();
                    System.out.println("ActiveClient [RANDOM_PARTICIPANT_HOST_PORT]:\n\t" + msg);
                }
                if (Execute("PARTICIPANT_HOST_PORT", "FRANCO")) {  // TODO
                    String msg = GetMonitorMessage();
                    System.out.println("ActiveClient [PARTICIPANT_HOST_PORT]:\n\t" + msg);
                }
                if (Execute("PARTICIPANT_STATUS")) {
                    String msg = GetMonitorMessage();
                    System.out.println("ActiveClient [PARTICIPANT_STATUS]:\n\t" + msg);
                }
                ChangePassword(PASSWORD);  // TODO:  where get new password?
                System.out.println("Password:" + PASSWORD);
                */
                toMonitor.close();
                out.close();
                in.close();
                try {
                    System.out.println("ActiveClient [run]: Sleeping for " + DELAY + "...");
                    Thread.sleep(DELAY);
                } catch (Exception e) {
                    System.out.println("ActiveClient [run]: Exception:\n\t" + e + this);
                }
                
                System.out.println("ActiveClient [run]: Looping again...");
                
            } catch (UnknownHostException e) {
                System.out.println("ActiveClient [run]: UnknownHostException:\n\t" + e + this);
            } catch (IOException e) {
                System.out.println("ActiveClient [run]: IOException:\n\t" + e + this);
                // TODO? - What does this block do?
                try {
                    toMonitor.close();
                    // toMonitor = new Socket(MonitorName,MONITOR_PORT);
                } catch (IOException ioe) {
                    System.out.println("ActiveClient [run]: IOException:\n\t" + ioe + this);
                } catch (NullPointerException n) {
                    System.out.println("ActiveClient [run]: NullPointerException:\n\t" + e + this);
                    try {
                        toMonitor.close();
                        // toMonitor = new Socket(MonitorName,MONITOR_PORT);
                    } catch (IOException ioe) {
                        System.out.println("ActiveClient [run]: IOException:\n\t" + ioe + this);
                    }
                }
            }
        }
    }
}
