import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;

public class PlantDHKey
{
  public static void main(String[] paramArrayOfString)
  {
    try
    {
      BigInteger localBigInteger1 = new BigInteger("7897383601534681724700886135766287333879367007236994792380151951185032550914983506148400098806010880449684316518296830583436041101740143835597057941064647");
      BigInteger localBigInteger2 = new BigInteger("2333938645766150615511255943169694097469294538730577330470365230748185729160097289200390738424346682521059501689463393405180773510126708477896062227281603");
      DHKey localDHKey = new DHKey(localBigInteger1, localBigInteger2, "C653 DH key");
      FileOutputStream localFileOutputStream = new FileOutputStream("DHKey");
      ObjectOutputStream localObjectOutputStream = new ObjectOutputStream(localFileOutputStream);
      localObjectOutputStream.writeObject(localDHKey);
    } catch (Exception localException) {
      System.out.println("Whoops!");
    }
  }
}