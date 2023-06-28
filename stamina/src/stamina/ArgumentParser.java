package stamina;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.lang.*;

import prism.PrismLog;

class ArgumentParser {
	// The size of the column when printing
	private static final int COLUMN_WIDTH = 32;
	private static final int MAX_DESCRIPTION_WIDTH = 42;
	private static final char SEPARATOR = '.';
	public enum ArgumentType {
		DOUBLE
		, INTEGER
		, STRING
		, NONE
	}

	class Argument<T> {
		public String name;
		public String description;
		public ArgumentType type;
		public Consumer<T> validateAndAccept;
		// If default value is not null then this argument will allow the flag but
		// if there is no value associated with it (passed in with the flag via argv),
		// the Argument use a this value
		T defaultValue = null;
		/**
		 * Constructor for Argument type
		 * @param name the name of the command-line argument
		 * @param type the argument type (to be validated)
		 * @param description A short description of the argument
		 * @param validateAndAccept A lambda function which validates and accepts the given argument. Note that
		 * if the `type` is `ArgumentType.NONE`, then a `null` object will be passed into this lambda. Additionally,
		 * an Integer object (not an int primitive) and a Double object (not a `double` primitive) will be passed in
		 * if those types are chosen
		 * */
		public Argument(String name, ArgumentType type, String description, Consumer<T> validateAndAccept) {
			this.name = name;
			this.description = description;
			this.type = type;
			this.validateAndAccept = validateAndAccept;
		}

		public Argument(String name, String description, Consumer<T> validateAndAccept) {
			this.name = name;
			this.description = description;
			this.type = ArgumentType.NONE;
			this.validateAndAccept = validateAndAccept;
		}

		public boolean hasValue() {
			return this.type != ArgumentType.NONE;
		}

		/**
		 * Parses the string input (if null, ignores) and calls the validateAndAccept consumer
		 *
		 * @param input The text-input (or null)
		 * */
		public void parseValidateAndAccept(String input) {
			switch (this.type) {
				case DOUBLE:
					// Call validateAndAccept
					((Consumer<Double>) this.validateAndAccept).accept(new Double(input));
					break;
				case INTEGER:
					// Call validateAndAccept
					((Consumer<Integer>) this.validateAndAccept).accept(new Integer(input));
					break;
				case STRING:
					// Call validateAndAccept
					((Consumer<String>) this.validateAndAccept).accept(input);
					break;
				default:
					// Leave it null
					this.validateAndAccept.accept(null);
			}
		}
	}

	public ArgumentParser() {
		index = 0;
		argIndex = 0;
		flags = new HashMap<String, Argument<Object>>();
		orderedFlags = new ArrayList<Argument<Object>>();
		arguments = new ArrayList<Argument<Object>>();
	}

	/**
	 * Sets the default arguments
	 * */
	public void setupArgs() {
		// We have two arguments: Model file and properties file, both string
		addArgument("MODEL FILE"
			, "Prism model file. Extensions: .prism, .sm"
			, (Consumer<String>) model -> {
				Options.setModelFileName(model);
			}
		);
		addArgument("PROPERTIES FILE"
			, "Property file. Extensions: .csl"
			, (Consumer<String>) prop -> {
				Options.setPropertyFileName(prop);
			}
		);
		// These are the possible flags, in order of importance
		addFlag("kappa"
			, ArgumentType.DOUBLE
			, "Reachability threshold for first iteration [default: 1.0]"
			, (Consumer<Double>) k -> {
				double kappa = k.doubleValue();
				if (kappa < 0 || kappa >= 1) {
					StaminaLog.errorAndExit("Reachability threshold 'kappa' should be in the range [0, 1)!", 1);
				}
				Options.setReachabilityThreshold(kappa);
			}
		);
		addFlag("rKappa"
			, ArgumentType.DOUBLE
			, "Reduction factor for ReachabilityThreshold (kappa) for refinement step. [default: 1.25]"
			, (Consumer<Double>) rk -> {
				double rKappa = rk.doubleValue();
				if (rKappa <= 1) {
					StaminaLog.errorAndExit("Reduction factor 'rKappa' must be greater than 1!", 1);
				}
				Options.setKappaReductionFactor(rKappa);
			}
		);
		addFlag("approxFactor"
			, ArgumentType.DOUBLE
			, "Factor to estimate how far off our reachability predictions will be [default: 2.0]"
			, (Consumer<Double>) approx -> {
				double approxFactor = approx.doubleValue();
				if (approxFactor < 0) {
					StaminaLog.errorAndExit("Misprediction factor 'approxFactor' should be greater than or equal to 0!", 1);
				}
				Options.setMispredictionFactor(approxFactor);
			}
		);
		addFlag("probWin"
			, ArgumentType.DOUBLE
			, "Probability window between lower and upper bound for termination. [default: 1.0e-3]"
			, (Consumer<Double>) w -> {
				double window = w.doubleValue();
				if (window <= 0 || window >= 1) {
					StaminaLog.errorAndExit("Probability window 'probWin' should be in the range (0, 1)!", 1);
				}
				Options.setProbErrorWindow(window);
			}
		);
		addFlag("cuddMaxMemory"
			, ArgumentType.STRING
			, "Maximum cudd memory. Expects the same format as PRISM [default: \"1g\"]"
			, (Consumer<String>) mem -> { Options.setCuddMemoryLimit(mem); }
		);
		addFlag("export"
			, ArgumentType.STRING
			, "Export model to a series of files with provided name (no extension)"
			, (Consumer<String>) filename -> {
				Options.setExportModel(true);
				Options.setExportFileName(filename);
			}
		);
		addFlag("exportPerimeterStates"
			, ArgumentType.STRING
			, "Export perimeter states to a file. Please provide a filename. This will append to the file if it is existing."
			, (Consumer<String>) filename -> {
				Options.setExportPerimeterStates(true);
				Options.setExportPerimeterFilename(filename);
			}
		);
		addFlag("import"
			, ArgumentType.STRING
			, "Import model to a file. Please provide a filename without an extension"
			, (Consumer<String>) filename -> {
				Options.setImportModel(true);
				Options.setImportFileName(filename);
			}
		);
		addFlag("property"
			, ArgumentType.STRING
			, "Choose a specific property to check in a model file that contains many"
			, (Consumer<String>) name -> {
				Options.setSpecificProperty(true);
				Options.setPropertyName(name);
			}
		);
		addFlag(
			"noPropRefine"
			, ArgumentType.NONE
			, "Do not use property based refinement. If given, model exploration method will reduce the kappa and do the property independent refinement. [default: off]"
			, p -> { Options.setNoPropRefine(true); }
		);
		addFlag("maxApproxCount"
			, ArgumentType.INTEGER
			, "Maximum number of approximation iterations. [default: 10]"
			, (Consumer<Integer>) mac -> {
				int maxApproxCount = mac.intValue();
				if (maxApproxCount <= 0) {
					StaminaLog.errorAndExit("Parameter 'maxApproxCount' must be greater than 0!", 1);
				}
				Options.setMaxRefinementCount(maxApproxCount);
			}
		);
		addFlag("maxIters"
			, ArgumentType.INTEGER
			, "Maximum number of iterations to find solution. [default: 10000]"
			, (Consumer<Integer>) mi -> {
				int maxIters = mi.intValue();
				if (maxIters <= 0) {
					StaminaLog.errorAndExit("Parameter 'maxIters' must be greater than 0!", 1);
				}
				Options.setMaxIterations(maxIters);
			}
		);
		addFlag("method"
			, ArgumentType.STRING
			, "Method to solve CTMC. Supported methods are 'power', 'jacobi', 'gaussseidel', and 'bgaussseidel'."
			, (Consumer<String>) method -> {
				if (method.equals("power") || method.equals("jacobi")
					|| method.equals("gaussseidel") || method.equals("bgaussseidel")) {
					// TODO: set method
				}
				else {
					StaminaLog.errorAndExit("Method '" + method + "' is not supported!", 1);
				}
			}
		);
		addFlag("const"
			, ArgumentType.STRING
			, "Comma separated values for constants (ex: \"a=1,b=5.6,c=true\")"
			, (Consumer<String>) consts -> {
				Options.appendUndefinedConsts(consts);
			}
		);
		addFlag("rankTransitions"
			, ArgumentType.NONE
			, "Rank transitions before expanding [default: false]"
			, rank -> {
				Options.setRankTransitions(true);
			}
		);
		addFlag(
			"exportTrans"
			, ArgumentType.STRING
			, "Export the list of transitions and actions to a specified file name, or to trans.txt if no file name is specified. Transitions exported in the format srcStateIndex destStateIndex actionLabe"
			, (Consumer<String>) filename -> {
				// TODO: Allow default value if no value provided to flag
				Options.setExportTransitionsToFile(filename);
			}
		);
	}

	public void addArgument(String name, String description, Consumer<String> validateAndAccept) {
		arguments.add(new Argument(name, ArgumentType.STRING, description, validateAndAccept));
	}

	public void addArgument(String name, ArgumentType type, String description, Consumer validateAndAccept) {
		arguments.add(new Argument(name, type, description, validateAndAccept));
	}

	public void addFlag(String name, String description, Consumer<Object> validateAndAccept) {
		Argument<Object> flag = new Argument(name, ArgumentType.NONE, description, validateAndAccept);
		flags.put(name, flag);
		orderedFlags.add(flag);
	}

	public void addFlag(String name, ArgumentType type, String description, Consumer validateAndAccept) {
		Argument flag = new Argument(name, type, description, validateAndAccept);
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
		// If it's not a flag, it is a generic argument
		if (!isFlag) {
			if (argIndex >= arguments.size()) {
				StaminaLog.log("Too many arguments");
				printUsage();
			}
			Argument currentArg = arguments.get(argIndex);
			currentArg.parseValidateAndAccept(arg);
			argIndex++;
			index++;
			return;
		}
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
		Argument flagData = flags.get(flag);
		if (flagData.type == ArgumentType.NONE) {
			flagData.parseValidateAndAccept(null);
		}
		else if (args.length > index + 1) {
			String value = args[++index];
			flagData.parseValidateAndAccept(value);
		}
		// TODO: Add another else if condition here for argument that can optionally not
		// take a value but has a default value.
		else {
			StaminaLog.errorAndExit("Argument '" + flagData.name + "' should have been given a value of type " + typeEnumToString(flagData.type), 1);
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
		for (Argument<Object> arg : arguments) {
			argsString += " [" + arg.name + "]";
		}
		StaminaLog.log("USAGE: pstamina" + argsString + " [OPTIONS...]");
	}

	public void printHelp() {
		String blankSpaces = "";
		for (int i = 0; i <= ArgumentParser.COLUMN_WIDTH; ++i) {
			blankSpaces += " ";
		}
		printDescription();
		printUsage();
		StaminaLog.endSection();
		for (Argument<Object> arg : arguments) {
			// the left column is the argument name and the type
			String leftColumn = arg.name + " (" + typeEnumToString(arg.type) + ")";
			String spaces = "";
			for (int i = leftColumn.length() - 1; i < ArgumentParser.COLUMN_WIDTH; ++i) {
				spaces += ArgumentParser.SEPARATOR;
			}
			// Break the lines in the description of the argument
			int startIndex = 0;
			while (startIndex < arg.description.length()) {
				String left = startIndex == 0 ? leftColumn + spaces : blankSpaces;
				StaminaLog.log(left + arg.description.substring(
						startIndex
						, Math.min(
							startIndex + ArgumentParser.MAX_DESCRIPTION_WIDTH
							, arg.description.length()
						)
					).trim()
				);
				startIndex += ArgumentParser.MAX_DESCRIPTION_WIDTH;
			}
		}
		StaminaLog.endSection();
		for (Argument<Object> flag : orderedFlags) {
			String leftColumn = flag.name;
			if (flag.hasValue()) {
				leftColumn += " (" + typeEnumToString(flag.type) + ")";
			}
			String spaces = "";
			for (int i = leftColumn.length() - 1; i < ArgumentParser.COLUMN_WIDTH; ++i) {
				spaces += ArgumentParser.SEPARATOR;
			}
			int startIndex = 0;
			while (startIndex < flag.description.length()) {
				String left = startIndex == 0 ? leftColumn + spaces : blankSpaces;
				StaminaLog.log(left + flag.description.substring(
						startIndex
						, Math.min(
							startIndex + ArgumentParser.MAX_DESCRIPTION_WIDTH
							, flag.description.length()
						)
					).trim()
				);
				startIndex += ArgumentParser.MAX_DESCRIPTION_WIDTH;
			}
// 			StaminaLog.log(leftColumn + spaces + flag.description);
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
	private int argIndex;
	private HashMap<String, Argument<Object>> flags;
	private ArrayList<Argument<Object>> orderedFlags;
	private ArrayList<Argument<Object>> arguments;
}
