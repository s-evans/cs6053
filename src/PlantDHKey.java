import java.math.*;
import java.io.*;

public class PlantDHKey {
    public static void plantKey() throws IOException {
        BigInteger p = new BigInteger(
                "7897383601534681724700886135766287333879367007236994792380151951185032550914983506148400098806010880449684316518296830583436041101740143835597057941064647");

        BigInteger g = new BigInteger(
                "2333938645766150615511255943169694097469294538730577330470365230748185729160097289200390738424346682521059501689463393405180773510126708477896062227281603");

        DHKey key = new DHKey(p, g, "C653 DH key");

        FileOutputStream fos = new FileOutputStream("DHKey");

        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(key);
        
        oos.close();
    }
}
