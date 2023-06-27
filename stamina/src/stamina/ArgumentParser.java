package stamina;

import java.util.HashMap;
import java.util.ArrayList;

import prism.PrismLog;

class ArgumentParser {
	// The size of the column when printing
	private static final int COLUMN_WIDTH = 42;
	private static final char SEPARATOR = '.';
	public enum ArgumentType {
		DOUBLE
		, INTEGER
		, STRING
		, NONE
	}

	class Argument {
		public String name;
		public String description;
		public ArgumentType type;
		/**
		 * Constructor for Argument type
		 * */
		public Argument(String name, ArgumentType type, String description) {
			this.name = name;
			this.description = description;
			this.type = type;
		}

		public Argument(String name, String description) {
			this.name = name;
			this.description = description;
			this.type = ArgumentType.NONE;
		}

		public boolean hasValue() {
			return this.type != ArgumentType.NONE;
		}
	}

	public ArgumentParser() {
		index = 0;
		flags = new HashMap<String, Argument>();
		orderedFlags = new ArrayList<Argument>();
		arguments = new ArrayList<Argument>();
	}

	/**
	 * Sets the default arguments
	 * */
	public void setupArgs() {
		// We have two arguments: Model file and properties file, both string
		addArgument("MODEL FILE", "Prism model file. Extensions: .prism, .sm");
		addArgument("PROPERTIES FILE", "Property file. Extensions: .csl");
		// These are the possible flags, in order of importance
		addFlag("kappa", ArgumentType.DOUBLE, "Reachability threshold for first iteration [default: 1.0]");
		addFlag("rKappa", ArgumentType.DOUBLE, "Reduction factor for ReachabilityThreshold (kappa) for refinement step. [default: 1.25]");
		addFlag("approxFactor", ArgumentType.DOUBLE, "Factor to estimate how far off our reachability predictions will be [default: 2.0]");
		addFlag("probWin", ArgumentType.DOUBLE, "Probability window between lower and upper bound for termination. [default: 1.0e-3]");
		addFlag("cuddMaxMemory", ArgumentType.STRING, "Maximum cudd memory. Expects the same format as PRISM [default: \"1g\"]");
		addFlag("export", ArgumentType.STRING, "Export model to a series of files with provided name (no extension)");
		addFlag("exportPerimeterStates", ArgumentType.STRING, "Export perimeter states to a file. Please provide a filename. This will append to the file if it is existing.");
		addFlag("import", ArgumentType.STRING, "Import model to a file. Please provide a filename without an extension");
		addFlag("property", ArgumentType.STRING, "Choose a specific property to check in a model file that contains many");
		addFlag("noPropRefine", ArgumentType.NONE, "Do not use property based refinement. If given, model exploration method will reduce the kappa and do the property independent refinement. [default: off]");
		addFlag("maxApproxCount", ArgumentType.INTEGER, "Maximum number of approximation iterations. [default: 10]");
		addFlag("maxIters", ArgumentType.INTEGER, "Maximum number of iterations to find solution. [default: 10000]");
		addFlag("method", ArgumentType.STRING, "Method to solve CTMC. Supported methods are 'power', 'jacobi', 'gaussseidel', and 'bgaussseidel'.");
		addFlag("const", ArgumentType.STRING, "Comma separated values for constants (ex: \"a=1,b=5.6,c=true\")");
		addFlag("rankTransitions", ArgumentType.NONE, "");
		addFlag("exportTrans", ArgumentType.STRING, "Export the list of transitions and actions to a specified file name, or to trans.txt if no file name is specified. Transitions exported in the format srcStateIndex destStateIndex actionLabe");
	}

	public void addArgument(String name, String description) {
		arguments.add(new Argument(name, ArgumentType.STRING, description));
	}

	public void addArgument(String name, ArgumentType type, String description) {
		arguments.add(new Argument(name, type, description));
	}

	public void addFlag(String name, String description) {
		Argument flag = new Argument(name, description);
		flags.put(name, flag);
		orderedFlags.add(flag);
	}

	public void addFlag(String name, ArgumentType type, String description) {
		Argument flag = new Argument(name, type, description);
		flags.put(name, flag);
		orderedFlags.add(flag);
	}

	public void parseArguments(String [] args) {
		// Index is updated in parseArgument
		if (index != 0) {
			StaminaLog.warning("It appears arguments have already been parsed! Will re-parse with this new list.");
			index = 0;
		}
		while (index < args.length) {
			parseArgument(args);
		}
	}
	private void parseArgument(String [] args) {
		String arg = args[index];
		// Get the switch if it's a switch
		boolean isFlag = arg.length() > 0 && arg.charAt(0) == '-';
		String flag = arg.substring(1);
		// Trim as many leading '-' characters as needed so as to support -flags and --flags
		while (flag.charAt(0) == '-') {
			flag = flag.substring(1);
		}
		// Some built-in arguments to the argument parser
		if (flag.equals("help")) {
			printHelp();
		}
		else if (flag.equals("usage")) {
			printUsage();
			System.exit(0);
		}
		else if (flag.equals("about") || flag.equals("version")) {
			printDescription();
			System.exit(0);
		}
		if (!flags.containsKey(flag)) {
			StaminaLog.errorAndExit("Argument '" + flag + "' not supported!", 1);
		}
		++index;
	}

	private void printDescription() {
		StaminaLog.log("STAMINA/PRISM: a PRISM-based infinite CTMC model checker (https://staminachecker.org)");
		StaminaLog.log("\tAuthors: Thakur Neupane, Riley Roberts, Joshua Jeppson, Zhen Zhang, and others...");
		StaminaLog.log("Version: " + StaminaCL.versionMajor + "." + StaminaCL.versionMinor);

	}

	private void printUsage() {
		String argsString = "";
		for (Argument arg : arguments) {
			argsString += " [" + arg.name + "]";
		}
		StaminaLog.log("USAGE: pstamina" + argsString + " [OPTIONS...]");
	}

	public void printHelp() {
		printDescription();
		printUsage();
		StaminaLog.endSection();
		for (Argument arg : arguments) {
			// the left column is the argument name and the type
			String leftColumn = arg.name + " (" + typeEnumToString(arg.type) + ")";
			String spaces = "";
			for (int i = leftColumn.length() - 1; i < ArgumentParser.COLUMN_WIDTH; ++i) {
				spaces += ArgumentParser.SEPARATOR;
			}
			StaminaLog.log(leftColumn + spaces + arg.description);
		}
		StaminaLog.endSection();
		for (Argument flag : orderedFlags) {
			String leftColumn = flag.name;
			if (flag.hasValue()) {
				leftColumn += " (" + typeEnumToString(flag.type) + ")";
			}
			String spaces = "";
			for (int i = leftColumn.length() - 1; i < ArgumentParser.COLUMN_WIDTH; ++i) {
				spaces += ArgumentParser.SEPARATOR;
			}
			StaminaLog.log(leftColumn + spaces + flag.description);
		}
		StaminaLog.endSection();
		StaminaLog.log("To show this message again, use the '-help'/'--help' flags. To show usage, use the '-usage'/'--usage' flags. To show an 'about' message, use the '-about'/'--about' flags.");
		System.exit(0);
	}

	private String typeEnumToString(ArgumentType type) {
		switch (type) {
			case DOUBLE:
				return "double";
			case INTEGER:
				return "int";
			case STRING:
				return "string";
			case NONE:
				return "null";
			default:
				return "unknown";
		}
	}

	private int index;
	private HashMap<String, Argument> flags;
	private ArrayList<Argument> orderedFlags;
	private ArrayList<Argument> arguments;
}
