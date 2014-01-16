import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.SecureRandom;

class DiffieHellmanExchange
{
  int keysize;
  DHKey key;
  BigInteger x;
  BigInteger x_pub;
  BigInteger s_secret;

  public DiffieHellmanExchange(String paramString)
    throws Exception
  {
    this.keysize = 512;
    FileInputStream localFileInputStream = new FileInputStream(paramString);
    ObjectInputStream localObjectInputStream = new ObjectInputStream(localFileInputStream);
    this.key = ((DHKey)localObjectInputStream.readObject());
    localObjectInputStream.close();
    SecureRandom localSecureRandom = new SecureRandom();
    this.x = new BigInteger(this.keysize, localSecureRandom);
    this.x_pub = this.key.g.modPow(this.x, this.key.p); }

  public BigInteger getPublicKey(String paramString) {
    return this.x_pub;
  }

  public BigInteger computeSecret(BufferedReader paramBufferedReader, PrintWriter paramPrintWriter)
    throws IOException
  {
    paramPrintWriter.println(this.x_pub.toString());
    BigInteger localBigInteger = new BigInteger(paramBufferedReader.readLine());
    this.s_secret = localBigInteger.modPow(this.x, this.key.p);
    System.out.println("Client: shared secret computed!");
    return this.s_secret;
  }
}