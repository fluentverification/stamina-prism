package stamina;

import prism.PrismLog;

/**
 * A simple class that forwards messages onto the Prism Log
 * */
class StaminaLog {
	// Allow us to print to stdout and stderr
	private static PrismLog out = new PrismLog("stdout");
	private static PrismLog err = new PrismLog("stderr");

	public static void warning(String message) {
		err.println("[STAMINA:WARNING]: " + message);
	}

	public static void error(String message) {
		err.println("[STAMINA:ERROR]: " + message);
	}

	public static void info(String message) {
		err.println("[STAMINA:LOG]: " + message);
	}

	public static void log(String message) {
		out.println(message);
	}
}
