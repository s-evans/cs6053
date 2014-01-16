/*
 * rmi_test.java
 *
 * Created on March 14, 2004, 3:19 PM
 *
 * @author  sshary
 */
import java.rmi.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class rmi_test implements CertRemote {
   public rmi_test() {
      System.out.println("\n************************************************");
      System.out.println("Starting RMI test for monitor's certificate\n\n");
      PlayerCertificate certMonitor = getCert("MONITOR");
      System.out.println("Non-empty RMI object received");
      System.out.println("RMI object name: " + certMonitor.getPlayerName());
      System.out.println("RMI object certificate: " + 
			 certMonitor.getCertificate());
      System.out.println("\n************************************************");
   }
   
   public static void main(String[] args) {  new rmi_test();  }
    
   public PlayerCertificate getCert(String playerName) {
      try {
	 System.out.println("Creating CertRemote object thru RMI lookup...");
	 String destin = "rmi://helios.ececs.uc.edu/CertRegistry";
	 CertRemote remoteCertGiver = (CertRemote)(Naming.lookup(destin));
	 System.out.println("GOOD: CertRemote created or bounded or "+
			    "whatever through RMI");
            
	 System.out.println("Checking the validity of the object we "+
			    "(supposed) get...");
	 if (remoteCertGiver == null) {
	    System.out.println("NOT GOOD: the instantiated object is null");
	    System.out.println("VERY NOT GOOD: YOUR SCREWED!!!  haha");
	    System.exit(0);
	 } else {
	    System.out.println("GOOD: we got something NOT null... a "+
			       "good start");
	 }
            
	 System.out.println("Getting the objects declared methods to make "+
			    "sure we are");
	 System.out.println("using the class object correctly...");
	 java.lang.reflect.Method methods[] = 
	    remoteCertGiver.getClass().getDeclaredMethods();
	 for (int i = 0; i < methods.length; i++) {
	    System.out.println("Method name is: " + methods[i].getName());
	    Class classInputs[] = methods[i].getParameterTypes();
	    for (int j = 0; j < classInputs.length; j++)
	       System.out.println("    Parameter[" + j + "] is: " + 
				  classInputs[j].getName());
	 }
            
	 PlayerCertificate certMonitor;
	 if ((certMonitor = remoteCertGiver.getCert(playerName)) == null) {
	    System.out.println("NOT GROOD:Error getting object from the "+
			       "CertRemote");
	    System.out.println("NOT GROOD:Here is what you get: " + 
			       remoteCertGiver.getCert(playerName).toString());
	    System.exit(0);
	 } else
	    System.out.println("GROOD: Getting player certificate through "+
			       "RMI Object");
            
	 System.out.println(">>>"+certMonitor.getPlayerName());
	 System.out.println("GROOD: Object retreived, returning object.");
	 PubRSA key = (PubRSA)certMonitor.getPublicKey();
	 System.out.println("Exp="+key.getExponent()+" Mod="+key.getModulus());
	 return certMonitor;
      }
      catch(Exception e) {
	 System.out.println("ERROR BEGIN");
	 System.out.print("ERROR: ");
	 e.printStackTrace();
	 System.out.println("ERROR END");
	 
      }// end of catch
      return null;
   }// end of method getCert(...)    
}
