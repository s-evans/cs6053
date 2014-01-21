/***************************************************************************
  * PubRSA - Store an RSA Public key, for distribution to other entities.  *
  **************************************************************************/
/*
  Written By: Coleman Kane <cokane@cokane.org>

  Written For: Dr. John Franco, University of Cincinnati ECECS Dept.
  			   20-ECES-653: Network Security

  Copyright(c): 2003, by Coleman Kane

  $Id: PubRSA.java,v 1.1 2008/05/18 14:48:30 franco Exp $

  $Log: PubRSA.java,v $
  Revision 1.1  2008/05/18 14:48:30  franco
  *** empty log message ***

  Revision 1.1.1.1  2008/04/03 01:33:08  franco


  Revision 1.1  2004/01/22 04:50:56  cokane
  Initial revision

  Revision 1.2  2003/11/25 23:17:37  cokane
  Add support to read the e,n values from the key.

  Revision 1.1  2003/11/23 07:46:41  cokane
  Initial revision


*/
import java.math.BigInteger;
import java.io.Serializable;

public class PubRSA implements Serializable {

	private static final long serialVersionUID = -4880882474760986889L;

	/** The components of the public key */
	BigInteger e, n;

	/** Takes the exponenet (e) and the modulus (nVal) as the two
	    parameters of the RSA secret key. nVal was contructed from
	    two primes (p,q) previously, and nVal = pq */
	public PubRSA(BigInteger eVal, BigInteger nVal) {
		e = eVal;
		n = nVal;
	}

	/** Encrypt the message m with this key, m < n, or you suffer truncation */
	public BigInteger encrypt(BigInteger m) {
		return m.modPow(e, n);
	}

	/** Verify that this public key's private key signed the message,
	    "reverse cipher" */
	public boolean verifySig(BigInteger m, BigInteger s) {
		return m.equals(s.modPow(e, n));
	}

	/** Return the public exponenet. **/
	public BigInteger getExponent() {
		return e;
	}

	/** Return Modulus. **/
	public BigInteger getModulus() {
		return n;
	}
}
