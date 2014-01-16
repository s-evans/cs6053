import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

class Karn
{
  final int RADIX = 32;
  final int PADSIZE = 40;
  private byte[] key;
  private byte[] key_left;
  private byte[] key_right;
  static SecureRandom sr = null;
  static MessageDigest md = null;

  Karn(BigInteger paramBigInteger)
  {
    if (sr == null) sr = new SecureRandom();
    this.key = paramBigInteger.toByteArray();

    this.key_left = new byte[this.key.length / 2];
    this.key_right = new byte[this.key.length / 2];

    for (int i = 0; i < this.key.length / 2; ++i) {
      this.key_left[i] = this.key[i];
      this.key_right[i] = this.key[(i + this.key.length / 2)];
    }
    try
    {
      md = MessageDigest.getInstance("SHA");
    } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
      System.err.println("Yow! NoSuchAlgorithmException. Abandon all hope");
    }
  }

  String encrypt(String paramString)
  {
    byte[] arrayOfByte6 = StringToBytes(paramString);

    byte[] arrayOfByte1 = new byte[20];
    byte[] arrayOfByte2 = new byte[20];

    byte[] arrayOfByte3 = new byte[20];
    byte[] arrayOfByte4 = new byte[20];

    byte[] arrayOfByte5 = new byte[20];

    int i = 0;
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();

    localByteArrayOutputStream.write(42);

    while (i < arrayOfByte6.length)
    {
      for (int j = 0; j < 20; ++j) {
        arrayOfByte1[j] = arrayOfByte6[(i + j)];
        arrayOfByte2[j] = arrayOfByte6[(i + 20 + j)];
      }

      md.reset();
      md.update(arrayOfByte1);
      md.update(this.key_left);
      arrayOfByte5 = md.digest();

      for (j = 0; j < 20; ++j) {
        arrayOfByte4[j] = (byte)(arrayOfByte5[j] ^ arrayOfByte2[j]);
      }

      md.reset();
      md.update(arrayOfByte4);
      md.update(this.key_right);
      arrayOfByte5 = md.digest();
      for (j = 0; j < 20; ++j)
        arrayOfByte3[j] = (byte)(arrayOfByte5[j] ^ arrayOfByte1[j]);

      localByteArrayOutputStream.write(arrayOfByte3, 0, 20);
      localByteArrayOutputStream.write(arrayOfByte4, 0, 20);
      i += 40;
    }
    BigInteger localBigInteger = new BigInteger(localByteArrayOutputStream.toByteArray());
    return localBigInteger.toString(32);
  }

  String decrypt(String paramString)
  {
    BigInteger localBigInteger = new BigInteger(paramString, 32);
    byte[] arrayOfByte1 = localBigInteger.toByteArray();

    ByteArrayOutputStream localByteArrayOutputStream1 = new ByteArrayOutputStream();
    localByteArrayOutputStream1.write(arrayOfByte1, 1, arrayOfByte1.length - 1);
    arrayOfByte1 = localByteArrayOutputStream1.toByteArray();

    byte[] arrayOfByte2 = new byte[20];
    byte[] arrayOfByte3 = new byte[20];

    byte[] arrayOfByte4 = new byte[20];
    byte[] arrayOfByte5 = new byte[20];

    byte[] arrayOfByte6 = new byte[20];

    int i = 0;
    ByteArrayOutputStream localByteArrayOutputStream2 = new ByteArrayOutputStream();
    while (i < arrayOfByte1.length)
    {
      for (int j = 0; j < 20; ++j) {
        arrayOfByte4[j] = arrayOfByte1[(i + j)];
        arrayOfByte5[j] = arrayOfByte1[(i + 20 + j)];
      }

      md.reset();
      md.update(arrayOfByte5);
      md.update(this.key_right);
      arrayOfByte6 = md.digest();
      for (j = 0; j < 20; ++j) {
        arrayOfByte2[j] = (byte)(arrayOfByte6[j] ^ arrayOfByte4[j]);
      }

      md.reset();
      md.update(arrayOfByte2);
      md.update(this.key_left);
      arrayOfByte6 = md.digest();
      for (j = 0; j < 20; ++j)
        arrayOfByte3[j] = (byte)(arrayOfByte6[j] ^ arrayOfByte5[j]);
      localByteArrayOutputStream2.write(arrayOfByte2, 0, 20);
      localByteArrayOutputStream2.write(arrayOfByte3, 0, 20);
      i += 40;
    }

    String str = StripPadding(localByteArrayOutputStream2.toByteArray());
    return str;
  }

  private byte[] StringToBytes(String paramString)
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();

    byte[] arrayOfByte = paramString.getBytes();
    int i = paramString.length();

    localByteArrayOutputStream.write(arrayOfByte, 0, i);
    localByteArrayOutputStream.write(0);

    int j = 40 - ((i + 1) % 40);
    arrayOfByte = new byte[j];
    sr.nextBytes(arrayOfByte);
    localByteArrayOutputStream.write(arrayOfByte, 0, j);
    return localByteArrayOutputStream.toByteArray();
  }

  private String StripPadding(byte[] paramArrayOfByte)
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    for (int i = 0; (i < paramArrayOfByte.length) && (paramArrayOfByte[i] != 0); ++i)
      localByteArrayOutputStream.write(paramArrayOfByte[i]);
    return new String(localByteArrayOutputStream.toByteArray());
  }
}