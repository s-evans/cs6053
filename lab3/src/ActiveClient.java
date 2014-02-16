import java.io.*;
import java.net.*;

public class ActiveClient extends MessageParser implements Runnable {

    public static String monitorName;
    Thread runner;
    Socket toMonitor = null;
    public static int monitorPort;
    public static int localPort;
    public int sleepMode;
    int DELAY = 90000; // Interval after which a new Active Client is started
    long prevTime, present;


    public ActiveClient(String mname, int p, int lp, int sm, String name, String password) throws IOException {
        super(name, password);
        try {
            sleepMode = sm;
            monitorName = mname;
            monitorPort = p;
            localPort = lp;
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
                System.out.print("Active Client: trying monitor: " + monitorName + " port: " + monitorPort + "...");
                toMonitor = new Socket(monitorName, monitorPort);
                System.out.println("completed.");

                out = new PrintWriter(toMonitor.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(toMonitor.getInputStream()));

                hostName = toMonitor.getLocalAddress().getHostName();
                CType = 0; // Indicates Client

                hostPort = localPort;

                //Attempt to Login, starting the DH exchange
                if (!Login()) {
                    System.out.println("ActiveClient [run]: Login failed");
                    System.exit(1);
                }

                String monMsg = GetMonitorMessage();
                String nextCmd = GetNextCommand(monMsg, "");

                if (!nextCmd.trim().equals("HOST_PORT")) {
                    System.out.println("ActiveClient [run]: Monitor may not be legit.  Asking for " + nextCmd + " instead of HOST_PORT");
                    System.exit(1);
                }

                if (!Execute("HOST_PORT")) {
                    System.out.println("ActiveClient [run]: HOST_PORT failed");
                    System.exit(1);
                }


                // at this point, we think we're legit
                System.out.println("ActiveClient [run]: Login succeeded");
                IsVerified = 1;

                System.out.println("***************************");
                /*
                // TODO: Implement client commands / attacks
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
                        // toMonitor = new Socket(monitorName,monitorPort);
                    } catch (IOException ioe) {
                        System.out.println("ActiveClient [run]: IOException:\n\t" + ioe + this);
                    } catch (NullPointerException n) {
                        System.out.println("ActiveClient [run]: NullPointerException:\n\t" + e + this);
                        try {
                            toMonitor.close();
                            // toMonitor = new Socket(monitorName,monitorPort);
                        } catch (IOException ioe) {
                            System.out.println("ActiveClient [run]: IOException:\n\t" + ioe + this);
                        }
                    }
                }
            }
        }
    }
