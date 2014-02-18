import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.util.Arrays;

public class Karn {
    private final int RADIX_SIZE_BYTES = 32;
    private final int PADSIZE_BYTES = 40;
    private final int HALF_BLOCK_SIZE_BYTES = PADSIZE_BYTES / 2;
    private final int GUARD_VALUE = 42;

    private byte key[];
    private byte key_left[];
    private byte key_right[];

    static SecureRandom sr = null;  // This is expensive.  We only need one
    static MessageDigest md = null; // This will be shared.

    /**
     * Initializes a new Karn encryption class
     * Creates a random source of SecureRandom and defines a SHA message digest
     *
     * @param bi Key represented as a BigInteger
     */
    public Karn(BigInteger bi) {
        if (sr == null) sr = new SecureRandom();
        key = bi.toByteArray();

        // Digest encryption needs keys split into two halves
        key_left = new byte[key.length / 2];
        key_right = new byte[key.length / 2];

        for (int i = 0; i < key.length / 2; i++) {
            key_left[i] = key[i];
            key_right[i] = key[i + key.length / 2];
        }

        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Yow! NoSuchAlgorithmException. Abandon all hope");
        }
    }

    /**
     * Encrypt the string using the karn algorithm
     *
     * @param plaintext Plaintext string to be encrypted
     * @return base32 encoded encrypted string with guard byte and padding
     */
    public String encrypt(String plaintext) {
        byte[] plain_left, plain_right;
        byte[] ciph_left, ciph_right;
        byte[] digest;

        // These buffers are used for the encryption.
        byte input[] = StringToBytes(plaintext); // Pad the string

        plain_left = new byte[HALF_BLOCK_SIZE_BYTES];
        plain_right = new byte[HALF_BLOCK_SIZE_BYTES];

        ciph_left = new byte[HALF_BLOCK_SIZE_BYTES];
        ciph_right = new byte[HALF_BLOCK_SIZE_BYTES];

        digest = new byte[HALF_BLOCK_SIZE_BYTES];  // Temp storage for the hash

        // Our pointer into the workspace
        int cursor = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Guard Byte for the ciphertext
        out.write(GUARD_VALUE);

        while (cursor < input.length) {
            // Copy the next slab into the left and right
            // TODO: This could be optimized with array copies
            for (int i = 0; i < HALF_BLOCK_SIZE_BYTES; i++) {
                plain_left[i] = input[cursor + i];
                plain_right[i] = input[cursor + HALF_BLOCK_SIZE_BYTES + i];
            }

            // Hash the left plaintext with the left key
            md.reset(); // Start the hash fresh
            md.update(plain_left);
            md.update(key_left);
            digest = md.digest(); // Get out the digest bits
            // XOR the digest with the right plaintext for the right c-text
            // Right half
            for (int i = 0; i < HALF_BLOCK_SIZE_BYTES; i++)
                ciph_right[i] = (byte) (digest[i] ^ plain_right[i]);

            // Now things get a little strange
            md.reset();
            md.update(ciph_right);
            md.update(key_right);
            digest = md.digest();
            for (int i = 0; i < HALF_BLOCK_SIZE_BYTES; i++)
                ciph_left[i] = (byte) (digest[i] ^ plain_left[i]);

            out.write(ciph_left, 0, HALF_BLOCK_SIZE_BYTES);
            out.write(ciph_right, 0, HALF_BLOCK_SIZE_BYTES);
            cursor += PADSIZE_BYTES;
        }
        BigInteger bi_out = new BigInteger(out.toByteArray());
        return (bi_out.toString(RADIX_SIZE_BYTES));
    }

    /**
     * Decrypt the ciphertext by running Karn in reverse
     * Takes a base32 encoded stream with guard byte and padding
     * Outputs the decrypted plaintext
     */
    public String decrypt(String ciphertext) {
        BigInteger bi;
        byte input[];
        byte[] plaintTextLeft = new byte[HALF_BLOCK_SIZE_BYTES];
        byte[] paintTextRight = new byte[HALF_BLOCK_SIZE_BYTES];
        byte[] ciphLeft = new byte[HALF_BLOCK_SIZE_BYTES];
        byte[] ciphRight = new byte[HALF_BLOCK_SIZE_BYTES];
        byte[] digest;
        int pos = 0;

        // Convert to a BigInteger, extract the bytes
        bi = new BigInteger(ciphertext, RADIX_SIZE_BYTES);
        input = bi.toByteArray();

	    // Remove guard value from beginning
        ByteArrayOutputStream scratch = new ByteArrayOutputStream();
        scratch.write(input,1,input.length - 1);
        input = scratch.toByteArray();

        // Start decryption
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        //Decryption is a mirror of the encryption code
        while (pos < input.length) {
            // Copy the next slab into the left and right
            // TODO: This could be optimized with array copies
            for (int i = 0; i < HALF_BLOCK_SIZE_BYTES; i++) {
                ciphLeft[i] = input[pos + i];
                ciphRight[i] = input[pos + HALF_BLOCK_SIZE_BYTES + i];
            }

            md.reset();
            md.update(ciphRight);
            md.update(key_right);

            digest = md.digest();
            for (int i = 0; i < HALF_BLOCK_SIZE_BYTES; i++) {
                plaintTextLeft[i] = (byte) (digest[i] ^ ciphLeft[i]);
            }

            md.reset();
            md.update(plaintTextLeft);
            md.update(key_left);
            digest = md.digest();
            for (int i = 0; i < HALF_BLOCK_SIZE_BYTES; i++) {
                paintTextRight[i] = (byte) (digest[i] ^ ciphRight[i]);
            }

            out.write(plaintTextLeft, 0, HALF_BLOCK_SIZE_BYTES);
            out.write(paintTextRight, 0, HALF_BLOCK_SIZE_BYTES);
            pos += PADSIZE_BYTES;
        }

        return StripPadding(out.toByteArray());
    }

    /**
     * Pads the plaintext string to a blocklength per the Karn specification
     */
    private byte[] StringToBytes(String input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte scratch[];

        scratch = input.getBytes();
        int len = input.length();

        //The following adds the null byte per the Karn algorithm
        buffer.write(scratch, 0, len);
        buffer.write(0);

        int padLength = PADSIZE_BYTES - (buffer.size() % PADSIZE_BYTES);
        scratch = new byte[padLength];
        sr.nextBytes(scratch);

        buffer.write(scratch, 0, padLength);

        return (buffer.toByteArray());
    }

    /**
     * Removes the padding from the decrypted plaintext and returns a string
     */
    private String StripPadding(byte input[]) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int i = 0;

        // TODO: This could maybe done faster. Padding should only be on the last block.
        while (input[i] != 0 && i < input.length) {
            buffer.write(input[i]);
            i++;
        }

        return (new String(buffer.toByteArray()));
    }
    
    public static void main(String[] args) {
    	// Validate that encryption and decryption are consistent
    	String plaintext = null;
    	String ciphertext = null;
        String decrypted = null;
    	Karn karn = null;
    	BigInteger key;
        String testDescr;
        int i;

        testDescr = "Static key and a short plaintext of \"test\"";
        key = new BigInteger("123456789", 16);
        plaintext = "test";
        karn = new Karn(key);
        ciphertext = karn.encrypt(plaintext);
        decrypted = karn.decrypt(ciphertext);
        if (plaintext.equals(decrypted)) {
            System.out.printf("Passed: %s\n", testDescr);
        } else {
            System.out.printf("Failed: %s\n", testDescr);
        }

        testDescr = "Static key and a long plaintext of \"The quick brown fox jumped over the lazy dog. The five boxing wizards jump quickly\"";
        key = new BigInteger("123456789", 16);
        plaintext = "The quick brown fox jumped over the lazy dog. The five boxing wizards jump quickly";
        karn = new Karn(key);
        ciphertext = karn.encrypt(plaintext);
        decrypted = karn.decrypt(ciphertext);
        if (plaintext.equals(decrypted)) {
            System.out.printf("Passed: %s\n", testDescr);
        } else {
            System.out.printf("Failed: %s\n", testDescr);
        }

        testDescr = "Static key with plaintext from 0 to 256 characters";
        key = new BigInteger("1234567890123456789012345678901234567890", 16);
        String origPlaintext = "";
        for (i=0; i < 256; i++) {
            origPlaintext = origPlaintext + ((char)('a' + i % 26));
        }
        karn = new Karn(key);

        for (i = 1; i < 256; i++) {
            plaintext = origPlaintext.substring(0, i);
            ciphertext = karn.encrypt(plaintext);
            decrypted = karn.decrypt(ciphertext);
            if (!plaintext.equals(decrypted)) {
                break;
            }
        }

        if ( i == 256 ) {
            System.out.printf("Passed: %s\n", testDescr);
        } else {
            System.out.printf("Failed: %s\n", testDescr);
        }

        System.out.println("Karn test finished");
    }
}
