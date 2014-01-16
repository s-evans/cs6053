// Timer.java                                                 -*- Java -*-
//    A class to encapsulate a session with a monitor
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
// $Source: /home/franco/CVS/Courses/C653/Homework/FrancoMonitor/Project/Timer.java,v $
// $Revision: 1.1 $
// $Date: 2009/04/22 11:21:11 $
//
// $Log: Timer.java,v $
// Revision 1.1  2009/04/22 11:21:11  franco
// *** empty log message ***
//
// Revision 1.1.1.1  2008/04/16 13:16:50  franco
//
//
// Revision 0.4  1998/12/15 05:56:01  bkuhn
//   -- put files under the GPL
//
// Revision 0.3  1998/11/09 08:43:12  bkuhn
//   -- took out unnecessary yeild
//
// Revision 0.2  1998/11/09 06:08:43  bkuhn
//   -- changed the priority so it was not quite so high.
//   -- added a yield() call right before stop()...this is
//      probably pointless and should be taken out
//
// Revision 0.1  1998/11/08 05:46:06  bkuhn
//   # initial version
//

class Timer extends Thread implements TimedExistance
{
      TimedExistance whomIamTiming = null;
      long amountToSleep;
      long absoluteStartTime, absoluteFinishTime;
      
      public Timer(double a, TimedExistance who)
      {
         whomIamTiming = who;
         amountToSleep = (long) (1000 * a);
         setPriority(NORM_PRIORITY - 3);
      }

      public double getTime()
      {
         if (this.isAlive())
            absoluteFinishTime = System.currentTimeMillis();
            
         return (double)(absoluteFinishTime - absoluteStartTime) / 1000.0;
      }
   
      public void outOfTime()
      {
         absoluteFinishTime = System.currentTimeMillis();
         whomIamTiming = null;
         stop();
      }
   
      public void run()
      {
         absoluteStartTime = System.currentTimeMillis();
         try
         {
            sleep(amountToSleep);
         }
         catch (InterruptedException  ie) { }

         if (whomIamTiming != null)
            whomIamTiming.outOfTime();

         absoluteFinishTime = System.currentTimeMillis();
      }
}

/*
  Local Variables:
  tab-width: 4
  indent-tabs-mode: nil
  eval: (c-set-style "ellemtel")
  End:
*/
