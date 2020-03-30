package stamina;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;

import explicit.CTMC;
import explicit.CTMCModelChecker;
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
	public Result modelCheckStamina(PropertiesFile propertiesFile, Property prop) throws PrismException
	{
		
		Result[] res_min_max = new Result[2];
		
		double reachTh = Options.getReachabilityThreshold();
		
		// Instantiate and load model generator
		infModelGen = new InfCTMCModelGenerator(getPRISMModel(), this);
		super.loadModelGenerator(infModelGen);
		
		// Time bounds		
		double lTime, uTime;
		
		// Split property into 2 to find P_min and P_max
		
		String propName = prop.getName()==null ? "Prob" : prop.getName();
		
		Property prop_min = new Property(prop.getExpression().deepCopy());
		prop_min.setName(propName+"_min");
		modifyExpression(prop_min.getExpression(), true);
		
		Property prop_max = new Property(prop.getExpression().deepCopy());
		prop_max.setName(propName+"_max");
		modifyExpression(prop_max.getExpression(), false);
		
		
		// timer 
		long timer = 0;
		
		// iteration count
		int numRefineIteration = 0;
		
		// flag to switch optimized CTMC analysis
		boolean switchToCombinedCTMC = false;
		
		Expression exprProp = prop.getExpression();
		if(exprProp instanceof ExpressionProb) {
			
		
			while(numRefineIteration==0 || ((!terminateModelCheck(res_min_max[0].getResult(), res_min_max[1].getResult(), Options.getProbErrorWindow())) && (numRefineIteration < Options.getMaxApproxCount()))) {
				
				Expression expr = ((ExpressionProb) exprProp).getExpression();
				if(expr instanceof ExpressionTemporal) {
					
				//	expr = Expression.convertSimplePathFormulaToCanonicalForm(expr);
					ExpressionTemporal exprTemp = (ExpressionTemporal) expr.deepCopy();
					
					if(exprTemp.isPathFormula(false) && (exprTemp.getOperator()==ExpressionTemporal.P_U) && (!Options.getNoPropRefine())) {
						infModelGen.setPropertyExpression(exprTemp);
					}
					
					if(exprTemp.isPathFormula(false) && (exprTemp.getOperator()==ExpressionTemporal.P_U)) {
						switchToCombinedCTMC = true;
					}
				    	
					if(switchToCombinedCTMC) {
						
						//////////////////////////Approximation Step///////////////////////////
						mainLog.println();
						mainLog.println("========================================================================");
						mainLog.println("Approximation<" + (numRefineIteration+1) + "> : kappa = " + reachTh);
						mainLog.println("========================================================================");
						infModelGen.setReachabilityThreshold(reachTh);
						
						
						// Explicitely invoke model build
						super.buildModel();

						// model check operands first for all states
						explicit.CTMCModelChecker mcCTMC = new CTMCModelChecker(this);
						BitSet b1 = mcCTMC.checkExpression(super.getBuiltModelExplicit(), exprTemp.getOperand1(), null).getBitSet();
						BitSet b2 = mcCTMC.checkExpression(super.getBuiltModelExplicit(), exprTemp.getOperand2(), null).getBitSet();
						
						BitSet minStatesNeg = (BitSet) b1.clone();
						minStatesNeg.andNot(b2);
						
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
						
						if(lTime>0.0) throw new PrismException("Currently only supports [0,t] time bound.");
						
						// verification step
						mainLog.println();
						mainLog.println("---------------------------------------------------------------------");
						mainLog.println();
						mainLog.println("Verifying " + propName + " .....");
						
						timer = System.currentTimeMillis();
						
						// run transient analysis
						explicit.StateValues probsExpl = mcCTMC.doTransient((CTMC) super.getBuiltModelExplicit(), uTime);
						
						double ans_min = 0.0;
						
						for(int i=0; i<super.getBuiltModelExplicit().getNumStates(); ++i) {
							
							if(b2.get(i)) ans_min += (double) probsExpl.getValue(i);
							
						}
						
						// TODO: need the index of absorbing state
						double ans_max = ans_min + (double) probsExpl.getValue(0);
						ans_max = ans_max > 1 ? 1.0 : ans_max;
						
						timer = System.currentTimeMillis() - timer;
						mainLog.println("\nTime for model checking: " + timer / 1000.0 + " seconds.");
						
						// set results
						res_min_max[0] = new Result(ans_min);
						res_min_max[0].setExplanation("Minimum Bound".toLowerCase());
						
						// Print result to log
						mainLog.print("\nResult: " + res_min_max[0].getResultString() + "\n");
						
						res_min_max[1] = new Result(ans_max);
						res_min_max[1].setExplanation("Maximum Bound".toLowerCase());
						
						// Print result to log
						mainLog.print("\nResult: " + res_min_max[1].getResultString() + "\n");
                        String filename = "results.txt";
                        try {
                        File file = new File(filename);
                        file.delete();
				        FileWriter writer = new FileWriter("results.txt");
                        writer.write(Double.toString(ans_min));
                        writer.write("\r\n");
                        writer.write(Double.toString(ans_max));
                        writer.write("\r\n");
                        writer.close();
                        } catch(IOException e) {
                            // Do nothing for now
                        }
					}
					
					else {
					
						//////////////////////////Approximation Step///////////////////////////
						mainLog.println();
						mainLog.println("========================================================================");
						mainLog.println("Approximation<" + (numRefineIteration+1) + "> : kappa = " + reachTh);
						mainLog.println("========================================================================");
						infModelGen.setReachabilityThreshold(reachTh);
						
						// Explicitely invoke model build
						super.buildModel();
					
						
						mainLog.println();
						mainLog.println("---------------------------------------------------------------------");
						mainLog.println();
						mainLog.println("Verifying Lower Bound for " + prop_min.getName() + " .....");
						res_min_max[0] = super.modelCheck(propertiesFile, prop_min);
						
						mainLog.println();
						mainLog.println("---------------------------------------------------------------------");
						mainLog.println();
						mainLog.println("Verifying Upper Bound for " + prop_max.getName() + " .....");
						res_min_max[1] = super.modelCheck(propertiesFile, prop_max);
						String filename = "results.txt";
                        try {
                        File file = new File(filename);
                        file.delete();
				        FileWriter writer = new FileWriter("results.txt");
                        writer.write(res_min_max[0].toString());
                        writer.write("\r\n");
                        writer.write(res_min_max[1].toString());
                        writer.write("\r\n");
                        writer.close();
                        } catch(IOException e) {
                            // Do nothing for now
                        }
					}
					
					
					// Reduce kappa for refinement
					reachTh /= Options.getKappaReductionFactor();
					
					// increment refinement count
					++numRefineIteration;
					
				}
			}
			
		}
		
		// Print the final result
		mainLog.println();
		mainLog.println("========================================================================");
		mainLog.println();
		mainLog.println("Property: " + prop);
		mainLog.println();
		mainLog.println("ProbMin: " + res_min_max[0].getResultString());
		mainLog.println();
		mainLog.println("ProbMax: " + res_min_max[1].getResultString());
		mainLog.println();
		mainLog.println("========================================================================");
		
		return res_min_max[0];
		
	}

}
