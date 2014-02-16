import java.io.IOException;

class Homework {
    public static String monitorName;
    public static int monitorPort = 8180;
    public static final int HOST_PORT = 22334;  // TODO: randomize this

    private static ActiveClient activeClient;
    private static Server server;

    public Homework(String name, String password) {
        System.out.println("Project Begin:\n\tMonitor: " + monitorName + " random port: " + HOST_PORT
                + " monitor port: " + monitorPort);
        try {
            activeClient = new ActiveClient(monitorName, monitorPort, HOST_PORT, 0, name, password);
            server = new Server(HOST_PORT, HOST_PORT, name, password);
        } catch (IOException e) {
            System.err.println("Could not start client");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Homework monitor monitor-port ident");
        } else {
            monitorName = new String(args[0]);
            monitorPort = Integer.parseInt(args[1]);
            Homework hw = new Homework(args[2], "qwerqwer");  // TODO
            hw.activeClient.start(); // Start the Active Client
            hw.server.start(); // Start the Server
        }
    }
}
