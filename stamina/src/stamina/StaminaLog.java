package stamina;

import prism.PrismFileLog;
import prism.PrismLog;

/**
 * A simple class that forwards messages onto the Prism Log
 * */
class StaminaLog {
	public static final int GENERAL_ERROR = 1;
	// Allow us to print to stdout and stderr
	private static PrismLog out = new PrismFileLog("stdout");
	private static PrismLog err = new PrismFileLog("stderr");
	private static final String line = "========================================================================";
	private static final String thinLine = "------------------------------------------------------------------------";

	/**
	 * Gets the main log used since it may be needed for a Prism Object
	 *
	 * @return The main log
	 * */
	public static PrismLog getMainLog() {
		return out;
	}

	public static void warning(String message) {
		// Use PrismLog.printWarning?
		err.println("[STAMINA:WARNING]: " + message);
	}

	public static void error(String message) {
		err.println("[STAMINA:ERROR]: " + message);
		flushLogs();
	}

	public static void errorAndExit(String message, int exitCode) {
		err.println("[STAMINA:ERROR]: (Unrecoverable) " + message);
		flushLogs();
		System.exit(exitCode);
	}

	public static void flushLogs() {
		out.flush();
		err.flush();
	}

	public static void info(String message) {
		err.println("[STAMINA:LOG]: " + message);
	}

	public static void log(String message) {
		out.println(message);
	}

	public static void print(String message) {
		out.print(message);
	}

	/**
	 * Prints the message as a "header", meaning that it has lines on top and bottom
	 *
	 * @param message The message to print in the header
	 * */
	public static void header(String message) {
		out.println(line);
		out.println(message);
		out.println(line);
	}

	public static void endSection() {
		out.println(thinLine);
	}

	public static void logResult(String prop, String pMin, String pMax) {
		out.println(line);
		out.println("Property: " + prop);
		out.println("Probability Minimum: " + pMin);
		out.println("Probability Maximum: " + pMax );
		out.println(line);
	}

}
