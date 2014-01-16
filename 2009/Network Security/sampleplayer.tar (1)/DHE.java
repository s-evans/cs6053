import java.io.PrintStream;
import java.math.BigInteger;
import java.security.SecureRandom;

class DHE
{
  static SecureRandom sr = null;
  final int keysize = 512;
  final int ARBITRARY_CONSTANT = 80;
  final int RADIX = 32;
  private DHKey key;
  private BigInteger x;
  private BigInteger x_pub;
  private BigInteger s_secret;

  DHE(DHKey paramDHKey)
  {
    this.key = paramDHKey;
    if (sr == null) sr = new SecureRandom();

    System.out.println("DHKey: g=" + this.key.g.toString() + " n=" + this.key.p.toString());

    this.x = new BigInteger(512, sr);
    this.x_pub = this.key.g.modPow(this.x, this.key.p);
    this.s_secret = BigInteger.valueOf(0L);
  }

  DHE()
  {
    if (sr == null) sr = new SecureRandom();
    this.key = MakeKey(512, 80);

    this.x = new BigInteger(512, sr);
    this.x_pub = this.key.g.modPow(this.x, this.key.p);
    this.s_secret = BigInteger.valueOf(0L);
    System.out.println("Done");
  }

  DHKey getKeyObject() {
    return this.key; }

  public String getExchangeKey() {
    return this.x_pub.toString(32);
  }

  public boolean setExchangeKey(String paramString) {
    try {
      BigInteger localBigInteger = new BigInteger(paramString, 32);
      this.s_secret = localBigInteger.modPow(this.x, this.key.p);
      return true;
    } catch (NumberFormatException localNumberFormatException) {
      System.err.println("Malformed DH Key"); }
    return false;
  }

  BigInteger getSharedKey()
  {
    return this.s_secret;
  }

  private DHKey MakeKey(int paramInt1, int paramInt2) {
    BigInteger localBigInteger1 = BigInteger.valueOf(1L);
    BigInteger localBigInteger2 = BigInteger.valueOf(2L);
    BigInteger localBigInteger3 = BigInteger.valueOf(-1L);

    BigInteger localBigInteger4 = null;
    BigInteger localBigInteger5 = null;
    BigInteger localBigInteger6 = null;

    int j = 0;

    System.out.println("Initializing DHE ");
    System.out.print("Looking for a suitable n: ");

    int i = 0;
    do
    {
      localBigInteger4 = new BigInteger(paramInt1, paramInt2, sr);
      localBigInteger6 = localBigInteger4.subtract(localBigInteger1).divide(localBigInteger2);

      if (localBigInteger6.isProbablePrime(paramInt2)) i = 1;

      System.out.print((++j) + " "); }
    while (i == 0);

    System.out.println("\nFound " + localBigInteger4.toString(32));

    i = 0;

    System.out.print("Looking for a suitable g: ");
    j = 0;

    BigInteger localBigInteger7 = localBigInteger3.mod(localBigInteger4);
    do
    {
      localBigInteger5 = new BigInteger(paramInt1 - 2, sr);
      if ((localBigInteger5.compareTo(localBigInteger7) != 0) && (localBigInteger5.compareTo(localBigInteger5.modPow(localBigInteger6, localBigInteger7)) == 0));
      i = 1;
      System.out.print((++j) + " "); }
    while (i == 0);
    return new DHKey(localBigInteger4, localBigInteger5, "DHE $Revision: 1.1 $/512");
  }

  public String toString() {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("Secret Key(x): " + this.x.toString(32) + "\n");
    localStringBuffer.append("Public Key(X): " + this.x_pub.toString(32) + "\n");
    localStringBuffer.append("Shared Key   : " + this.s_secret.toString(32));
    return localStringBuffer.toString();
  }
}