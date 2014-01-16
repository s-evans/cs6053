import java.math.*;

class SubSetACommand extends Command {
	private static String rcsid = "$Revision: 1.1 $";
	public static String COMMAND_STRING = "SUBSET_A";
	private int argnum;
	String subset;

	public String getCommandMessage() {
		return new String(this.COMMAND_STRING);
	}

	public void initialize(String args[]) throws CommandException {
		super.initialize(args);
		try {
			subset = "";
			argnum = args.length - 1;
			for(int i = 1; i < args.length; i++)
				subset += args[i].trim() + " ";
			subset = subset.trim(); // There will be a trailing space
		} catch(ArrayIndexOutOfBoundsException ax) {
			throw new CommandException("SUBSET_A Usage: SUBSET_A " +
					"ARG1 ARG2 ... ARGn, n is the number of chosen " +
					"components.");
		}
	}

	public boolean verify(MonitorSession session) {
		if(!session.transferring()) {
			session.sendError("SUBSET_A not available, must use " +
					"TRANSFER_REQUEST");
			return false;
		}
		if(argnum > session.getTransferRounds()) {
			session.sendError("SUBSET_A cannot have more elements " +
					"than ROUNDS specified.");
			return false;
		}
		return true;
	}
	
	public void execute(MonitorSession session) {
		session.setSubSetA(subset);
	}
}
