// TransferRequestCommand.java                                     -*- Java -*-
//   Command to request trades
//
// COPYRIGHT (C) 2003, Coleman Kane
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of
// the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
//
// Written   :   Bradley M. Kuhn         University of Cincinnati
//   By          
//
// Written   :   John Franco
//   For         Special Topics: Java Programming
//               15-653-595-001, Fall 2003
// RCS       :
//
// $Source: /home/franco/CVS/Courses/C653/Homework/FrancoMonitor/Project/TransferRequestCommand.java,v $
// $Revision: 1.1 $
// $Date: 2009/04/22 11:21:11 $
//
// $Log: TransferRequestCommand.java,v $
// Revision 1.1  2009/04/22 11:21:11  franco
// *** empty log message ***
//
// Revision 1.1.1.1  2008/04/16 13:16:50  franco
//
//
// Revision 0.5  1998/12/15 05:56:01  bkuhn
//   -- put files under the GPL
//
// Revision 0.4  1998/12/01 21:51:37  bkuhn
//    -- Fixed bug with trade
//
// Revision 0.3  1998/11/30 03:18:24  bkuhn
// -- changed things to use new println() method in MonitorSession
//
// Revision 0.2  1998/11/29 12:02:07  bkuhn
//    # changed some types from int to long
//   -- added code to handle trades with MONITOR
//   -- added check to see if Player was trying to trade with themselves
//   -- changed use of TradeConfirmConnection to TransactionConfirmConnection
//
// Revision 0.1  1998/11/18 07:32:04  bkuhn
//   # initial version
//

import java.util.*;
import java.io.*;
import java.net.*;
/*****************************************************************************/
class TransferRequestCommand extends Command {
    public static final String COMMAND_STRING = "TRANSFER_REQUEST";

    Player playerTo, playerFrom;
    String identityTo;
    String identityFrom;
    String results = "";
    long amountFrom;
    String tradeData;

    /** True if from, false if for. **/
    boolean direction = true;

    /**********************************************************************/
    public Player getPlayerToTradeWith() {  return playerTo; }
    /**********************************************************************/
    String getCommandMessage() {  return new String(COMMAND_STRING);  }
    /**********************************************************************/
    void initialize(String args[]) throws CommandException {
        super.initialize(args);
        String forString;

        try {
            identityFrom = arguments[1];
            amountFrom   = Long.parseLong(arguments[2]);
            forString    = arguments[3];
            identityTo = arguments[4];

            tradeData = "";
            for (int ii = 1 ; ii < 5 ; ii ++) 
                tradeData = tradeData.concat(arguments[ii] + " ");
        } catch (ArrayIndexOutOfBoundsException abe) {
            Date date = new Date();
            String ss = date.toString();
            throw new CommandException(ss+": command, " + arguments[0] + 
                    ", requires 4 arguments, <IDENTITY1> <AMOUNT1> [for|from]"+
                    " <IDENTITY2>");
        } catch(NumberFormatException ne) {
            Date date = new Date();
            String ss = date.toString();
            throw new CommandException(ss+": Amounts must be integers");
        }

        String tmpPlayer;
        if (forString.equalsIgnoreCase("from")) {
            tmpPlayer = identityFrom;
            identityFrom = identityTo;
            identityTo = tmpPlayer;
            direction = false;
        } else if(forString.equalsIgnoreCase("for")) {
            direction = true;
        } else 
            throw new CommandException("ARG3 must be \"for\" or \"from\"");

        if (amountFrom < 0)
            throw new CommandException("Only positive amounts may be transfered");

        if (amountFrom == 0)
            throw new CommandException("One of the parties must be giving "+
                    "something");
    }
    /**********************************************************************/
    public void execute(MonitorSession session) {
        /** If we got here, then this is a good transfer, so just do it. **/
        Player thisPlayer = session.getPlayer();

        synchronized (playerTo) {
            try {
                /*if (identityTo.equalsIgnoreCase(GameParameters.MONITOR_IDENTITY)) {
                  if (thisPlayer.getEconomy().trade(thisPlayer, amountFrom))
                  session.sendResult(COMMAND_STRING + " ACCEPTED");
                  else
                  session.sendResult(COMMAND_STRING + " REJECTED");
                  } else */
					// JVF The line below had been in the test of the if
               /*(playerTo.getWealth().getHolding("rupyulars")<amountFrom)||*/
                if (playerFrom.getWealth().getHolding("rupyulars") < amountFrom) {
                    session.sendResult(COMMAND_STRING+" REJECTED-Lack of points");
                } else {
                    /*  String results = tradeConnection.getMessage();*/
                    long amt = amountFrom;
                    playerTo.wonWar();
                    playerFrom.lostWar();

                    if (!direction) {
                        amt = (long)(amt * 1.10);
                        playerFrom.trucedWar(amountFrom);
                    } else {
                        playerTo.foughtWar(amountFrom);
                    }
           playerTo.getWealth().changeHolding("rupyulars",amt);
		     playerFrom.getWealth().changeHolding("rupyulars",0-amountFrom);
                    /*playerTo.trucedWar(amountFrom);*/
                    /*session.sendResult(this.COMMAND_STRING + " " +
                      tradeConnection.getMessage());*/
                }
            } catch (UnknownResourceException ure) {
                session.sendError(
                        "FATAL TRANSFER ERROR: post to project@helios.ececs.uc.edu ("+
                        ure.getMessage() + ")");
            } catch (InsufficientResourceException ire) {
                session.sendError(
                        "FATAL TRANSFER ERROR: post to project@helios.ececs.uc.edu ("+
                        ire.getMessage() + ")");
            }
        }

    }
   /**********************************************************************/
   public void echo(MonitorSession session) {
      session.println(Directive.COMMENT_DIRECTIVE + 
                      "Seeing if transfer can be executed...");
   }
   /**********************************************************************/
   public boolean verify(MonitorSession session) {
	   /* NOTE: The "local" player is always chosen as the connected player */
	   /***
		 if (! identityFrom.equalsIgnoreCase(session.getPlayer().getIdentity())) {
		 session.sendError(session.getPlayer().getIdentity() +
		 " is not permitted to transfer as " +
		 identityFrom);
		 return false;
		 }
		***/

	   /** First, check that all input parameters are valid. **/
	   if (identityFrom.equalsIgnoreCase(identityTo)) {
		   session.sendError("trading with yourself is counter-productive");
		   return false;
	   }

	   playerTo = session.getPlayerDB().lookup(identityTo);
	   playerFrom = session.getPlayerDB().lookup(identityFrom);

	   if ((playerTo == null) || (playerFrom == null)) { 
		   session.sendError("unable to find player with identity, "
				   + identityTo);
		   return false;
	   }

	   try {
			/*
		   if (session.getPlayer().getWealth().getHolding("rupyulars")
				   < amountFrom) {
			   session.sendError(identityFrom +
					   " holds less than " + amountFrom +
					   " units of rupyulars");
			   return false;
		   }
		   */
		   if (playerTo == null) {
			   // Note: this is just a dummy check to see if we get an
			   //       exception thrown on resource value
			   if (session.getPlayer().getWealth().getHolding("rupyulars") >= 0)
				   return true;
		   }
		   /***
			 else {
			 if (playerTo.getWealth().getHolding("rupyulars") < amountFrom)
		   // just pretend and return true---check again in execute()
		   return true;
		   }
			***/
	   } catch (UnknownResourceException ure) {
		   Date date = new Date();
		   String ss = date.toString();
		   System.out.println(ss+": Transfer Request: unknown resource");
		   session.sendError(ure.getMessage());
		   return false;
	   }

	   /** Now actually run the verification socket. **/
	   try {
		   if(direction) {
			   /** Local session is the SENDER, open a remote RECIEVER 
				 connection. **/
			   playerFrom = session.getPlayer();
			   Player remotePlayer = playerTo;
			   Player localPlayer = playerFrom;
			   System.out.println("] Connection RCV to " +
					   remotePlayer.getIdentity());
			   RecieverAuthConnection outConnection = 
				   new RecieverAuthConnection(session.getPlayerDB(),
					   remotePlayer, session.getPlayerDB().lookup(identityFrom), 
					   tradeData);
			   outConnection.beginSession();
			   PubRSA pKey = outConnection.requestPubKey();
			   session.setTransfer();
			   session.setTransferDirection();
			   session.setTransferPubKey(pKey.getExponent(), 
					   pKey.getModulus());
			   session.sendResult(PublicKeyCommand.COMMAND_STRING + " " +
					   pKey.getExponent().toString(32) + " " +
					   pKey.getModulus().toString(32));
			   session.requireVerifyAndExecute(RoundsCommand.COMMAND_STRING);
			   outConnection.sendRounds(session.getTransferRounds());
			   String authSet = outConnection.requestAuthorizeSet();
			   session.setAuthorizeSet(authSet);
			   session.sendResult(AuthorizeSetCommand.COMMAND_STRING + " " +
					   authSet);
			   session.requireVerifyAndExecute(SubSetACommand.COMMAND_STRING);
			   outConnection.sendSubsetA(session.getSubSetA());
			   String subSetK = outConnection.getSubSetK();
			   session.setSubSetK(subSetK);
			   session.sendResult(SubSetKCommand.COMMAND_STRING + " " +
					   subSetK);
			   String subSetJ = outConnection.getSubSetJ();
			   session.setSubSetJ(subSetJ);
			   session.sendResult(SubSetJCommand.COMMAND_STRING + " " +
					   subSetJ);
			   session.requireVerifyAndExecute(
					   TransferResponseCommand.COMMAND_STRING);
			   if(!session.isTransferAuthorized()) {
				   outConnection.transferDeclined();
				   playerFrom.declaredWar();
				   return false;
			   }
			   /** Allows listening spoofers to steal money. hehe. **/
			   playerTo = outConnection.getConnectedIdentity();
			   outConnection.transferAuthorized();
			   outConnection = null;
		   } else {
			   /** Local session is the RECIEVER, open a remote SENDER 
				 connection. **/
			   playerTo = session.getPlayer();
			   Player remotePlayer = playerFrom;
			   Player localPlayer = playerTo;
			   System.out.println("] Connection SND to " +
					   remotePlayer.getIdentity());
			   SenderAuthConnection outConnection = new SenderAuthConnection(
					   session.getPlayerDB(), remotePlayer,
					   session.getPlayerDB().lookup(identityTo),
					   tradeData);
			   outConnection.beginSession();
			   session.setTransfer();
			   session.resetTransferDirection();
			   session.requireVerifyAndExecute(PublicKeyCommand.COMMAND_STRING);
			   PubRSA pKey = session.getTransferPubKey();
			   outConnection.sendPublicKey(pKey);
			   session.setTransferRounds(outConnection.getRounds());
			   session.sendResult(RoundsCommand.COMMAND_STRING + " " +
					   session.getTransferRounds());
			   session.requireVerifyAndExecute(
					   AuthorizeSetCommand.COMMAND_STRING);
			   outConnection.sendAuthorizeSet(
					   session.getAuthorizeSet());
			   session.setSubSetA(outConnection.getSubSetA());
			   session.sendResult(SubSetACommand.COMMAND_STRING + " " +
					   session.getSubSetA());
			   session.requireVerifyAndExecute(SubSetKCommand.COMMAND_STRING);
			   outConnection.sendSubSetK(session.getSubSetK());
			   session.requireVerifyAndExecute(SubSetJCommand.COMMAND_STRING);
			   outConnection.sendSubSetJ(session.getSubSetJ());
			   if(!outConnection.isAuthorized()) {
				   session.sendResult(TransferResponseCommand.COMMAND_STRING + 
						   " DECLINED");
				   outConnection.shutdownConnection();
				   playerFrom.declaredWar();
				   return false;
			   }
			   session.sendResult(TransferResponseCommand.COMMAND_STRING + 
					   " ACCEPTED");
			   playerFrom = outConnection.getConnectedIdentity();
			   outConnection.shutdownConnection();
			   outConnection = null;
			   return true;
		   }
	   } catch(MonitorSessionCreationException mscex) {
		   session.endTransfer();
		   session.sendError("NOT ALIVE; " + mscex.getMessage());
		   return false;
	   } catch(MonitorSessionException msex) {
		   session.endTransfer();
		   session.sendError("Session Ended Prematurely, dying");
		   return false;
	   } catch(IOException iox) {
		   session.endTransfer();
		   System.out.println("There was a local I/O Error: " +
				   iox.getMessage());
		   return false;
	   }
	   return true;
   }
   /**********************************************************************/
}
/*
  Local Variables:
  tab-width: 4
  indent-tabs-mode: nil
  eval: (c-set-style "ellemtel")
  End:
*/

