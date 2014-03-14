import java.math.BigInteger;
import java.security.MessageDigest;

public class SHA {
    static public String perform(String input) throws Exception {
        /*
         * This method is: Copyright(C) 1998 Robert Sexton. Use it any way you
         * wish. Just leave my name on.
         */

        MessageDigest md;
        byte target[];

        md = MessageDigest.getInstance("SHA");
        target = input.toUpperCase().getBytes();
        md.update(target);

        return (new BigInteger(1, md.digest())).toString(16);
    }
}
