// this file was retrieved from http://gauss.ececs.uc.edu/Courses/c6053/hwhints/ZKP/ZKPTest.java
// on 2014-Mar-2.  i believe it was originally written by Tim Rapp and his group.
// it has been adapted for use by our team.

import java.security.*;
import java.security.spec.*;
import java.util.*;
import java.math.*;

class Sender {
    protected BigInteger mBigIntTwo = new BigInteger("2", 10); // = 2
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

    public void setAuthorizeSet(String[] set) throws Exception {
        // Validate the set size
        if ( set.length != mRounds ) {
            throw new Exception("Set size unexpected; exp = " + mRounds + "; act = " + set.length + ";");
        }

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
        String[] strAr = new String[setSize];

        mSubsetA = new int[setSize];

        // Create a set of increasing numbers as indexes into auth set, the set size of which is no larger than the number of rounds
        for (int i = 0; i < setSize; i++) {
            // Make sure we have enough room for all numbers
            int modulo = mRounds - (setSize - i) - lastVal;

            // Keep integer indexes from inflating too quickly 
            modulo = (modulo > maxStep ? maxStep : modulo);

            // Get a random index
            int val = lastVal + rnd.nextInt(modulo) + 1;

            // Convert to a string and store in our array
            strAr[i] = Integer.toString(val);

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
        if ( setSize != set.length ) {
            System.out.println("Subset K set size validation failed!");
            return false;
        }

        for (int i = 0; i < setSize; i++) {
            // Do the math ourselves
            BigInteger a1 = mRR[mSubsetA[i]].multiply(mV).mod(mN);

            // Get the value given to us
            BigInteger a2 = new BigInteger(set[i]).modPow(mBigIntTwo, mN);

            // Compare 
            if (!a1.equals(a2)) {
                mCheck = false;
                return false;
            }
        }

        return true;
    }

    public boolean checkSubsetJ(String[] set) {
        int subsetASize = mSubsetA.length;
        int setSize = mRounds - subsetASize;

        // Validate that the sizes match
        if ( setSize != set.length ) {
            System.out.println("Subset J set size validation failed!");
            return false;
        }

        int j = 0; // Index into subset a
        int k = 0; // Index into incoming subset j
        for (int i = 0; i < mRounds; i++) {
            // Check if our current round index doesn't equal our current pointer into the Subset A array
            if ( j >= subsetASize || mSubsetA[j] != i ) {
                // If not in Subset A...
                BigInteger a1 = mRR[i]; 
                BigInteger a2 = new BigInteger(set[k]).modPow(mBigIntTwo, mN);
                
                // Compare values
                if ( !a1.equals(a2) ) {
                    mCheck = false;
                    return false;
                }

                k++;
                continue;
            }

            // If we've found an index in Subset A, skip past it
            j++;
        }

        return true;
    }

    public String response() {
        if (mCheck) {
            return "ACCEPTED";
        } else {
            return "DECLINED";
        }
    }
}
