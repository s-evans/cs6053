import java.util.StringTokenizer;

public class BetterStringTokenizer extends StringTokenizer {

    public BetterStringTokenizer(String arg)
    {
        super(arg);
    }

    public BetterStringTokenizer(String arg1, String arg2)
    {
        super(arg1, arg2);
    }

    public String GetRemaining()
    {
        int tokenCount = countTokens();
        String remainingTokens = "";
        if ( tokenCount <= 0 ) {
            return null;
        }
        
        for (int i = 0; i < tokenCount; i++ ) {
            remainingTokens = remainingTokens.concat(nextToken()).concat(" ");
        }
        return remainingTokens.trim();
    }

}
