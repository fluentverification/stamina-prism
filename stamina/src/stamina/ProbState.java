package stamina;

import java.util.HashMap;
import java.util.Map;
import parser.State;

public class ProbState extends State{
	
	private double curReachabilityProb;
	
	private boolean isStateTerminal;
	private boolean isStateAbsorbing;
	
	
	public ProbState(State s) {
		super(s);
		
		curReachabilityProb = 0.0;
		
		isStateTerminal = true;
		isStateAbsorbing = false;
		
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
	
	
	public void addToReachability(double newReach) {
		curReachabilityProb += newReach;
		if(curReachabilityProb >= 1.0) {
			curReachabilityProb = 1.0;
		}
	}

	public void subtractFromReachability(double minusReach) {
		curReachabilityProb -= minusReach;
		if(curReachabilityProb <= 0.0) {
			curReachabilityProb = 0.0;
		}
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
