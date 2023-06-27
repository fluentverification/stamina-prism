package stamina;

public class Options {

	//Probabilistic state search termination value, should start at 1 in most normal cases : Defined by kappa in command line argument
	private static double reachabilityThreshold = 1;

	// Kappa reduction factor
	private static double kappaReductionFactor = 1.25;

	// The amount we divide our estimated perimeter reachabilty by before comparing it to the target
	private static double mispredictionFactor = 2;

	// max number of refinement count
	private static int maxApproxCount = 10;

	// termination Error window
	private static double probErrorWindow = 1.0e-3;

	// Use property based refinement
	private static boolean noPropRefine = false;

	// Rank transitions
	private static boolean rankTransitions = false;

	// Set cudd Memory limit
	private static String cuddMemoryLimit = "1g";

	// Saving variables
	private static boolean exportModel = false;

	// Saving filenames
	private static String exportFileName = null;


	//Variables for exporting perimeter states
	private static boolean exportPerimeterStates = false;
	private static String exportPerimeterFilename = null;

	// Import variables
	private static boolean importModel = false;

	// Import filenames
	private static String importFileName = null;

	// Specific Property
	private static boolean specificProperty = false;
	private static String property = null;

	// Should the transitions be exported to a file?
	private static String exportTransitionsToFile = null;

	/**
	 * Gets reachability threshold (&kappa;) as double.
	 * @return The reachability threshold.
	 */
	public static double getReachabilityThreshold() {
		return reachabilityThreshold;
	}
	/**
	 * Sets the reachability threshold (&kappa;).
	 * @param reach New reachability threshold.
	 */
	public static void setReachabilityThreshold(double reach) {
		reachabilityThreshold = reach;
	}
	/**
	 * Gets the reduction factor associated with the reachability
	 * threshold (r<sub>&kappa;</sub>).
	 * @return Kappa reduction factor.
	 */
	public static double getKappaReductionFactor() {
		return kappaReductionFactor;
	}
	/**
	 * Sets the reduction factor associated with the reachability
	 * threshold (r<sub>&kappa;</sub>).
	 * @param fac New kappa reduction factor
	 */
	public static void setKappaReductionFactor(double fac) {
		kappaReductionFactor = fac;
	}
	/**
	 * Gets the misprediction factor.
	 * @return The misprediction factor.
	 */
	public static double getMispredictionFactor() {
		return mispredictionFactor;
	}
	/**
	 * Sets a new misprediction factor.
	 * @param fac New misprediction factor.
	 */
	public static void setMispredictionFactor(double fac) {
		mispredictionFactor = fac;
	}
	/**
	 * Gets the max approximate count:  maximum number of iterations used in computing the upper and lower bounds of the
	 * probabilities of reaching a certain state.
	 * @return Max approximate count.
	 */
	public static int getMaxApproxCount() {
		return maxApproxCount;
	}
	/**
	 * Sets the maximum number of iterations used in computing the upper and lower bounds of the
	 * probabilities of reaching a certain state.
	 * @param rc The maximum number of iterations.
	 */
	public static void setMaxRefinementCount(int rc) {
		maxApproxCount = rc;
	}
	/**
	 * Gets the maximum allowed difference between P<sub>min</sub> and P<sub>max</sub>.
	 * @return Probability error window.
	 */
	public static double getProbErrorWindow() {
		return probErrorWindow;
	}
	/**
	 * Sets the maximum allowed difference between P<sub>min</sub> and P<sub>max</sub>.
	 * @param w The window to be set.
	 */
	public static void setProbErrorWindow(double w) {
		probErrorWindow = w;
	}
	/**
	 * Sets whether or not we are using property based refinement.
	 * @param o Whether or not to use property based refinement.
	 */
	public static void setNoPropRefine(boolean o) {
		noPropRefine = o;
	}
	/**
	 * Gets whether or not we are using property based refinement.
	 * @return Whether or not we are using property based refinement.
	 */
	public static boolean getNoPropRefine() {
		return noPropRefine;
	}
	/**
	 * Sets whether or not we are using rank transitions.
	 * @param o Rank transitions are used or not.
	 */
	public static void setRankTransitions(boolean o) {
		rankTransitions = o;
	}
	/**
	 * Gets whether or not we are using rank transitions.
	 * @return Whether rank transitions are used or not.
	 */
	public static boolean getRankTransitions() {
		return rankTransitions;
	}
	/**
	 * Sets the cudd memory limit available to STAMINA.
	 * @param limit The memory limit formatted as string.
	 */
	public static void setCuddMemoryLimit(String limit) {
		cuddMemoryLimit = new String(limit);
	}
	/**
	 * Gets the cudd memory limit available to STAMINA.
	 * @return The memory limit formatted as a string.
	 */
	public static String getCuddMemoryLimit() {
		return cuddMemoryLimit;
	}
	/**
	 * Gets whether or not we will be exporting the model
	 * @return Whether or not the model is to be exported.
	 */
	public static boolean getExportModel() {
		return exportModel;
	}
	/**
	 * Sets whether or not we will be exporting the model.
	 * @param e Whether or not the model is to be exported.
	 */
	public static void setExportModel(boolean e) {
		exportModel = e;
	}
	/**
	 * Gets the filename the model will be exported to.
	 * @return The filename.
	 */
	public static String getExportFileName() {
		return exportFileName;
	}
	/**
	 * Sets the filename the model will be exported to.
	 * @param s The filename.
	 */
	public static void setExportFileName(String s) {
		exportFileName = s;
	}
	/**
	 * Gets whether or not we are going to export perimeter states.
	 * @return If perimeter states are to be exported.
	 */
	public static boolean getExportPerimeterStates() {
		return exportPerimeterStates;
	}
	/**
	 * Sets whether or not we are going to export perimeter states.
	 * @param b If perimeter states are to be exported.
	 */
	public static void setExportPerimeterStates(boolean b) {
		exportPerimeterStates = b;
	}
	/**
	 * Gets the filename we will export perimter states to.
	 * @return The filename.
	 */
	public static String getExportPerimeterFilename() {
		return exportPerimeterFilename;
	}
	/**
	 * Sets the filename we will export perimeter states to.
	 * @param s The filename.
	 */
	public static void setExportPerimeterFilename(String s) {
		exportPerimeterFilename = s;
	}
	/**
	 * Gets whether or not we are going to import a model.
	 * @return Whether or not to import a model.
	 */
	public static boolean getImportModel() {
		return importModel;
	}
	/**
	 * Sets whether or not we are going to import a model.
	 * @param e Whether or not to import a model.
	 */
	public static void setImportModel(boolean e) {
		importModel = e;
	}
	/**
	 * Gets the filename we are going to import from.
	 * @return The filename.
	 */
	public static String getImportFileName() {
		return importFileName;
	}
	/**
	 * Sets the filename we are going to import from.
	 * @param s The filename.
	 */
	public static void setImportFileName(String s) {
		importFileName = s;
	}
	/**
	 * Gets whether or not this is a specific property.
	 * @return Whether or not this is a specific property.
	 */
	public static boolean getSpecificProperty() {
		return specificProperty;
	}
	/**
	 * Sets whether or not this is a specific property.
	 * @param b Whether or not this is a specific property.
	 */
	public static void setSpecificProperty(boolean b) {
		specificProperty = b;
	}
	/**
	 * Gets the property name of this property.
	 * @return The property name.
	 */
	public static String getPropertyName() {
		return property;
	}
	/**
	 * Sets the property name of this property.
	 * @param s The property name.
	 */
	public static void setPropertyName(String s) {
		property = s;
	}
	/**
	 * Gets the filename where the transition matrix will be
	 * exported.
	 * @return The filename.
	 */
	public static String getExportTransitionsToFile() {
		return exportTransitionsToFile;
	}
	/**
	 * Sets the filename where the transition matrix will be
	 * exported.
	 * @param b The filename.
	 */
	public static void setExportTransitionsToFile(String b) {
		exportTransitionsToFile = b;
	}
}
