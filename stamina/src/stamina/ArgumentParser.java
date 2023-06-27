package stamina;

import prism.PrismLog;

class ArgumentParser {
	public static void parseArguments(String [] args) {
		// Index is updated in parseArgument
		if (index != 0) {
			StaminaLog.warning("It appears arguments have already been parsed! Will re-parse with this new list.");
			index = 0;
		}
		while (index < args.length()) {
			parseArgument(args);
		}
	}
	private static void parseArgument(String [] args) {

		++index;
	}
	private static void printHelp() {

	}

	private static int index = 0;
}
