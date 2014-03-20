import java.math.BigInteger;
import java.util.Random;

/**
 * Implements obfuscation to hide the actual output of a command
 * <p/>
 * Commands are interpreted by calling to tokenizeIncoming which uses a StringTokenizer. This tokenizes based on
 * " \t\r\n\f". Because the tokenized line is retrieved with BufferedReader.readline(), only
 * " \t\f" would not be possible characters for tokenizing. Consecutive tokens are treated as a single token.
 * <p/>
 * We could also send pretty much anything in a command including output that looked like the output of other commands.
 * After encryption has been established, then there exists the possibility for adding any string to the output
 * including newlines. This could make it impossible to distinguish real output (esp IDENT commands) vs our output.
 * If people are attempting passwords from IDENT commands then we could force them to exhaust their commands/hour quota
 */
public class ObfuscateSend {
    private Random rand = new Random();
    private static final char[] CLEARTEXT_ARG_SEPARATORS = {' ', '\t', '\f'};
    private static final char[] ENCRYPTED_ARG_SEPARATORS = {' ', '\t', '\f', '\r', '\n'};
    private static final int MAX_ARG_SEPARATORS = 5;

    public static String ObfuscateCommand(String commandWithArgs, boolean encrypted) {
        return ObfuscateCommand(commandWithArgs.split(" "), encrypted);
    }

    // TODO: Update this class to output junk that is more meaningful

    /**
     * Obfuscates a command by mangling the command string and adding log output
     * @param commandWithArgs Command (e.g. IDENT) and any applicable arguments
     * @param encrypted True if the command will be sent in an encrypted form
     * @return obfuscated command string that should be processed identically by the monitor
     */
    public static String ObfuscateCommand(String[] commandWithArgs, boolean encrypted) {
        String command = ObfuscateCommandNone(commandWithArgs, encrypted);
        if (encrypted) {
            String additionalLine = "\n" + GenerateInput("TESTCOMMAND_LINE1", true) + "\n";
            additionalLine += GenerateInput("TESTCOMMAND_LINE2", true) + "\n";
            additionalLine += GenerateInput("TESTCOMMAND_LINE3", true) + "\n";
            additionalLine += GenerateEncryptedOutput(24) + "\n";
            additionalLine += GenerateInput("TESTCOMMAND_LINE5", true) + "\n";
            return command + additionalLine;
        }
        return command;
    }

    /**
     * Perform no obfuscation
     * @param commandWithArgs Command (e.g. IDENT) and any applicable arguments
     * @param encrypted True if the command will be sent in an encrypted form
     * @return obfuscated command string that should be processed identically by the monitor
     */
    private static String ObfuscateCommandNone(String[] commandWithArgs, boolean encrypted) {
        // Null obfuscation is to just join everything with spaces
        int i;
        StringBuilder stringBuilder = new StringBuilder();
        for (i = 0; i < commandWithArgs.length -1; i++) {
            stringBuilder.append(commandWithArgs[i]).append(" ");
        }
        // Add on the last one without any trailing space
        stringBuilder.append(commandWithArgs[i]);

        return stringBuilder.toString();
    }

    /**
     * Adds random amounts of valid spaces between the command
     * @param commandWithArgs Command (e.g. IDENT) and any applicable arguments
     * @param encrypted True if the command will be sent in an encrypted form
     * @return obfuscated command string that should be processed identically by the monitor
     */
    private static String ObfuscateCommandSeparators(String[] commandWithArgs, boolean encrypted) {
        int i;
        int numSeparators;
        char separator;
        Random rand = new Random();
        StringBuilder stringBuilder = new StringBuilder();

        for (i = 0; i < commandWithArgs.length; i++) {
            stringBuilder.append(commandWithArgs[i]);
            // Add a random number of separators
            for (numSeparators = rand.nextInt(MAX_ARG_SEPARATORS) + 1; numSeparators > 0; numSeparators--) {
                if (encrypted) {
                    separator = ENCRYPTED_ARG_SEPARATORS[rand.nextInt(ENCRYPTED_ARG_SEPARATORS.length)];
                } else {
                    separator = CLEARTEXT_ARG_SEPARATORS[rand.nextInt(CLEARTEXT_ARG_SEPARATORS.length)];
                }
                stringBuilder.append(separator);
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Randomizes the casing of the supplied string
     *
     * This only currently works for ASCII encoded strings.
     *
     * @param input String to be randomized
     * @return string with the same characters but randomized in casing
     */
    private static String RandomizeStringCase(String input) {
        int caseInversion = 'A' - 'a';
        char[] charArray = input.toCharArray();
        Random rand = new Random();
        // At random invert the character case
        for (int i = 0; i < charArray.length; i++ ) {
            if (rand.nextBoolean()) {
                charArray[i] = (char)((int)charArray[i] ^ caseInversion);
            }
        }

        return new String(charArray);
    }

    /**
     * Generate random log strings that can be added to an encrypted command to be added to the output
     */


    /**
     * Generates a valid looking encrypted lines. They will be padded to the appropriate length to encompass a
     * plaintext message of the given length
     * @param plaintextLen Number of characters of plaintext that should be in the encrypted output
     * @return Valid looking encrypted string with the server "outgoing [encrypted] >>>" prefix
     */
    private static String GenerateEncryptedOutput(int plaintextLen) {
        Random rand = new Random();
        int PAD_SIZE = 40;
        int cipherTextLength = ((plaintextLen + 1 + PAD_SIZE -1 ) / PAD_SIZE) * PAD_SIZE;
        byte[] byteArray = new byte[cipherTextLength+1];

        rand.nextBytes(byteArray);
        byteArray[0] = 42; // Set guard byte

        return "outgoing [encrypted] >>>" + (new BigInteger(byteArray).toString(32));
    }

    /**
     * Returns a command-looking input string
     * @param command Type of command to generate
     * @param encrypted If the command was sent encrypted
     * @return String of input
     */
    private static String GenerateInput(String command, boolean encrypted) {
        // TODO: Make this look more like a real version of this command
        return "incoming [decrypted] >>>" + command + " This is just a test";
    }

}
