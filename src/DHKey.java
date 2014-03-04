import java.util.Date;
import java.math.*;
import java.io.*;

/*
 * This object is used for Public Key Exchange.
 * The Crypto routines require it.  I haven't put the heavy
 * duty methods in here because I want it to stay small
 */

class DHKey implements Serializable {

    private static final long serialVersionUID = 1L;

    BigInteger p, g; /* These two make up the public Key */

    String Description;
    Date created;

    DHKey(BigInteger P, BigInteger G, String what) {

        p = P;
        g = G;

        Description = what;
        created = new Date();
    }

    public String toString() {

        StringBuffer scratch = new StringBuffer();
        scratch.append("Public Key(p): " + p.toString(32) + "\n");
        scratch.append("Public Key(g): " + g.toString(32) + "\n");
        scratch.append("Description: " + Description + "\n");
        scratch.append("Created: " + created);
        return scratch.toString();

    }

}

