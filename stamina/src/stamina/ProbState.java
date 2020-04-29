package stamina;

import java.util.HashMap;

import parser.State;

public class ProbState extends State{
	
	private double curReachabilityProb;
	private double nextReachabilityProb;
	
	private boolean isStateTerminal;
	private boolean isStateAbsorbing;
	
	/**
	 * This maps stores transition rate for each outgoing transition.
	 */
	private HashMap<ProbState, Double> predecessorPropMap; 
	
	
	public ProbState(State s) {
		super(s);
		
		curReachabilityProb = 0.0;
		nextReachabilityProb = 0.0;
		
		isStateTerminal = true;
		isStateAbsorbing = false;
		
		predecessorPropMap = new HashMap<ProbState, Double>();
	}
	
	
	public boolean isStateTerminal(){
		return isStateTerminal;
	}
	
	public void setStateTerminal(boolean flag) {
		isStateTerminal = flag;
	}
	
	public boolean isStateAbsorbing(){
		return isStateAbsorbing;
	}
	
	public void setStateAbsorbing(boolean flag) {
		isStateAbsorbing = flag;
	}
	
	
	/* Probabilistic search */
	public double getCurReachabilityProb() {
		
		return curReachabilityProb;
		
	}
	
	public void setCurReachabilityProb(double reachProb) {
		curReachabilityProb = reachProb;
	}
	
	public double getNextReachabilityProb() {
		
		return nextReachabilityProb;
		
	}
	
	public void setNextReachabilityProbToCurrent() {
		curReachabilityProb = nextReachabilityProb;
	}
	
	public void computeNextReachabilityProb() {
		
		nextReachabilityProb = 0.0;
		
		for(ProbState pre: predecessorPropMap.keySet()) {
			nextReachabilityProb += pre.getCurReachabilityProb() * predecessorPropMap.get(pre);
		}
		
		if (nextReachabilityProb > 1.0) {
			throw new RuntimeException("Path Probability greater than 1.0");
		}
	}
	
	public void updatePredecessorProbMap(ProbState state, double tranProb) {
		predecessorPropMap.put(state, tranProb);
	}
	
	
	/**
	 * Get string representation, e.g. "(0,true,5)". 
	 */
	@Override
	public String toString()
	{
		int i, n;
		String s = "";
		n = varValues.length;
		for (i = 0; i < n; i++) {
			s += varValues[i];
			s += ",";
		}
		
		s += curReachabilityProb;
		return s;
	}
	

}
