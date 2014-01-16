// CheckForLiving.java                                            -*- Java -*-
//    Check to see if Players in the Player Database are living
//
// COPYRIGHT (C) 1998, Bradley M. Kuhn
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
//               15-625-595-001, Fall 1998
// RCS       :
//
// $Source: /home/franco/CVS/Courses/C653/Homework/FrancoMonitor/Project/CheckForLiving.java,v $
// $Revision: 1.1 $
// $Date: 2009/04/22 11:21:10 $
//
// $Log: CheckForLiving.java,v $
// Revision 1.1  2009/04/22 11:21:10  franco
// *** empty log message ***
//
// Revision 1.1.1.1  2008/04/16 13:16:50  franco
//
//
// Revision 0.5  1998/12/15 05:56:01  bkuhn
//   -- put files under the GPL
//
// Revision 0.4  1998/12/01 21:42:06  bkuhn
//   # minor changes that should not effect running...just simplified
//      the logic in run()
//
// Revision 0.3  1998/11/18 03:10:18  bkuhn
//   # cosemetic changes for formatting of output to stdout
//
// Revision 0.2  1998/11/16 08:37:38  bkuhn
//   # cosmetic changes to output
//
// Revision 0.1  1998/11/13 08:55:12  bkuhn
//   # initial version
//

import java.util.*;

/*****************************************************************************/
class CheckForLiving extends Thread implements RecurredEvent
{
      PlayerDB playerDB;
      static final String rcsid = "$Revision: 1.1 $";

      CheckForLiving()
      {
      }
      /**********************************************************************/
      public void initialize(Object o)
      {
         playerDB = (PlayerDB) o;
      }
      /**********************************************************************/
      public void run()
      {
         Player curPlayer;

			Date date = new Date();
			String s = date.toString();
			System.out.println(s+":");
			
         System.out.println("CHECK_FOR_LIVING: Beginning...");

         for (Enumeration e = playerDB.getPlayers() ; e.hasMoreElements() ; )
         {
            AliveConnection aliveConnection = null;

            curPlayer = (Player) e.nextElement();

            try
            {
               aliveConnection = 
                  new AliveConnection(curPlayer.getInetAddress(),
                                      curPlayer.getPort(),
                                      playerDB, curPlayer);            
            }
            catch (MonitorSessionException mse)
            {
               aliveConnection = null;
            }

            if (aliveConnection != null)
            {
               aliveConnection.start();

               try { aliveConnection.join(); }
               catch(InterruptedException ie) { }
            }

            curPlayer.checkedForLiving(aliveConnection != null && 
                                       aliveConnection.completedNormally());

            System.out.println("CHECK_FOR_LIVING:     " +
                               curPlayer.getIdentity() + " " +
                               " has been alive for " + 
               ( (double) 
                 curPlayer.getSecondsAliveSinceLastResourceAllocation()
                                  / 60.0)
                                  + " mins. (since res. alloc.)");
         }
			date = new Date();
			s = date.toString();
			System.out.println(s+":");
			
         System.out.println("CHECK_FOR_LIVING: Ending...");
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
