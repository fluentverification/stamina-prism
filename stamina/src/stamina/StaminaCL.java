package stamina;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import parser.Values;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import parser.ast.Property;
import prism.Prism;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import prism.Result;
import prism.ResultsCollection;
import prism.UndefinedConstants;

public class StaminaCL {
	// Version
	public static final int versionMajor = 2;
	public static final int versionMinor = 1;

	// Argument parser
	ArgumentParser argParse;

	// Stamina Object
	private StaminaModelChecker staminaMC = null;

	// storage for parsed model/properties files
	private String modelFilename = null;
	private String propertiesFilename = null;
	private ModulesFile modulesFile = null;
	private PropertiesFile propertiesFile = null;

	// info about which properties to model check
	private int numPropertiesToCheck = 0;
	private List<Property> propertiesToCheck = null;

	// info about undefined constants
	private UndefinedConstants undefinedConstants[];
	private UndefinedConstants undefinedMFConstants;
	private Values definedMFConstants;
	private Values definedPFConstants;

	// results
	private ResultsCollection results[] = null;

	//////////////////////// Command line options ///////////////////////

	// argument to -const switch
	private String constSwitch = null;

	//Probabilistic state search termination value : Defined by kappa in command line argument
// 	private static double reachabilityThreshold = -1.0;
//
// 	Kappa reduction factor
// 	private static double kappaReductionFactor = -1;
// 	private static double mispredictionFactor = -1;

	// max number of refinement count
	private static int maxApproxCount = -1;

	// termination Error window
	private static double probErrorWindow = -1.0;

	// Use property based refinement
	private static boolean noPropRefine = false;

	// Use property to explore by highest rank transition
	private static boolean rankTransitions = false;

	// Export a list of transitions with their associated action
	private String exportTransitionsToFile = null;


	//////////////////////////////////// Command lines args to pass to prism ///////////////////
	// Solutions method max iteration
	private int maxLinearSolnIter = -1;

	// Solution method
	private String solutionMethod = null;


	/**
	 * Main function. Entry point into STAMINA.
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run() {
				Runtime.getRuntime().halt(0);
			}
		});
		// Normal operation: just run StaminaCL
		if (args.length > 0) {
			new StaminaCL().run(args);
		}
		else {
			System.err.println("Error: Missing arguments.");
		}
	}
	/**
	 * Runs the StaminaCL.
	 * @param args Command line arguments to parse.
	 */
	public void run(String[] args) {

		Result res;
		argParse = new ArgumentParser();
		// Parse options
// 		doParsing(args);
		argParse.setupArgs();
		argParse.parseArguments(args);
		argParse.printHelp();

		//Initialize
		initializeSTAMINA();
		parseModelProperties();

		// Process options
		processOptions();

		try {
			// process info about undefined constant
			undefinedMFConstants = new UndefinedConstants(modulesFile, null);

			undefinedConstants = new UndefinedConstants[numPropertiesToCheck];
			for (int i = 0; i < numPropertiesToCheck; i++) {
				undefinedConstants[i] = new UndefinedConstants(modulesFile, propertiesFile, propertiesToCheck.get(i));
			}

			// then set up value using const switch definitions
			undefinedMFConstants.defineUsingConstSwitch(constSwitch);
			for (int i = 0; i < numPropertiesToCheck; i++) {
				undefinedConstants[i].defineUsingConstSwitch(constSwitch);
			}



			// initialise storage for results
			results = new ResultsCollection[numPropertiesToCheck];
			for (int i = 0; i < numPropertiesToCheck; i++) {
				results[i] = new ResultsCollection(undefinedConstants[i], propertiesToCheck.get(i).getName());
			}

			// iterate through as many models as necessary
			for (int i = 0; i < undefinedMFConstants.getNumModelIterations(); i++) {

				// set values for ModulesFile constants
				try {
					definedMFConstants = undefinedMFConstants.getMFConstantValues();
					staminaMC.setPRISMModelConstants(definedMFConstants);
				} catch (PrismException e) {
					// in case of error, report it, store as result for any properties, and go on to the next model
					// (might happen for example if overflow or another numerical problem is detected at this stage)
					StaminaLog.log("\nError: " + e.getMessage() + ".");
					for (int j = 0; j < numPropertiesToCheck; j++) {
						results[j].setMultipleErrors(definedMFConstants, null, e);
					}
					// iterate to next model
					undefinedMFConstants.iterateModel();
					for (int j = 0; j < numPropertiesToCheck; j++) {
						undefinedConstants[j].iterateModel();
					}
					continue;
				}

				// Work through list of properties to be checked
				for (int j = 0; j < numPropertiesToCheck; j++) {


					for (int k = 0; k < undefinedConstants[j].getNumPropertyIterations(); k++) {

						try {
							// Set values for PropertiesFile constants
							if (propertiesFile != null) {
								definedPFConstants = undefinedConstants[j].getPFConstantValues();
								propertiesFile.setSomeUndefinedConstants(definedPFConstants);
							}

							res = staminaMC.modelCheckStamina(propertiesFile, propertiesToCheck.get(j));



						} catch (PrismException e) {
							StaminaLog.log("\nError: " + e.getMessage() + ".");
							res = new Result(e);
						}

						// store result of model checking
						results[j].setResult(definedMFConstants, definedPFConstants, res.getResult());
						//results[j+1].setResult(definedMFConstants, definedPFConstants, res[1].getResult());

						// iterate to next property
						undefinedConstants[j].iterateProperty();

					}
				}

				// iterate to next model
				undefinedMFConstants.iterateModel();
				for (int j = 0; j < numPropertiesToCheck; j++) {
					undefinedConstants[j].iterateModel();
				}

			}

		} catch (PrismException e) {
			StaminaLog.errorAndExit(e.getMessage(), StaminaLog.GENERAL_ERROR);
		}

	}

	/**
	 * Initializes STAMINA to ready state. Also initializes the PRISM engine we're using.
	 */
	public void initializeSTAMINA() {

		//init prism
		try {
			// Create a log for PRISM output (hidden or stdout)

			// Print our version
			StaminaLog.log("STAMINA\n=====\nVersion: " + Integer.toString(versionMajor) + "." + Integer.toString(versionMinor) + "\n");
			// Initialise PRISM engine
			staminaMC = new StaminaModelChecker();
			staminaMC.initialise();

			staminaMC.setEngine(Prism.EXPLICIT);

		} catch (PrismException e) {
			StaminaLog.errorAndExit(e.getMessage(), StaminaLog.GENERAL_ERROR);
		}
	}


	/**
	 * Processes command line arguments.
	 */
	private void processOptions() {

		try {

			if (maxLinearSolnIter >= 0) {
				staminaMC.setMaxIters(Options.getMaxIterations());
			}

			if (solutionMethod != null) {

				if (solutionMethod.equals("power")) {
					staminaMC.setEngine(Prism.POWER);
				}
				else if (solutionMethod.equals("jacobi")) {
					staminaMC.setEngine(Prism.JACOBI);
				}
				else if (solutionMethod.equals("gaussseidel")) {
					staminaMC.setEngine(Prism.GAUSSSEIDEL);
				}
				else if (solutionMethod.equals("bgaussseidel")) {
					staminaMC.setEngine(Prism.BGAUSSSEIDEL);
				}
			}
			staminaMC.loadPRISMModel(modulesFile);

		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * parse model and properties file
	 */
	void parseModelProperties(){

		propertiesToCheck = new ArrayList<Property>();

		try {
			// Parse and load a PRISM model from a file
			modulesFile = staminaMC.parseModelFile(new File(Options.getModelFileName()));

			// Parse and load a properties model for the model
			propertiesFile = staminaMC.parsePropertiesFile(modulesFile, new File(Options.getPropertyFileName()));

			if (propertiesFile == null) {
				numPropertiesToCheck = 0;
			}
			// unless specified, verify all properties
			else {
				if (Options.getSpecificProperty()) {
					int tempProperties = propertiesFile.getNumProperties();
					for (int i = 0; i<tempProperties; ++i) {
						if (propertiesFile.getPropertyObject(i).getName().equals(Options.getPropertyName())) {
							propertiesToCheck.add(propertiesFile.getPropertyObject(i));
							numPropertiesToCheck = 1;
							break;
						}
					}
					if (numPropertiesToCheck != 1) {
						throw new PrismException("Did not find property " + Options.getPropertyName());
					}
				} else {
					numPropertiesToCheck = propertiesFile.getNumProperties();
					for(int i = 0; i< numPropertiesToCheck; ++i) {
						propertiesToCheck.add(propertiesFile.getPropertyObject(i));
					}
				}
			}

		} catch (FileNotFoundException e) {
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		} catch (PrismException e) {
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		}

	}


	/**
	 * Report a (fatal) error and exit cleanly (with exit code 1).
	 */
	private void exit() {
		if (staminaMC != null) {
			staminaMC.closeDown();
		}
		StaminaLog.flushLogs();
		System.exit(1);
	}

	/**
	 * Report a (fatal) error and exit cleanly (with exit code 1).
	 * @param s Error message
	 */
	private void errorAndExit(String s) {
		if(staminaMC != null) {
			staminaMC.closeDown();
		}
		StaminaLog.errorAndExit(s + ".", StaminaLog.GENERAL_ERROR);
	}
}
