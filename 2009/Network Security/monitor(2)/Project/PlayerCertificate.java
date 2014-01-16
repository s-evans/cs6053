/****************************************************************
  * PlayerCertificate - A certificate identity for the player,  *
  * stored at the monitor's database.                           *
  ***************************************************************/
/*
   Written by: Coleman Kane <cokane@cokane.org>
   
   Written For: Dr. John Franco, University of Cincinnati ECECS dept.
                20-ECES-653: Network Security

   Copyright(c): 2003, by Coleman Kane
   
   $Id: PlayerCertificate.java,v 1.1 2009/04/22 11:21:11 franco Exp $

   $Log: PlayerCertificate.java,v $
   Revision 1.1  2009/04/22 11:21:11  franco
   *** empty log message ***

   Revision 1.1.1.1  2008/04/16 13:16:50  franco


   Revision 1.3  2003/11/23 08:08:09  cokane
   Marked up for javadoc

   Revision 1.2  2003/11/23 08:03:46  cokane
   Fixed Serializeable --> Serializable

   Revision 1.1  2003/11/23 07:45:59  cokane
   Initial revision


*/
import java.math.BigInteger;
import java.io.Serializable;

public class PlayerCertificate implements Serializable {
	private BigInteger h;
	private String playerName;

	/** Load the player certificate by taking the supplied number,
	  corresponding to the CA-Signed SHA-1 hash of the named player's
	  public key, also take as a String the player's Name. **/
	public PlayerCertificate(BigInteger hash, String name) {
		h = hash;
		playerName = name;
	}

	/** Returns the registered name of the player to which this certificate has
	    been assigned. */
	public String getPlayerName() {
		return playerName;
	}
	
	/** Returns the cetrificate itself, as a base32 number string. */
	public String getCertificate() {
		return h.toString(32);
	}
}
