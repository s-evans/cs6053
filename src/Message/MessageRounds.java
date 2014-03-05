public class MessageRounds extends Message {
    public Integer mRounds;

    public MessageRounds() {
        mRounds = 0;
    }

    public MessageRounds(int rounds) {
        mRounds = rounds;
    }

    public MessageRounds(String args) throws Exception {
        // Parse the input string 
        String[] vals = args.split(" ");
        if ( vals.length != 1 ) {
            throw new Exception(new String("PARSE ERROR"));
        }

        // Populate from values
        mRounds = Integer.parseInt(vals[0]);
    }

    public String directive() {
        return "ROUNDS";
    }

    public String serialize() {
        return directive().concat(sArgDelimit).concat(mRounds.toString());
    }
}
