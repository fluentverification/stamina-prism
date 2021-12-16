package stamina;

import java.util.HashMap;
import java.util.Map;
import parser.State;

public class ProbState extends State{
	
	private double curReachabilityProb;
	
	private boolean isStateTerminal;
	private boolean isStateAbsorbing;
	private double shortestTime;
	
	/**
	 * Constructor. Sets the current reachability probability to 0,
	 * and the state to terminal and not absorbing.
	 * @param s The state we are building from.
	 */
	public ProbState(State s) {
		super(s);
		
		curReachabilityProb = 0.0;
		
		isStateTerminal = true;
		isStateAbsorbing = false;
		shortestTime = Double.POSITIVE_INFINITY;
		
	}
	
	public double getShortestTime(){
		return shortestTime;
	}

	public void updateShortestTime(double newTime){
		if (newTime < shortestTime) {
			shortestTime = newTime;
		}
	}

	public void setShortestTime(double newTime){
		shortestTime = newTime;
	}

	/**
	 * Gets whether or not the state is terminal.
	 * @return Whether or not the state is terminal
	 */
	public boolean isStateTerminal(){
		return isStateTerminal;
	}
	
	/**
	 * Sets whether or not the state is terminal.
	 * @param flag Whether or not the state is terminal.
	 */
	public void setStateTerminal(boolean flag) {
		isStateTerminal = flag;
	}
	/**
	 * Gets whether or not the state is absorbing
	 * @return Whether the state is absorbing or not.
	 */
	public boolean isStateAbsorbing(){
		return isStateAbsorbing;
	}
	/**
	 * Sets whether or not this state is absorbing.
	 * @param flag Whether or not this state is absorbing.
	 */
	public void setStateAbsorbing(boolean flag) {
		isStateAbsorbing = flag;
	}

	/* Probabilistic search */

	/**
	 * Gets the current reachability probability of this state.
	 * @return The current reachability probability.
	 */
	public double getCurReachabilityProb() {	
		return curReachabilityProb;
	}
	/**
	 * Sets the current reachability probability.
	 * @param reachProb The new reachability probability.
	 */
	public void setCurReachabilityProb(double reachProb) {
		curReachabilityProb = reachProb;
	}
	/**
	 * Adds to reachability probability, maxing at 1.0
	 * @param newReach Amount to add to reachability probability.
	 */
	public void addToReachability(double newReach) {
		curReachabilityProb += newReach;
		if(curReachabilityProb >= 1.0) {
			curReachabilityProb = 1.0;
		}
	}
	/**
	 * Subtracts from reachability probability, min at 0.0.
	 * @param minusReach The amount to subtract from reachability probability.
	 */
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
