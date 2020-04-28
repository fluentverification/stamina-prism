package stamina;

public class Options {

	//Probabilistic state search termination value : Defined by kappa in command line argument
	private static double reachabilityThreshold = 1e-6;
	
	// Kappa reduction factor
	private static double kappaReductionFactor = 1000.0;
	
	// max number of refinement count 
	private static int maxApproxCount = 10;
	
	// termination Error window
	private static double probErrorWindow = 1.0e-3;
	
	// Use property based refinement
	private static boolean noPropRefine = false;

	private static boolean rankTransitions = false;

	private static String cuddMemoryLimit = "1g";	

	// Saving variables
	private static boolean exportModel = false;

	// Saving filenames
	private static String exportFileName = null;

	private static boolean exportPerimeterStates = false;
	private static String exportPerimeterFilename = null;
    // Import variables
    private static boolean importModel = false;

    // Import filenames
    private static String importFileName = null;

    // Specific Property
    private static boolean specificProperty = false;
    private static String property = null;
	
	public static double getReachabilityThreshold() {
		return reachabilityThreshold;
	}
	
	public static void setReachabilityThreshold(double reach) {
		reachabilityThreshold = reach;
	}
	
	public static double getKappaReductionFactor() {
		return kappaReductionFactor;
	}
	
	public static void setKappaReductionFactor(double fac) {
		kappaReductionFactor = fac;
	}
	
	public static int getMaxApproxCount() {
		return maxApproxCount;
	}
	
	public static void setMaxRefinementCount(int rc) {
		maxApproxCount = rc;
	}
	
	public static double getProbErrorWindow() {
		return probErrorWindow;
	}
	
	public static void setProbErrorWindow(double w) {
		probErrorWindow = w;
	}
	
	public static void setNoPropRefine(boolean o) {
		noPropRefine = o;
	}
	
	public static boolean getNoPropRefine() {
		return noPropRefine;
	}

	public static void setRankTransitions(boolean o) {
		rankTransitions = o;
	}

	public static boolean getRankTransitions() {
		return rankTransitions;
	}

	public static void setCuddMemoryLimit(String limit) {
		cuddMemoryLimit = new String(limit);
	}

	public static String getCuddMemoryLimit() {
		return cuddMemoryLimit;
	}

	public static boolean getExportModel() {
		return exportModel;
	}

	public static void setExportModel(boolean e) {
		exportModel = e;
	}

	public static String getExportFileName() {
		return exportFileName;
	}

	public static void setExportFileName(String s) {
		exportFileName = s;
	}

	public static boolean getExportPerimeterStates() {
		return exportPerimeterStates;
	}
	public static void setExportPerimeterStates(boolean b) {
		exportPerimeterStates = b;
	}

	public static String getExportPerimeterFilename() {
		return exportPerimeterFilename;
	}
	public static void setExportPerimeterFilename(String s) {
		exportPerimeterFilename = s;
	}
    public static boolean getImportModel() {
        return importModel;
    }

    public static void setImportModel(boolean e) {
        importModel = e;
    }

    public static String getImportFileName() {
        return importFileName;
    }

    public static void setImportFileName(String s) {
        importFileName = s;
    }

    public static boolean getSpecificProperty() {
        return specificProperty;
    }

    public static void setSpecificProperty(boolean b) {
        specificProperty = b;
    }

    public static String getPropertyName() {
        return property;
    }

    public static void setPropertyName(String s) {
        property = s;
    }
}
