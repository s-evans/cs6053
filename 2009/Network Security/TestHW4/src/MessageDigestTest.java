import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class MessageDigestTest
{
  public static void main(String[] paramArrayOfString)
  {
    try
    {
      MessageDigest localMessageDigest = MessageDigest.getInstance("SHA");
      SecureRandom localSecureRandom1 = new SecureRandom();
      SecureRandom localSecureRandom2 = new SecureRandom();
      byte[] arrayOfByte1 = new byte[80];
      byte[] arrayOfByte2 = new byte[80];

      localSecureRandom1.nextBytes(arrayOfByte1);
      localSecureRandom2.nextBytes(arrayOfByte2);

      localMessageDigest.reset();
      localMessageDigest.update(arrayOfByte1);
      localMessageDigest.update(arrayOfByte1);
      localMessageDigest.update(arrayOfByte1);
      byte[] arrayOfByte3 = localMessageDigest.digest();
      try {
        int i = 0;
        System.out.print(arrayOfByte3[i] + "|");

        ++i;
      }
      catch (Exception localException5) {
        System.out.println();

        localMessageDigest.reset();
        localMessageDigest.update(arrayOfByte2);
        localMessageDigest.update(arrayOfByte2);
        localMessageDigest.update(arrayOfByte2);
        byte[] arrayOfByte4 = localMessageDigest.digest();
        try {
          int j = 0;
          System.out.print(arrayOfByte4[j] + "|");

          ++j;
        }
        catch (Exception e) {
          System.out.println();

          localMessageDigest.reset();
          localMessageDigest.update(arrayOfByte2);
          localMessageDigest.update(arrayOfByte1);
          localMessageDigest.update(arrayOfByte2);
          byte[] arrayOfByte5 = localMessageDigest.digest();
          try {
            int k = 0;
            System.out.print(arrayOfByte5[k] + "|");

            ++k;
          }
          catch (Exception f) {
            System.out.println();

            localMessageDigest.reset();
            localMessageDigest.update(arrayOfByte1);
            localMessageDigest.update(arrayOfByte2);
            localMessageDigest.update(arrayOfByte2);
            byte[] arrayOfByte6 = localMessageDigest.digest();
            try {
              int l = 0;
              System.out.print(arrayOfByte6[l] + "|");

              ++l;
            }
            catch (Exception g) {
              System.out.println();
            }
          }
        }
      }
    }
    catch (Exception localException1)
    {
    }
  }
}