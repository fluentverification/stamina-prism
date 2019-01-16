package stamina;

import java.io.FileNotFoundException;
import parser.State;
import parser.ast.Expression;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionLiteral;
import parser.ast.ExpressionProb;
import parser.ast.ExpressionTemporal;
import parser.ast.ExpressionUnaryOp;
import parser.ast.ExpressionVar;
import parser.ast.PropertiesFile;
import parser.ast.Property;
import parser.type.TypeInt;
import prism.Prism;
import prism.PrismException;
import prism.PrismLangException;
import prism.PrismLog;
import prism.Result;

public class StaminaModelChecker extends Prism {
	
	
	////////////////////////////////////
	private InfCTMCModelGenerator infModelGen = null;
	
	/**
	 * Construct a new Prism object.
	 * @param mainLog PrismLog where all output will be sent.
	 */
	public StaminaModelChecker(PrismLog mainLog) {
		
		super(mainLog);
		
	}
	
	
	private void modifyExpression(Expression expr, boolean isMin) throws PrismLangException {
		
		if(expr instanceof ExpressionBinaryOp) {
			Expression op1 = ((ExpressionBinaryOp) expr).getOperand1();
			if(op1 instanceof ExpressionVar && ExpressionBinaryOp.isRelOp(((ExpressionBinaryOp) expr).getOperator()) ) {
				
				State absSt = infModelGen.getAbsorbingState();
				String varName = ((ExpressionVar) op1).getName();
				
				
				Expression newOp1 = new ExpressionBinaryOp(((ExpressionBinaryOp) expr).getOperator(), ((ExpressionBinaryOp) expr).getOperand1(), ((ExpressionBinaryOp) expr).getOperand2());
				
				Expression abs = new ExpressionBinaryOp(ExpressionBinaryOp.EQ, ((ExpressionBinaryOp) expr).getOperand1(), new ExpressionLiteral(TypeInt.getInstance(), absSt.varValues[getPRISMModel().getVarIndex(varName)]));
				
				if(isMin) {
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
		else if(expr instanceof ExpressionProb) {
			modifyExpression(((ExpressionProb) expr).getExpression(), isMin);
		}
		else if(expr instanceof ExpressionTemporal) {
			modifyExpression(((ExpressionTemporal) expr).getOperand1(), isMin);
			modifyExpression(((ExpressionTemporal) expr).getOperand2(), isMin);
		}
		else if(expr instanceof ExpressionUnaryOp) {
			modifyExpression(((ExpressionUnaryOp) expr).getOperand(), isMin);
		}
		
		//return expr;
	}
	
	
	private boolean terminateModelCheck(Object minProb, Object maxProb, double termParam) {
		
		if( (minProb instanceof Boolean) && (maxProb instanceof Boolean)){
				
				boolean terminateRefinment = !(((boolean) minProb) ^ ((boolean) maxProb));
				return terminateRefinment;
		}
		else if((minProb instanceof Double) && (maxProb instanceof Double)) {
			
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
	public Result modelCheck(PropertiesFile propertiesFile, Property prop) throws PrismException
	{
		
		double reachTh = Options.getReachabilityThreshold();
		
		Result[] res_min_max = new Result[2];
		
		infModelGen = new InfCTMCModelGenerator(getPRISMModel(), this);
		infModelGen.setReachabilityThreshold(reachTh);
		
		super.loadModelGenerator(infModelGen);
		
		String propName = prop.getName()==null ? "Prob" : prop.getName();
		
		Property prop_min = new Property(prop.getExpression().deepCopy());
		prop_min.setName(propName+"_min");
		modifyExpression(prop_min.getExpression(), true);
		
		Property prop_max = new Property(prop.getExpression().deepCopy());
		prop_max.setName(propName+"_max");
		modifyExpression(prop_max.getExpression(), false);
		
		res_min_max[0] = super.modelCheck(propertiesFile, prop_min);
		res_min_max[1] = super.modelCheck(propertiesFile, prop_max);
	
			
		Expression exprProp = prop.getExpression();
		if(exprProp instanceof ExpressionProb) {
			
		
			while(!terminateModelCheck(res_min_max[0].getResult(), res_min_max[1].getResult(), 1.0e-3)) {
				
				Expression expr = ((ExpressionProb) exprProp).getExpression();
				if(expr instanceof ExpressionTemporal) {
					
					expr = Expression.convertSimplePathFormulaToCanonicalForm(expr);
					ExpressionTemporal exprTemp = (ExpressionTemporal) expr.deepCopy();
					
					if(exprTemp.isPathFormula(false)) {
						infModelGen.setPropertyExpression(exprTemp);
					}
					
							
					reachTh /= Options.getKappaReductionFactor();
					mainLog.println("\n\n\nReachability Threshold(kappa) is: " + reachTh);
					infModelGen.setReachabilityThreshold(reachTh);
					
					// Explicitely invoke model build
					super.buildModel();
					
					
					res_min_max[0] = super.modelCheck(propertiesFile, prop_min);
					res_min_max[1] = super.modelCheck(propertiesFile, prop_max);
					
				}
			}
			
		}
		
		
		return res_min_max[0];
		
	}	
	
	
	////////////////////////////////////// Options class ////////////////
	public static class Options {
		
		//Probabilistic state search termination value : Defined by kappa in command line argument
		private static double reachabilityThreshold = 1e-6;
		
		// Kappa reduction factor
		private static double kappaReductionFactor = 1000.0;
			
		// max number of approx-refinement limit 
		private static int maxApproxRefineCount = 10;
		
		// termination Error window
		private static double probErrorWindow = 1.0e-3;
		
		//////////////////////////////////// Command lines args to pass to prism ///////////////////
		// Solutions method max iteration
		//private static int maxLinearSolnIter;
		
		// Solution method
		//private static int solutionMethod;
		
		//private  Options() {}
		
		
		public static double getReachabilityThreshold() {
			return reachabilityThreshold;
		}
		
		public static void setReachabilityThreshold(double reach) {
			reachabilityThreshold = reach;
		}
		
		public static double getKappaReductionFactor() {
			return kappaReductionFactor;
		}
		
		public static void setgetKappaReductionFactor(double fac) {
			kappaReductionFactor = fac;
		}
		
		public static int getMaxApproxRefineCount() {
			return maxApproxRefineCount;
		}
		
		public static void setMaxApproxRefineCount(int arc) {
			maxApproxRefineCount = arc;
		}
		
		public static double getProbErrorWindow() {
			return probErrorWindow;
		}
		
		public static void setProbErrorWindow(double w) {
			probErrorWindow = w;
		}
	}

}
