// this file was retrieved from http://gauss.ececs.uc.edu/Courses/c6053/hwhints/ZKP/ZKPTest.java
// on 2014-Mar-2.  i believe it was originally written by Tim Rapp and his group.
// it has been adapted for use by our team.

import java.security.*;
import java.security.spec.*;
import java.util.*;
import java.math.*;
import java.util.List;
import java.util.ArrayList;

class Initiator {
    protected BigInteger mSqr = new BigInteger("2", 10);
    protected BigInteger mV;
    protected BigInteger mS;
    protected BigInteger mN;
    protected int mRounds;
    protected BigInteger[] mR;
    protected int[] mA;
    protected int mSubsetASize;
    protected BigInteger[] mK;
    protected BigInteger[] mJ;

    public Initiator() throws Exception {
        // Generate a 512-bit RSA key pair
        KeyFactory kf = KeyFactory.getInstance("RSA");
        KeyPairGenerator kpgRSA = KeyPairGenerator.getInstance("RSA");
        kpgRSA.initialize(512);
        KeyPair kpKeyPair = kpgRSA.genKeyPair();
        RSAPublicKeySpec x = kf.getKeySpec(kpKeyPair.getPublic(), RSAPublicKeySpec.class);

        // Grab the modulus
        mN = x.getModulus();

        // Returns a random 512 bit BigInteger
        mS = new BigInteger(512, new SecureRandom());

        // Now we have our public value
        mV = mS.pow(2).mod(mN);
    }

    public BigInteger getV() {
        return mV;
    }

    public BigInteger getN() {
        return mN;
    }

    public void setRounds(int rounds) {
        mRounds = rounds;
    }

    public String[] getAuthorizeSet() {
        // Create some junk
        Random rnd = new Random();
        mR = new BigInteger[mRounds];
        
        // Create a new collection class
        List<String> set = new ArrayList<String>();

        // Generate a random set
        for (int i = 0; i < mRounds; i++) {
            // Create a random big int
            mR[i] = new BigInteger(256, rnd);

            // Do some math and populate the collection class
            set.add(mR[i].modPow(mSqr, mN).toString());
        }

        // Return the collection class
        return set.toArray(new String[set.size()]);
    }

    public void setSubsetA(String[] set) {
        // Get length of the set
        mSubsetASize = set.length;

        // Create an array to hold the set
        mA = new int[mSubsetASize];

        // Populate the array
        for (int i = 0; i < mSubsetASize; i++) {
            mA[i] = Integer.parseInt(set[i]);
        }
    }

    public String[] getSubsetK() {
        // Create a new collection class
        List<String> set = new ArrayList<String>();

        // Populate the collection class 
        for (int i = 0; i < mSubsetASize; i++) {
            set.add(mR[mA[i]].multiply(mS).modPow(mSqr, mN).toString());
        }

        // Return the collection class
        return set.toArray(new String[set.size()]);
    }

    public String[] getSubsetJ() {
        // Create a new collection class
        List<String> set = new ArrayList<String>();

        for (int i = 0; i < mRounds; i++) {
            int j = 0;

            for (; j < mSubsetASize; j++) {
                if (mA[j] == i) {
                    break;
                }
            }

            if (j != mSubsetASize) {
                continue;
            }

            // Do some math and populate the collection
            set.add(mR[i].modPow(mSqr, mN).toString());
        }

        return set.toArray(new String[set.size()]);
    }
}
