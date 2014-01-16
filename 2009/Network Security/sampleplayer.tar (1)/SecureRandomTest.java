import java.io.PrintStream;
import java.math.BigInteger;
import java.security.SecureRandom;

public class SecureRandomTest
{
  public static void main(String[] paramArrayOfString)
  {
    SecureRandom localSecureRandom = new SecureRandom();
    byte[] arrayOfByte1 = new byte[20];
    byte[] arrayOfByte2 = new byte[40];
    byte[] arrayOfByte3 = new byte[256];
    localSecureRandom.nextBytes(arrayOfByte1);
    BigInteger localBigInteger = new BigInteger(1, arrayOfByte1);
    System.out.println(localBigInteger.toString(32));
    localSecureRandom.nextBytes(arrayOfByte2);
    localBigInteger = new BigInteger(1, arrayOfByte2);
    System.out.println(localBigInteger.toString(32));
    localSecureRandom.nextBytes(arrayOfByte3);
    localBigInteger = new BigInteger(1, arrayOfByte3);
    System.out.println(localBigInteger.toString(32));
  }
}