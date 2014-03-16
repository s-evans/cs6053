import java.io.*;

public class IdentFile {
    protected static final String sFolder = "users/";

    protected String mIdent; 
    protected String mPath;

    public String mPassword;
    public String mCookie;

    // CTOR
    public IdentFile (String ident) {
        mIdent = ident;
        mPath = sFolder + mIdent;
    }

    // Reads identity data from file
    public boolean Read() throws Exception {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(mPath));
            String line = bufferedReader.readLine();
            String[] identEntry = line.split("[:,]"); // passwords cannot contain ':' or ',' characters, or this will break

            // Validate length
            if ( identEntry.length != 6 ) {
                System.out.println("IdentFile [Read]: Invalid formatted line");
                return false;
            } 

            // Check to make sure that identity matches current identity
            if (identEntry[0].trim().equals("IDENT") && identEntry[1].trim().equals(mIdent)) {

                // Set the password if non-empty
                if (identEntry[2].trim().equals("PASSWORD")) {
                    String tempPassword = identEntry[3].trim();
                    if (tempPassword.length() != 0) {
                        System.out.println("IdentFile [Read]: Got password");
                        mPassword = tempPassword;
                    } else {
                        System.out.println("IdentFile [Read]: Zero length password");
                    }
                } else {
                    System.out.println("IdentFile [Read]: Password not found");
                }

                // Set the cookie if non-empty
                if (identEntry[4].trim().equals("COOKIE")) {
                    String tempCookie = identEntry[5].trim();
                    if (tempCookie.length() != 0) {
                        System.out.println("IdentFile [Read]: Got cookie");
                        mCookie = tempCookie;
                    } else {
                        System.out.println("IdentFile [Read]: Zero length cookie");
                    }
                } else {
                    System.out.println("IdentFile [Read]: Cookie not found");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // Writes identity data to file
    public boolean Write() throws Exception {
        boolean success = false;
        PrintWriter pout;

        String Passwd = mPassword;
        String Cookie = mCookie;

        try {
            if (Passwd == null) {
                Passwd = "";
            }

            if (Cookie == null) {
                Cookie = "";
            }

            pout = new PrintWriter(new FileWriter(mPath));
            pout.printf("IDENT: %s, PASSWORD: %s, COOKIE: %s\n", mIdent, mPassword, mCookie);
            pout.flush();
            pout.close();

            success = true;
        } catch (IOException e) {
            System.out.println("MessageParser [WritePersonalData]: IOException:\n\t" + e + " " + this);
        }

        return success;
    }
}
