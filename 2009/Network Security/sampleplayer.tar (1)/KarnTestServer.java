import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class KarnTestServer
{
  public static void main(String[] paramArrayOfString)
  {
    DiffieHellmanExchange localDiffieHellmanExchange = null;
    try
    {
      localDiffieHellmanExchange = new DiffieHellmanExchange("DHKey");
    } catch (Exception localException1) {
      System.out.println("Error getting DHKey from file.");
      System.exit(1);
    }

    try
    {
      ServerSocket localServerSocket = new ServerSocket(8280);
      Socket localSocket = localServerSocket.accept();
      BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));

      PrintWriter localPrintWriter = new PrintWriter(localSocket.getOutputStream(), true);

      Karn localKarn = new Karn(localDiffieHellmanExchange.computeSecret(localBufferedReader, localPrintWriter));

      String str1 = localBufferedReader.readLine();
      System.out.println("Server: ciphertext:" + str1 + "\n");
      String str2 = localKarn.decrypt(str1);
      System.out.println("Server: plaintext:" + str2);
    }
    catch (Exception localException2)
    {
      System.out.println("Whoops! - no network");
    }
  }
}