import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;

public class KarnTestClient
{
  public static void main(String[] paramArrayOfString)
  {
    DiffieHellmanExchange localDiffieHellmanExchange = null;
    try
    {
      localDiffieHellmanExchange = new DiffieHellmanExchange("DHKey");
    } catch (Exception localException1) {
      System.out.println("Error in getting DHKey from file.");
      System.exit(1);
    }

    try
    {
      Socket localSocket = new Socket("localhost", 8280);
      BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));

      PrintWriter localPrintWriter = new PrintWriter(localSocket.getOutputStream(), true);

      Karn localKarn = new Karn(localDiffieHellmanExchange.computeSecret(localBufferedReader, localPrintWriter));

      String str1 = paramArrayOfString[0];
      System.out.println("Client: plaintext:" + str1 + "\n");
      String str2 = localKarn.encrypt(str1);
      System.out.println("Client: ciphertext:" + str2 + "\n");
      localPrintWriter.println(str2);
    }
    catch (Exception localException2)
    {
      System.out.println("Yikes!");
    }
  }
}