package stamina;

import java.io.*;
import java.util.*;

import parser.State;
import parser.ast.*;
import parser.type.TypeInt;
import prism.ModelType;
import prism.*;

import common.IterableStateSet;
// import common.iterable.IterableInt;
import common.iterable.MappingIterator;
import parser.VarList;
import explicit.*;

public class StaminaModelChecker extends Prism {

	private StaminaModelGenerator infModelGen = null;

	/**
	 * Construct a new Prism object.
	 * @param mainLog PrismLog where all output will be sent.
	 */
	public StaminaModelChecker() {
		super(StaminaLog.getMainLog());
		try {
			super.setCUDDMaxMem(Options.getCuddMemoryLimit());
		}
		catch (Exception e) {
			StaminaLog.error("Got error when trying to set CUDD max memory limit:" + e);
		}
	}

	/**
	 * Modifies an expression to produce either the expression needed to calculate Pmin or Pmax.
	 * @param expr Expression to modify.
	 * @param isMin Expression is minimum.
	 * @throws PrismLangException
	 */
	private void modifyExpression(Expression expr, boolean isMin) throws PrismLangException {

		if (expr instanceof ExpressionBinaryOp) {
			Expression op1 = ((ExpressionBinaryOp) expr).getOperand1();
			if (op1 instanceof ExpressionVar && ExpressionBinaryOp.isRelOp(((ExpressionBinaryOp) expr).getOperator()) ) {

				State absSt = infModelGen.getAbsorbingState();
				String varName = ((ExpressionVar) op1).getName();


				Expression newOp1 = new ExpressionBinaryOp(((ExpressionBinaryOp) expr).getOperator(), ((ExpressionBinaryOp) expr).getOperand1(), ((ExpressionBinaryOp) expr).getOperand2());

				Expression abs = new ExpressionBinaryOp(ExpressionBinaryOp.EQ, ((ExpressionBinaryOp) expr).getOperand1(), new ExpressionLiteral(TypeInt.getInstance(), absSt.varValues[getPRISMModel().getVarIndex(varName)]));

				if (isMin) {
					Expression newOp2 = new ExpressionUnaryOp(ExpressionUnaryOp.PARENTH, new ExpressionUnaryOp(ExpressionUnaryOp.NOT, abs));

					((ExpressionBinaryOp) expr).setOperator("&");
					((ExpressionBinaryOp) expr).setOperand1(newOp1);
					((ExpressionBinaryOp) expr).setOperand2(newOp2);
				}
				else {
					Expression newOp2 = new ExpressionUnaryOp(ExpressionUnaryOp.PARENTH, abs);

					((ExpressionBinaryOp) expr).setOperator("|");
					((ExpressionBinaryOp) expr).setOperand1(newOp1);
					((ExpressionBinaryOp) expr).setOperand2(newOp2);
				}
			}
			else {
				modifyExpression(((ExpressionBinaryOp) expr).getOperand1(), isMin);
				modifyExpression(((ExpressionBinaryOp) expr).getOperand2(), isMin);
			}
		}
		else if (expr instanceof ExpressionProb) {
			modifyExpression(((ExpressionProb) expr).getExpression(), isMin);
		}
		else if (expr instanceof ExpressionTemporal) {
			modifyExpression(((ExpressionTemporal) expr).getOperand1(), isMin);
			modifyExpression(((ExpressionTemporal) expr).getOperand2(), isMin);
		}
		else if (expr instanceof ExpressionUnaryOp) {
			modifyExpression(((ExpressionUnaryOp) expr).getOperand(), isMin);
		}

		//return expr;
	}

	/**
	 * Whether or not, according to our bounding of probabilities, we can terminate the model checking.
	 * @param minProb The lower bound on the probability.
	 * @param maxProb The upper bound on the probability.
	 * @param termParam The (tight) range we want for Pmax - Pmin
	 * @return Whether or not the model achieved maxProb - minProb <= termParam. I.e., whether or not
	 * we were successful in state space approximation.
	 */
	private boolean terminateModelCheck(Object minProb, Object maxProb, double termParam) {
		if ((minProb instanceof Boolean) && (maxProb instanceof Boolean)){
				boolean terminateRefinment = !(((boolean) minProb) ^ ((boolean) maxProb));
				return terminateRefinment;
		}
		else if ((minProb instanceof Double) && (maxProb instanceof Double)) {
			boolean terminateRefinment = (((double) maxProb) - ((double) minProb)) <= termParam;
			return terminateRefinment;
		}
		else {
			return false;
		}
	}

	/**
	 * Perform model checking of a property on the currently loaded model and return result.
	 * @param propertiesFile Parent property file of property (for labels/constants/...)
	 * @param prop The property to check
	 * @throws FileNotFoundException
	 */
	public Result modelCheckStamina(PropertiesFile propertiesFile, Property prop) throws PrismException {
		Result[] resultsMinMax = new Result[2];
		double reachTh = Options.getReachabilityThreshold();

		// Instantiate and load model generator
		ModulesFile pModel = getPRISMModel();
		infModelGen = new StaminaModelGenerator(getPRISMModel(), this);
		// For some reason this sets the prism model to null
		super.loadModelGenerator(infModelGen);
		super.loadPRISMModel(pModel);
		if (getPRISMModel() == null) { StaminaLog.log("it's null here."); }


		// Time bounds
		double lTime, uTime;

		// Split property into 2 to find P_min and P_max
		String propName = prop.getName() == null ? "Prob" : prop.getName();
		Property prop_min = new Property(prop.getExpression().deepCopy());
		prop_min.setName(propName + "_min");
		modifyExpression(prop_min.getExpression(), true);

		Property prop_max = new Property(prop.getExpression().deepCopy());
		prop_max.setName(propName + "_max");
		modifyExpression(prop_max.getExpression(), false);

		// timer
		long timer = 0;

		// iteration count
		int numRefineIteration = 0;

		// flag to switch optimized CTMC analysis
		boolean switchToCombinedCTMC = false;

		Expression exprProp = prop.getExpression();
		if (!(exprProp instanceof ExpressionProb)) {
			return null;
		}

		Expression expr = ((ExpressionProb) exprProp).getExpression();
		while (numRefineIteration == 0 ||
			(!terminateModelCheck(resultsMinMax[0].getResult(), resultsMinMax[1].getResult(), Options.getProbErrorWindow()) && numRefineIteration < Options.getMaxApproxCount())) {
			reachTh = Options.getReachabilityThreshold();
			if (!(expr instanceof ExpressionTemporal)) {
				continue;
			}
			ExpressionTemporal exprTemp = (ExpressionTemporal) expr.deepCopy();

			if (exprTemp.isPathFormula(false) && exprTemp.getOperator() == ExpressionTemporal.P_U) {
				if (!Options.getNoPropRefine()) {
					infModelGen.setPropertyExpression(exprTemp);
				}
				switchToCombinedCTMC = !Options.getNoPropRefine();
			}

			// Approximation Step
			StaminaLog.header("Approximation [" + (numRefineIteration + 1) + "] : kappa = " + reachTh);
			infModelGen.setReachabilityThreshold(reachTh);
			if (switchToCombinedCTMC) {
				// Explicitely invoke model build
				if (Options.getImportModel()) {
					importModel();
				}
				super.buildModel();
				if (Options.getExportModel()) {
					exportModel(propertiesFile);
				}

				// model check operands first for all states
				explicit.CTMCModelChecker mcCTMC = new CTMCModelChecker(this);
				BitSet b2 = mcCTMC.checkExpression(super.getBuiltModelExplicit(), exprTemp.getOperand2(), null).getBitSet();

				// lower bound is 0 if not specified
				// (i.e. if until is of form U<=t)
				Expression timeExpr = exprTemp.getLowerBound();
				if (timeExpr != null) {
					lTime = timeExpr.evaluateDouble(mcCTMC.getConstantValues());
					if (lTime < 0) {
						throw new PrismException("Invalid lower bound " + lTime + " in time-bounded until formula");
					}
				} else {
					lTime = 0;
				}
				// upper bound is -1 if not specified
				// (i.e. if until is of form U>=t)
				timeExpr = exprTemp.getUpperBound();
				if (timeExpr != null) {
					uTime = timeExpr.evaluateDouble(mcCTMC.getConstantValues());
					if (uTime < 0 || (uTime == 0 && exprTemp.upperBoundIsStrict())) {
						String bound = (exprTemp.upperBoundIsStrict() ? "<" : "<=") + uTime;
						throw new PrismException("Invalid upper bound " + bound + " in time-bounded until formula");
					}
					if (uTime < lTime) {
						throw new PrismException("Upper bound must exceed lower bound in time-bounded until formula");
					}
				} else {
					uTime = -1;
				}

				if (lTime > 0.0) throw new PrismException("Currently only supports [0,t] time bound.");

				// verification step
				StaminaLog.endSection();
				StaminaLog.log("Verifying " + propName + "...");

				timer = System.currentTimeMillis();

				// run transient analysis
				explicit.StateValues probsExpl = mcCTMC.doTransient((CTMC) super.getBuiltModelExplicit(), uTime);

				double ansMin = 0.0;
				double ansMax;

				// Check if the model has an abosrbing state (i.e. not all are generated)
				// If it it does, it will be the first state and we don't want to add it to
				// ansMin, so we start the for loop and i=1
				if (infModelGen.finalModelHasAbsorbing()) {
					for(int i=1; i<super.getBuiltModelExplicit().getNumStates(); ++i) {
						if (b2.get(i)) { ansMin += (double) probsExpl.getValue(i); }
					}

					ansMax = ansMin + (double) probsExpl.getValue(0);
					ansMax = ansMax > 1 ? 1.0 : ansMax;
				}
				else {
					for(int i=0; i<super.getBuiltModelExplicit().getNumStates(); ++i) {
						if (b2.get(i)) { ansMin += (double) probsExpl.getValue(i); }
					}
					// If there is no absorbing state, we know the solution exactly.
					ansMax = ansMin;
				}



				timer = System.currentTimeMillis() - timer;
				StaminaLog.log("Time for model checking: " + timer / 1000.0 + " seconds.");

				// set results
				resultsMinMax[0] = new Result(ansMin);
				resultsMinMax[0].setExplanation("minimum bound");

				// Print result to log
				StaminaLog.log("Result: " + resultsMinMax[0].getResultString());

				resultsMinMax[1] = new Result(ansMax);
				resultsMinMax[1].setExplanation("maximum bound");

				// Print result to log
				StaminaLog.log("Result: " + resultsMinMax[1].getResultString());
				writeResults(resultsMinMax); // TODO: allow this to turn off
			}
			else {
				// Explicitely invoke model build
				super.buildModel();

				StaminaLog.endSection();
				StaminaLog.log("Verifying Lower Bound for " + prop_min.getName() + "...");
				resultsMinMax[0] = super.modelCheck(propertiesFile, prop_min);

				StaminaLog.endSection();
				StaminaLog.log("Verifying Upper Bound for " + prop_max.getName() + "...");
				resultsMinMax[1] = super.modelCheck(propertiesFile, prop_max);
				writeResults(resultsMinMax); // TODO: allow this to turn off

				if (Options.getExportModel()) {
					exportModel(propertiesFile);
				}
			}


			// We need to check how far off our current results are from our goal
			// If we are way off, our estimate is further off than we want, so we update
			// the misprediction factor in proportion to our percentage off.
			// Here we multiply the percentoff by four but cap it at 100. Both 4 and 100 were determined heuristically through testing.
			double percentOff = 4 * ((Double) resultsMinMax[1].getResult() - (Double) resultsMinMax[0].getResult()) / Options.getProbErrorWindow();
			if (percentOff > 100) {
				percentOff = 100;
			}
			Options.setMispredictionFactor(Options.getMispredictionFactor() * percentOff);

			// increment refinement count
			if (Options.getExportPerimeterStates()) {
				exportPerimeterStates(infModelGen, numRefineIteration);
			}
			infModelGen.clearPerimeterStatesVector();
			++numRefineIteration;
		}


		// Print the final result
		StaminaLog.logResult(prop.toString(), resultsMinMax[0].getResultString(), resultsMinMax[1].getResultString());

		if (Options.getExportTransitionsToFile() != null) {
			StaminaLog.log("\n\nExporting transition list...");
			printTransitionActions(infModelGen, Options.getExportTransitionsToFile());
			StaminaLog.log("Export Complete");
		}

		return resultsMinMax[0];

	}
	/**
	 * Prints all transition actions taken by the StaminaModelGenerator to a file.
	 * @param modelGen The model generator we're using.
	 * @param exportFileName The file we're exporting to.
	 * @throws PrismException
	 */
	private void printTransitionActions(StaminaModelGenerator modelGen, String exportFileName) throws PrismException{

		// Stamina naturally sorts states in the order it encounters them, but PRISM prints out transitions
		// sorted in the natural ordering of their variable values. So, we need to sort the states the same
		// was as PRISM before printing them out to be consistent.
		HashMap<State, ProbState> globalStateSet = modelGen.getGlobalStateSet();
		TreeSet<State> sortedStates = new TreeSet<State>(globalStateSet.keySet());
		if (modelGen.finalModelHasAbsorbing()) {
			sortedStates.add(modelGen.getAbsorbingState());
		}
		HashMap<State, Integer> stateIndex = new HashMap<State,Integer>();

		// This loops gets the indexes that PRISM would use
		int spot = 0;
		for (State sortState : sortedStates) {
			stateIndex.put(sortState, spot);
			spot++;
		}

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(exportFileName));

			for (State exploredState : sortedStates) {
				modelGen.exploreState(exploredState);
				// Look at each outgoing choice in turn
				int nc = modelGen.getNumChoices();
				TreeMap<State, ArrayList<Integer>> sortedTrans = new TreeMap<State, ArrayList<Integer>>(sortedStates.comparator());
				for (int i = 0; i < nc; i++) {
					// Look at each transition in the choice
					int nt = modelGen.getNumTransitions(i);
					for (int j = 0; j < nt; j++) {
						State stateNew = modelGen.computeTransitionTarget(i, j);
						ArrayList<Integer> currentList = sortedTrans.get(stateNew);
						if (currentList == null) {
							currentList = new ArrayList<Integer>();

						}
						// This is a single list where the even elements are the choices and the odd
						// elements are the transitions. This keeps track of the transitions in the
						// order that PRISM would use.
						currentList.add(i);
						currentList.add(j);

						sortedTrans.put(stateNew, currentList);
					}
				}

				// Print out the sorted list of transitions
				while(!sortedTrans.isEmpty()) {
					Map.Entry<State, ArrayList<Integer>> mapping = sortedTrans.pollFirstEntry();
					State stateNew = mapping.getKey();
					ArrayList<Integer> indexList = mapping.getValue();
					int size = indexList.size() / 2;
					for (int k = 0; k < size; k++) {
						// Get the next choice transition combination from the list
						int i = indexList.get(2*k);
						int j = indexList.get(2*k + 1);

						out.write(stateIndex.get(exploredState) + " " + stateIndex.get(stateNew) + " " + PrismUtils.formatDouble(modelGen.getTransitionProbability(i,j)) + " " + modelGen.getTransitionAction(i,j));
						out.newLine();
					}
				}
			}
			out.close();
		}
		catch (Exception e) {
			StaminaLog.error("An error occurred creating the transition file");
			e.printStackTrace();
		}
	}

	private void exportModel(PropertiesFile propertiesFile) throws PrismException {
		try {
			int exportType = Options.getMrmc() ? Prism.EXPORT_MRMC : Prism.EXPORT_PLAIN;
			String suffix = Options.getMrmc() ? ".mrmc" : "";
			String exportFilename = Options.getExportFileName();
			String transFile = exportFilename + ".tra" + suffix;
			String stateRewardsFile = exportFilename + "srew" + suffix;
			String transRewardsFile = exportFilename + ".trew" + suffix;
			String statesFile = exportFilename + ".sta" + suffix;
			String labelsFile = exportFilename + ".lab" + suffix;
			super.exportTransToFile(true, exportType, new File(transFile));
			super.exportStateRewardsToFile(exportType, new File (stateRewardsFile));
			super.exportTransRewardsToFile(true, exportType, new File(transRewardsFile));
			super.exportStatesToFile(exportType, new File(statesFile));
			super.exportLabelsToFile(propertiesFile, exportType, new File(labelsFile));
		} catch (FileNotFoundException e) {
			// throw e;
			throw new PrismException("Cannot open file for exporting " + e.toString());
		} catch (Exception e) {
			throw new PrismException(e.toString());
		}
	}

	private void importModel() throws PrismException {
		File sf = null, lf = null, srf = null, mf = null;
		try {
			String filename = Options.getImportFileName();
			String transFile = filename + ".tra";
			String stateRewardsFile = filename + "srew";
			String transRewardsFile = filename + ".trew";
			String statesFile = filename + ".sta";
			String labelsFile = filename + ".lab";
			sf = new File(statesFile);
			lf = new File(labelsFile);
			srf = new File(stateRewardsFile);
			mf = new File(transFile);
			ArrayList<File> srfl = new ArrayList<File>();
			srfl.add(srf);
			super.loadModelFromExplicitFiles(sf, mf, lf, srfl, ModelType.CTMC);
		} catch (Exception e) {
			throw new PrismException(e.toString());
		}
	}

	private void writeResults(Result[] results) throws PrismException {
		if (results.length != 2) {
			throw new PrismException("Should have two results! Min and Max!");
		}
		String filename = "results.txt";
		try {
			File file = new File(filename);
			file.delete();
			FileWriter writer = new FileWriter("results.txt");
			writer.write(results[0].toString());
			writer.write("\r\n");
			writer.write(results[1].toString());
			writer.write("\r\n");
			writer.close();
		} catch(IOException e) {
			// Do nothing for now
		}
	}

	private void exportPerimeterStates(StaminaModelGenerator infModelGen, int numRefineIteration) throws PrismException {
		try {
			FileWriter writer = new FileWriter(Options.getExportPerimeterFilename(), true);
			Vector<String> values = infModelGen.getPerimeterStatesVector();
			writer.write("Iteration: " + Integer.toString(numRefineIteration) + "\r\n");
			for (int i = 0; i < infModelGen.getNumVars(); ++i) {
				writer.write(infModelGen.getVarName(i) + ",");
			}
			writer.write("Current Reachability Probability\r\n");
			for (int i = 0; i<values.size(); ++i) {
				writer.write(values.get(i) + "\r\n");
			}
			writer.close();
		} catch (IOException e) {
			// throw e;
			throw new PrismException("Cannot handle file for exporting " + e.toString());
		} catch (Exception e) {
			throw new PrismException(e.toString());
		}
	}
}
