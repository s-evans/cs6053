import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

class DHKey
  implements Serializable
{
  BigInteger p;
  BigInteger g;
  String Description;
  Date created;

  DHKey(BigInteger paramBigInteger1, BigInteger paramBigInteger2, String paramString)
  {
    this.p = paramBigInteger1;
    this.g = paramBigInteger2;

    this.Description = paramString;
    this.created = new Date();
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("Public Key(p): " + this.p.toString(32) + "\n");
    localStringBuffer.append("Public Key(g): " + this.g.toString(32) + "\n");
    localStringBuffer.append("Description: " + this.Description + "\n");
    localStringBuffer.append("Created: " + this.created);
    return localStringBuffer.toString();
  }
}