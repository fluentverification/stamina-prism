package stamina;

import java.util.HashMap;
import java.util.Map;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
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
	
	
	public synchronized boolean isStateTerminal(){
		return isStateTerminal;
	}
	
	public synchronized void setStateTerminal(boolean flag) {
		isStateTerminal = flag;
	}
	
	public synchronized boolean isStateAbsorbing(){
		return isStateAbsorbing;
	}
	
	public synchronized void setStateAbsorbing(boolean flag) {
		isStateAbsorbing = flag;
	}
	
	
	/* Probabilistic search */
	public synchronized double getCurReachabilityProb() {
		
		return curReachabilityProb;
		
	}
	
	public synchronized void setCurReachabilityProb(double reachProb) {
		curReachabilityProb = reachProb;
	}
	
	
	public synchronized void addToReachability(double newReach) {
		curReachabilityProb += newReach;
		if(curReachabilityProb >= 1.0) {
			curReachabilityProb = 1.0;
		}
	}
	
	/**
	 * Get string representation, e.g. "(0,true,5)". 
	 */
	@Override
	public synchronized String toString()
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
