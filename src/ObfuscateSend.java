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

    /**
     * Obfuscates a command by mangling the command string and adding log output
     *
     * @param command Full single command line to be sent to the monitor
     *
     * @return obfuscated command string that should be processed identically by the monitor
     */
    public static String ObfuscateCommandOutput(String command, String[] fakeOutputCommand, boolean active) {
        if ( !active ) {
            return command;
        }

        return command + "\n" + GenerateOutputGroup(fakeOutputCommand);
    }

    public static String ObfuscateCommandInput(String command, String[] fakeInputCommand, boolean active) {
        if ( !active ) {
            return command;
        }
             
        return command + "\n" + GenerateInput(fakeInputCommand);
    }

    // Joins a string array using spaces
    private static String join (String[] array, String delimit) {
        if (array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        int i;
        for ( i = 0 ; i < array.length - 1 ; i++ ) {
            sb.append(array[i]+delimit);
        }
        return sb.toString() + array[i];
    }

    private static String GeneratePlaintextOutput(String[] in) {
        return "outgoing [plaintext] >>>" + join(in, " "); 
    }

    private static String GenerateEncryptedOutput(int plaintextLen) {
        Random rand = new Random();
        int PAD_SIZE = 40;
        int cipherTextLength = ((plaintextLen + 1 + PAD_SIZE - 1) / PAD_SIZE) * PAD_SIZE;
        byte[] byteArray = new byte[cipherTextLength+1];

        rand.nextBytes(byteArray);
        byteArray[0] = 42; // Set guard byte

        return "outgoing [encrypted] >>>" + (new BigInteger(byteArray).toString(32));
    }

    private static String GenerateOutputGroup(String[] in) {
        String out = GeneratePlaintextOutput(in) + "\n" + GenerateEncryptedOutput(join(in, " ").length());
        return out;
    }

    private static String GenerateInput(String[] in) {
        return "incoming [decrypted] >>>" + join(in, " ");
    }

}
