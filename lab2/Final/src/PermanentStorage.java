import java.io.*;
import java.util.*;

public class PermanentStorage {
    String identity, filename;
    BufferedReader fIn = null;
    PrintWriter fOut = null;
    static String ResourceFileName = "Resources.dat";
    MessageParser mp = null;
    StringTokenizer t;

    public PermanentStorage(MessageParser mp) {
        filename = "passwd.dat";
        this.mp = mp;
    }

    public PermanentStorage(MessageParser mp, String identity) {
        this.identity = identity;        
        filename = identity + ".dat";
        this.mp = mp;
    }
    
    public RSA ReadKey(){
        RSA key = null;        
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(this.identity + ".key"));
            key = (RSA) ois.readObject();
            ois.close();
        } catch (IOException ex) {
            // this is cool - means we have not used this user before
        } catch (ClassNotFoundException ex){
            ex.printStackTrace();
        } 
        
        return key;
    }
    
    public void WriteKey(RSA key){        
        try {
            File f = new File(this.identity + ".key");
            if(f.exists()){
                f.delete();
            }
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.identity + ".key"));
            oos.writeObject(key);
            oos.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
    

    // Write Personal data such as Password and Cookie
    public boolean  WritePersonalData(String Passwd,String Cookie)
    {
        boolean success = false;
        PrintWriter pout = null;
        try
        {
            pout = new PrintWriter(new FileWriter(filename));
            if ( pout == null ) {
                return false;
            }
            pout.println("PASSWORD");
            if ( (Passwd != null) && !(Passwd.equals("")) )
            {
                pout.println(Passwd);
            }
            if ( (Cookie != null) && !(Cookie.equals("")) )
            {
                pout.println("COOKIE");
                pout.flush();
                pout.println(Cookie);
                pout.flush();
            }
            pout.close();
            success = true;
        }
        catch ( IOException e )
        {
            if ( pout != null )
            {
                pout.close();
            }
            return success;
        }
        catch ( NumberFormatException n )
        {
        	n.printStackTrace();
        }
        System.out.println("Wrote dat file: " + Passwd + " " + Cookie);
        return success;
    }

    public void SaveResources(String res) throws IOException {
        System.out.println("MessageParser [SaveResources]:");
        try  // If an error occurs then don't update the Resources File
        {
            String temp = mp.GetNextCommand (res, "COMMAND_ERROR");
            if ( (temp == null) || (temp.equals("")) )
            {
                fOut = new PrintWriter(new FileWriter(ResourceFileName));
                t = new StringTokenizer(res," :\n");
                try
                {
                    temp = t.nextToken();
                    temp = t.nextToken();
                    temp = t.nextToken();
                    System.out.println("MessageParser [SaveResources]: got "+
                                       "token before write: "+temp);
                    for ( int i=0 ; i < 20 ; i++ )
                    {
                        fOut.println(temp);
                        fOut.flush();
                        temp = t.nextToken();
                    }
                }
                catch ( NoSuchElementException ne )
                {
                    temp = "";
                    fOut.close();
                }
            }
            fOut.close();
        }
        catch ( IOException e )
        {
            fOut.close();
        }
    }
}
