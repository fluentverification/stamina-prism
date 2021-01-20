package stamina;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import simulator.ModulesFileModelGenerator;
import prism.ProgressDisplay;
import parser.ast.ExpressionTemporal;
import parser.Values;
import parser.State;
import prism.PrismException;


public class ReachabilityThread implements Runnable{
    public ReachabilityThread(ModulesFileModelGenerator modelGen, List<ProbState> exploredK, Set<ProbState> statesK, ProgressDisplay progress, AtomicInteger counter, ExpressionTemporal propertyExpression, Values mfConstants, double reachabilityThreshold) {
        this.modelGen = modelGen;
        this.exploredK = exploredK;
        this.statesK = statesK;
        this.progress = progress;
        this.counter = counter;
        this.propertyExpression = propertyExpression;
        this.mfConstants = mfConstants;
        this.reachabilityThreshold = reachabilityThreshold;
    }

    public void mainThread() throws PrismException {
        do {
            if(!exploredK.isEmpty()) {
                counter.incrementAndGet();
                while (!exploredK.isEmpty()) {

                    ProbState curProbState;
                    try {
                        curProbState = exploredK.remove(0);
                    }
                    catch (Exception e) {
                        break;
                    }
                    
                    //statesK.remove(curProbState);
                    
                    //System.out.println("\nExplored exactly one time\n");
                    // Explore all choices/transitions from this state
                    //double trackPrismTime = System.currentTimeMillis();
                    modelGen.exploreState(curProbState);
                    //prismTime += System.currentTimeMillis() - trackPrismTime;
                    
                    /////////////////////////////////////////////
                    //double propCheckTimeHelper = System.currentTimeMillis();
                    if(propertyExpression!=null) {
                        
                        ExpressionTemporal tempProp = (ExpressionTemporal) propertyExpression;
                        
                        boolean b1 = (boolean) tempProp.getOperand1().evaluate(mfConstants, curProbState);
                        boolean b2 = (boolean) tempProp.getOperand2().evaluate(mfConstants, curProbState);
                        
                        if(!(b1&&(!b2))) {
                            curProbState.setStateAbsorbing(true);
                            //perimeterStates.addElement(curProbState.toString());
                            curProbState.setStateTerminal(false);
                            continue;
                        }
                    }
                    //propCheckTime += System.currentTimeMillis() - propCheckTimeHelper;
                    
                    ////////////////////////////////////////////
                    
                    
                    double curStateReachability = curProbState.getCurReachabilityProb();
                    /*System.out.println("\nState");
                    System.out.println(curProbState);
                    System.out.println(curStateReachability);*/
                    if(!curProbState.isStateTerminal() || curStateReachability >= reachabilityThreshold) {
                        //prismTime += System.currentTimeMillis() - trackPrismTime

                        if(curStateReachability == 0) {
                            int nc = modelGen.getNumChoices();
                            for (int i = 0; i < nc; i++) {
                                // Look at each transition in the choice
                                int nt = modelGen.getNumTransitions(i);
                                for (int j = 0; j < nt; j++) {
                                    State nxtSt = modelGen.computeTransitionTarget(i, j);
                                    ProbState nxtProbState = InfCTMCModelGenerator.addToExplore(nxtSt);
				    if (nxtProbState != null) {
				    	if(statesK.add(nxtProbState)){
						exploredK.add(nxtProbState);
					}
				    }
                                }
                            }
                        }
                        else {

                            double exitRateSum = 0.0;
                            //trackPrismTime = System.currentTimeMillis();
                            // Look at each outgoing choice in turn
                            int nc = modelGen.getNumChoices();
                            for (int i = 0; i < nc; i++) {
                                // Look at each transition in the choice
                                int nt = modelGen.getNumTransitions(i);
                                for (int j = 0; j < nt; j++) {
                                    exitRateSum += modelGen.getTransitionProbability(i, j);
                                }
                            }

                            for (int i = 0; i < nc; i++) {
                                // Look at each transition in the choice
                                //System.out.println("\nA choice\n");
                                int nt = modelGen.getNumTransitions(i);
                                for (int j = 0; j < nt; j++) {
                                    //trackPrismTime = System.currentTimeMillis();
                                    State nxtSt = modelGen.computeTransitionTarget(i, j);
                                    //prismTime += System.currentTimeMillis() - trackPrismTime;
                               

                                    //trackPrismTime = System.currentTimeMillis();
                                    double tranRate = modelGen.getTransitionProbability(i, j);
                                    //prismTime += System.currentTimeMillis() - trackPrismTime;
                                    //compute next reachability probability for nextState
                                    double tranProb = tranRate/exitRateSum;
                                    //double curProb = nxtProbState.getCurReachabilityProb();

                                    //double mapStart = System.currentTimeMillis();
                                    
                                    double leavingProb = tranProb*curStateReachability;

                                    ProbState nxtProbState = InfCTMCModelGenerator.addToStateMap(nxtSt);

                                    nxtProbState.addToReachability(leavingProb);
                                    //curProbState.subtractFromReachability(leavingProb);
                                    //nxtProbState.setNextReachabilityProbToCurrent();

                                    //predMapTime += System.currentTimeMillis() - mapStart;		
                                    
                                    // Is this a new state?
                                    //if (nxtProbState.getCurReachabilityProb() >= reachabilityThreshold) {
                                    // If so, add to the explore list
                                    if(statesK.add(nxtProbState)) {
                                        exploredK.add(nxtProbState);
                                    }         
                                        
                                                        
                                }
                            }
                            curProbState.setCurReachabilityProb(0.0);
                            curProbState.setStateTerminal(false);	
                        }	
                    }		

                    
                    
                    //if(numEnabledTrans < numFiredTrans)  throw new PrismException("Fired more transitions than enabled!!!!!!!");
                    
                    
                    // Print some progress info occasionally
                    progress.updateIfReady(InfCTMCModelGenerator.getStateSpaceSize() + 1);
                }
                counter.decrementAndGet();
            }    
        } while(counter.get() > 0);  
    }

    public void run() {
        try {
            mainThread();
        }
        catch (Exception e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        } 
    }






    private ModulesFileModelGenerator modelGen;
    private List<ProbState> exploredK;
    private Set<ProbState> statesK;
    private ProgressDisplay progress;
    private AtomicInteger counter;
    private ExpressionTemporal propertyExpression;
    private Values mfConstants;
    private double reachabilityThreshold;
}
