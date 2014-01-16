import java.io.*;
import java.util.Enumeration;

class ReadPlayerDB {
   PlayerDB playerDB;

   ReadPlayerDB(String filename) {
      playerDB = null;
      try {
         FileInputStream fis = new FileInputStream(filename);
         ObjectInputStream ois = new ObjectInputStream(fis);
         playerDB = (PlayerDB) ois.readObject();
      } catch (Exception e) {
         System.out.println("CANNOT READ DB: " + e);
      }
   }

   void processPlayers() {
      byte b1='0', b2='0';
      SillyPlayerWrapper sp = 
         new SillyPlayerWrapper("FOO", playerDB.getEconomy());

      try {
	 FileOutputStream fos = new FileOutputStream("pass_and_cook.txt");
	 PrintWriter pw = new PrintWriter(fos);


	 for (Enumeration e = playerDB.getPlayers() ; e.hasMoreElements() ; ) {
	    Player curPlayer = (Player) e.nextElement();
	    System.out.println(curPlayer.getIdentity() + " " + 
			       sp.getPlayerPassword(curPlayer) + " " +
			       curPlayer.getMonitorPassword());
	    String pass_str = sp.getPlayerPassword(curPlayer);
	    String cook_str = curPlayer.getMonitorPassword();
	    char pass_out[] = null, cook_out[] = null;
	    if (pass_str != null) pass_out = pass_str.toCharArray();
	    if (cook_str != null) cook_out = cook_str.toCharArray();
	    pw.print(curPlayer.getIdentity()+"\n   password="+
		     sp.getPlayerPassword(curPlayer));
	    pw.print("\n   password=");
	    if (pass_str != null) {
	       try {
		  b1 = (byte)pass_out[0];
		  for (int i=1 ; ; i++) {
		     b2 = (byte)pass_out[i];
		     pw.print(b1);
		     pw.print('+');
		     b1 = b2;
		  }
	       } catch (Exception f) { pw.print(b1); }
	    } else {
	       pw.print("<null>");
	    }
	    pw.print("\n   cookie="+curPlayer.getMonitorPassword());
	    pw.print("\n   cookie=");
	    if (cook_str != null) {
	       try {
		  b1 = (byte)cook_out[0];
		  for (int i=1 ; ; i++) {
		     b2 = (byte)cook_out[i];
		     pw.print(b1);
		     pw.print("+");
		     b1 = b2;
		  }
	       } catch (Exception f) { pw.print(b1); }
	    } else {
	       pw.print("<null>");
	    }
	    pw.println();
	 }
	 pw.close();
      } catch (Exception e) { System.out.println("1:"+e.toString()); }
      

//          PrintStream file = null;
//          try
//          {
//             FileOutputStream f = new FileOutputStream(
//                           GameParameters.STATISTICS_FILE);

//             file = new PrintStream(f);

//          }
//          catch (IOException ioe)
//          {
//             System.out.println("UNABLE TO OPEN STATISTICS LOG!");
//          }

   }

   public static void main(String argv[]) {
      ReadPlayerDB rpd = new ReadPlayerDB(argv[0]);
      rpd.processPlayers();
   }
}
