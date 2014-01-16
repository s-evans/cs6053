// TradeResponseCommand.java                                 -*- Java -*-
//   Command for responsing to trade requests
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
// $Source: /home/franco/CVS/Courses/C653/Homework/FrancoMonitor/Project/TradeResponseCommand.java,v $
// $Revision: 1.1 $
// $Date: 2009/04/22 11:21:11 $
//
// $Log: TradeResponseCommand.java,v $
// Revision 1.1  2009/04/22 11:21:11  franco
// *** empty log message ***
//
// Revision 1.1.1.1  2008/04/16 13:16:50  franco
//
//
// Revision 0.4  1998/12/15 05:56:01  bkuhn
//   -- put files under the GPL
//
// Revision 0.3  1998/11/30 03:18:24  bkuhn
// -- changed things to use new println() method in MonitorSession
//
// Revision 0.2  1998/11/29 12:02:07  bkuhn
//   -- changed TradeResponseCommand so it now derives from TransactionResponseCommand
//
// Revision 0.1  1998/11/18 07:17:50  bkuhn
//   # initial version
//

import java.io.PrintStream;
/*****************************************************************************/
class TradeResponseCommand extends TransactionResponseCommand {
   public static final String COMMAND_STRING = "TRANSFER_RESPONSE";
   /**********************************************************************/
   String getCommandMessage() {  return new String(COMMAND_STRING); }
   /**********************************************************************/
   public void echo(MonitorSession session) {
      session.println(Directive.COMMENT_DIRECTIVE + 
                      "Seeing if transfer can be executed...");
   }
}
/*****************************************************************************/
/*
  Local Variables:
  tab-width: 4
  indent-tabs-mode: nil
  eval: (c-set-style "ellemtel")
  End:
*/
