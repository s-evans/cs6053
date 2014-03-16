// this file was retrieved from http://gauss.ececs.uc.edu/Courses/c6053/hwhints/ZKP/ZKPTest.java
// on 2014-Mar-2.  i believe it was originally written by Tim Rapp and his group.
// it has been adapted for use by our team.

import java.security.*;
import java.security.spec.*;
import java.util.*;
import java.math.*;

class Sender {
    public BigInteger mV;
    public BigInteger mN;
    public int mRounds;
    public BigInteger[] mRR;
    public boolean mCheck = true;
    public int[] mSubsetA;

    public Sender(int rounds) throws Exception {
        mRounds = rounds;
    }

    public void setPublicKey(String v, String n) {
        mV = new BigInteger(v);
        mN = new BigInteger(n);
    }

    public int getRounds() {
        return mRounds;
    }

    public void setAuthorizeSet(String[] set) {
        // Create a new array for these values
        mRR = new BigInteger[mRounds];

        // Copy the array
        for (int i = 0; i < mRounds; i++) {
            mRR[i] = new BigInteger(set[i]);
        }
    }

    public String[] getSubsetA() {
        int setSize = mRounds / 2;
        int maxStep = setSize / 5;
        int lastVal = 0;
        Random rnd = new Random();
        String[] strAr = new String[setSize]();

        mSubsetA = new int[setSize]();

        // Create a set of increasing numbers as indexes into auth set, the set size of which is no larger than the number of rounds
        for (int i = 0; i < setSize; i++) {
            // Make sure we have enough room for all numbers
            int modulo = mRounds - (setSize - i) - lastValue;

            // Keep integer indexes from inflating too quickly 
            modulo = (modulo > maxStep ? maxStep : modulo);

            // Get a random index
            int val = lastVal + rnd.nextInt(modulo) + 1;

            // Convert to a string and store in our array
            strAr[i] = val.toString();

            // Record the indexes we're sending for future reference
            mSubsetA[i] = val;

            // Record this value for the next iteration
            lastVal = val;
        }

        return strAr;
    }

    public boolean checkSubsetK(String[] set) {
        int setSize = mRounds / 2;

        // Validate that the sizes match
        if ( setSize != set.size() ) {
            System.out.println("Subset K set size validation failed!");
            return false;
        }

        for (int i = 0; i < setSize; i++) {
            // Use the current set index to get the index into the Auth Set
            int j = mSubsetA[i];

            // Do the math ourselves
            BigInteger a1 = mRR[j].multiply(mV).mod(mN);

            // Get the value given to us
            BigInteger a2 = new BigInteger(set[i]);

            // Compare 
            if (!a1.equals(a2)) {
                mCheck = false;
                return false;
            }
        }

        return true;
    }

    // TODO: Validate that the initiator is doing the right things as well....
    // TODO: Validate that the sender is doing the right things
    // TODO: Checkout the assignment and hints

    public boolean checkSubsetJ(String[] set) {
        int setSize = mRounds / 2;

        // Validate that the sizes match
        if ( setSize != set.size() ) {
            System.out.println("Subset J set size validation failed!");
            return false;
        }

        for (int i = 1; i < mRounds; i += 2) {
            BigInteger a1 = mRR[i];
            BigInteger a2 = new BigInteger(set[i]);
            
            // Compare values
            if (!a1.equals(a2)) {
                mCheck = false;
                return false;
            }
        }

        return true;
    }

    public String response() {
        if (mCheck) {
            return "ACCEPT";
        } else {
            return "DECLINE";
        }
    }
}
