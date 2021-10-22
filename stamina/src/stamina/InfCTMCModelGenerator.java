package stamina;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Vector;
import java.util.HashSet;
import explicit.IndexedSet;
import explicit.StateStorage;
import parser.State;
import parser.Values;
import parser.VarList;
import parser.ast.DeclarationBool;
import parser.ast.Expression;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionLabel;
import parser.ast.ExpressionLiteral;
import parser.ast.ExpressionTemporal;
import parser.ast.ExpressionUnaryOp;
import parser.ast.LabelList;
import parser.ast.ModulesFile;
import parser.ast.RewardStruct;
import parser.type.Type;
import parser.type.TypeBool;
import prism.ModelGenerator;
import prism.ModelType;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismLangException;
import prism.PrismNotSupportedException;
import prism.ProgressDisplay;
import simulator.ModulesFileModelGenerator;
import simulator.RandomNumberGenerator;
import simulator.TransitionList;
import simulator.Updater;


public class InfCTMCModelGenerator implements ModelGenerator
{
	// Parent PrismComponent (logs, settings etc.)
	protected PrismComponent parent;
	
	// PRISM model info
	/** The original modules file (might have unresolved constants) */
	private ModulesFile originalModulesFile;
	/** The modules file used for generating (has no unresolved constants after {@code initialise}) */
	private ModulesFile modulesFile;
	private ModelType modelType;
	private Values mfConstants;
	private VarList varList;
	private LabelList labelList;
	private List<String> labelNames;
	private Vector<String> perimeterStates = new Vector<String>();
	
	// Model exploration info
	
	// State currently being explored
	private State exploreState;
	// Updater object for model
	protected Updater updater;
	// List of currently available transitions
	protected TransitionList transitionList;
	// Has the transition list been built? 
	protected boolean transitionListBuilt;
	
	// Reachability threshold
	private double reachabilityThreshold = 1.0e-6;
	
	//private TreeMap<ProbState, Integer> globalStateSet = null;
	private HashMap<State, ProbState> globalStateSet = null;
	
	// Absorbing state
	private State absorbingState = null;
	private ModulesFileModelGenerator modelGen;
	
	//Non Absorbing state
	//private BitSet nonAbsorbingStateSet = null;
	
	// Temporal expression
	ExpressionTemporal propertyExpression = null;

	


	
	/**
	 * Build a ModulesFileModelGenerator for a particular PRISM model, represented by a ModuleFile instance.
	 * @param modulesFile The PRISM model
	 */
	public InfCTMCModelGenerator(ModulesFile modulesFile) throws PrismException
	{
		this(modulesFile, null);
	}
	
	/**
	 * Build a ModulesFileModelGenerator for a particular PRISM model, represented by a ModuleFile instance.
	 * @param modulesFile The PRISM model
	 */
	public InfCTMCModelGenerator(ModulesFile modulesFile, PrismComponent parent) throws PrismException
	{
		this.parent = parent;
		
		// No support for PTAs yet
		if (modulesFile.getModelType() == ModelType.PTA) {
			throw new PrismException("Sorry - the simulator does not currently support PTAs");
		}
		// No support for system...endsystem yet
		if (modulesFile.getSystemDefn() != null) {
			throw new PrismException("Sorry - the simulator does not currently handle the system...endsystem construct");
		}
		
		// Store basic model info
		this.modulesFile = modulesFile;
		this.originalModulesFile = modulesFile;
		modelType = modulesFile.getModelType();
		
		// If there are no constants to define, go ahead and initialise;
		// Otherwise, setSomeUndefinedConstants needs to be called when the values are available  
		mfConstants = modulesFile.getConstantValues();
		if (mfConstants != null) {
			initialise();
		}
		
		
		///////////////////////////Other init///////////////
		
		//globalStateSet = new TreeMap<ProbState, Integer>();
		globalStateSet = new HashMap<State, ProbState>();
		
		// Add Absorbing state if there is one, otherwise add state a with all variable -1
		absorbingState = new State(varList.getNumVars());
		for(int i=0; i<varList.getNumVars();++i) {
			
			if(varList.getDeclaration(i).getDeclType() instanceof DeclarationBool) {
				absorbingState.setValue(i, 0);
			}
			else {
				absorbingState.setValue(i, varList.getLow(i)-1);
				
			}
		}
		
	}
	
	public State getAbsorbingState() {
		return absorbingState;
	}
	
	/**
	 * set reachability threshold
	 * @param th (double) The new reachability threshold to set
	 */
	public void setReachabilityThreshold(double th) {
		reachabilityThreshold = th;
	}
	/**
	 * Set the property description
	 * @param expr (ExpressionTemporal) The property expression to set.
	 */
	public void setPropertyExpression(ExpressionTemporal expr) {
		propertyExpression = expr;
	}
	/**
	 * Clears the perimeter states vector
	 */
	public void clearPerimeterStatesVector() {
		perimeterStates.clear();
	}
	/**
	 * Gets a pointer to a vector containing all perimter states
	 * @return Perimeter states (in the format of a Vector<String>)
	 */
	public Vector<String> getPerimeterStatesVector() {
		return perimeterStates;
	}
	/**
	 * Gets the global state set.
	 * @return A hash map containing all states in the global state set.
	 */
	public HashMap<State, ProbState> getGlobalStateSet() {
		return globalStateSet;
	}
	/**
	 * Determines if the final (truncated) model has absorbing states
	 * @return a boolean value reflective of whether or not absorbing states exist in the final model.
	 */
	public boolean finalModelHasAbsorbing() {
		for (ProbState ps : globalStateSet.values()) {
			if(ps.isStateTerminal()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * (Re-)Initialise the class ready for model exploration
	 * (can only be done once any constants needed have been provided)
	 * @throws PrismLangException
	 */
	private void initialise() throws PrismLangException
	{
		// Evaluate constants on (a copy) of the modules file, insert constant values and optimize arithmetic expressions
		modulesFile = (ModulesFile) modulesFile.deepCopy().replaceConstants(mfConstants).simplify();

		// Get info
		varList = modulesFile.createVarList();
		labelList = modulesFile.getLabelList();
		labelNames = labelList.getLabelNames();
		
		// Create data structures for exploring model
		updater = new Updater(modulesFile, varList, parent);
		transitionList = new TransitionList();
		transitionListBuilt = false;
	}
	
	// Methods for ModelInfo interface
	/**
	 * Gets the model type
	 * @return the model type
	 */
	@Override
	public ModelType getModelType()
	{
		return modelType;
	}
	/**
	 * Sets constants which are undefined by the PRISM file passed in. This corresponds to the -const
	 * parameter passed into the stamina binary. This function is a wrapper for setSomeUndefinedConstants(Values, boolean)
	 * @param someValues The values to be passed in.
	 * @throws PrismException
	 */
	@Override
	public void setSomeUndefinedConstants(Values someValues) throws PrismException
	{
		setSomeUndefinedConstants(someValues, false);
	}
	/**
	 * Sets constants undefined by the PRISM file passed in. This corresponds to the -const
	 * parameter and allows the constants to be defined as exact or not.
	 * @param someValues The values to be passed in.
	 * @param exact are these constants exact?
	 * @throws PrismException
	 */
	@Override
	public void setSomeUndefinedConstants(Values someValues, boolean exact) throws PrismException
	{
		// We start again with a copy of the original modules file
		// and set the constants in the copy.
		// As {@code initialise()} can replace references to constants
		// with the concrete values in modulesFile, this ensures that we
		// start again at a place where references to constants have not
		// yet been replaced.
		modulesFile = (ModulesFile) originalModulesFile.deepCopy();
		modulesFile.setSomeUndefinedConstants(someValues, exact);
		mfConstants = modulesFile.getConstantValues();
		initialise();
	}
	/**
	 * Gets the constant values associated with this PRISM model
	 * @return The constant values
	 */
	@Override
	public Values getConstantValues()
	{
		return mfConstants;
	}
	/**
	 * Checks to see if the modules file contains unbounded variables.
	 * @return Whether or not there are unbounded variables
	 */
	@Override
	public boolean containsUnboundedVariables()
	{
		return modulesFile.containsUnboundedVariables();
	}
	/**
	 * Gets the number of variables in the PRISM modules file.
	 * @return The number of variables
	 */
	@Override
	public int getNumVars()
	{
		return modulesFile.getNumVars();
	}
	/**
	 * Gets the variable names from the modules file.
	 * @return A List of type String with the variable names in it.
	 */
	@Override
	public List<String> getVarNames()
	{
		return modulesFile.getVarNames();
	}
	/**
	 * Gets the types of each variable corresponding to the modules file.
	 * @return A List of type Type with the variable types in it.
	 */
	@Override
	public List<Type> getVarTypes()
	{
		return modulesFile.getVarTypes();
	}
	/**
	 * Gets the number of labels.
	 * @return The number of labels as int.
	 */
	@Override
	public int getNumLabels()
	{
		return labelList.size();	
	}
	/**
	 * Gets the label names.
	 * @return A List of type String with the label names.
	 */
	@Override
	public List<String> getLabelNames()
	{
		return labelNames;
	}
	/**
	 * Gets a label name at a particular index.
	 * @param i The index of the label name to get.
	 * @return The label at index i.
	 * @throws PrismException
	 */
	@Override
	public String getLabelName(int i) throws PrismException
	{
		return labelList.getLabelName(i);
	}
	/**
	 * Given a string with a desired label name, gets the index corresponding to that label.
	 * @param label The name of the label.
	 * @return The index of the label.
	 */
	@Override
	public int getLabelIndex(String label)
	{
		return labelList.getLabelIndex(label);
	}
	/**
	 * Gets the number of rewards structs.
	 * @return The number of rewards structs in the PRISM model.
	 */
	@Override
	public int getNumRewardStructs()
	{
		return modulesFile.getNumRewardStructs();
	}
	/**
	 * Gets all rewards structure names.
	 * @return A List of type String with the reward structure names.
	 */
	@Override
	public List<String> getRewardStructNames()
	{
		return modulesFile.getRewardStructNames();
	}
	/**
	 * Given a name of a rewards structure, gets the index of that rewards structure.
	 * @param name The name of the rewards structure.
	 * @return The index corresponding to the name.
	 */
	@Override
	public int getRewardStructIndex(String name)
	{
		return modulesFile.getRewardStructIndex(name);
	}
	/**
	 * Gets a particular reward struct at an index.
	 * @param i The index of the reward struct desired.
	 * @return The RewardStruct at index i.
	 */
	@Override
	public RewardStruct getRewardStruct(int i)
	{
		return modulesFile.getRewardStruct(i);
	}

	// Methods for ModelGenerator interface
	/**
	 * Checks to see if there is an initial state.
	 * @return boolean indicating whether or not there is a single initial state.
	 * @throws PrismException
	 */
	@Override
	public boolean hasSingleInitialState() throws PrismException
	{
		return modulesFile.getInitialStates() == null;
	}
	/**
	 * Gets the default initial state after model truncation.
	 * @return The default initial state.
	 * @throws PrismException
	 */
	@Override
	public State getInitialState() throws PrismException
	{
		
		doReachabilityAnalysis();
		
		return modulesFile.getDefaultInitialState();
		
	}
	/**
	 * Gets initial state for transition file.
	 * @return Initial state for transition matrix file.
	 * @throws PrismException
	 */
	public State getInitialStateForTransitionFile() throws PrismException
	{
		return modulesFile.getDefaultInitialState();
	}
	/**
	 * Gets all initial states after model truncation.
	 * @return A List of type State with all of the initial states.
	 * @throws PrismException
	 */
	@Override
	public List<State> getInitialStates() throws PrismException
	{
		// Default to the case of a single initial state
		return Collections.singletonList(getInitialState());
	}
	/**
	 * Gets all initial states for transition matrix file.
	 * @return A List of type State that contains all of the initial states for a transition file.
	 * @throws PrismException
	 */
	public List<State> getInitialStatesForTransitionFile() throws PrismException
	{
		return Collections.singletonList(getInitialStateForTransitionFile());
	}
	/**
	 * Explores a particular state. If it's an absorbing state, clears its transition list,
	 * otherwise, it calculates a transition list for it.
	 * @param exploreState The state to explore.
	 * @throws PrismException
	 */
	@Override
	public void exploreState(State exploreState) throws PrismException
	{
		this.exploreState = exploreState;
		transitionListBuilt = false;
		
		if(exploreState==absorbingState) {
			// Clear lists/bitsets
			transitionList.clear();
		}
		else {
			updater.calculateTransitions(exploreState, transitionList);
			transitionListBuilt = true;
		}
	}
	/**
	 * Gets the state the model generator is about to explore.
	 * @return The state we are about to explore.
	 */
	@Override
	public State getExploreState()
	{
		return exploreState;
	}
	/**
	 * Gets the number of choices available for the state we are about to explore.
	 * @return The number of choices available.
	 * @throws PrismException
	 */
	@Override
	public int getNumChoices() throws PrismException
	{
		if(transitionListBuilt) {
			return transitionList.getNumChoices();
		}
		else {
			// This is a CTMC so always exactly one nondeterministic choice (i.e. no nondeterminism)
			return 1;
		}
		
	}
	/**
	 * Gets the number of transitions for the current state.
	 * @return Not implemented.
	 * @throws PrismException
	 */
	@Override
	public int getNumTransitions() throws PrismException
	{
		throw new PrismException("Not Implemented");
	}
	/**
	 * Gets the number of transitions for the choice at a specific index in the transitions list.
	 * @param index The index of the choice within the transition list to look at.
	 * @return The number of transitions associated with that choice.
	 * @throws PrismException
	 */
	@Override
	public int getNumTransitions(int index) throws PrismException
	{
		if(transitionListBuilt) {
			return transitionList.getChoice(index).size();
		}
		else {
			return 1;
		}
		
	}
	/**
	 * Gets the action of the transition list at an index. If no transition list exists then it
	 * means that state is absorbing, so returns [Absorbing_State]
	 * @param index The index of the transition list to get.
	 * @return The transition action in the form of a String.
	 * @throws PrismException
	 */
	@Override
	public String getTransitionAction(int index) throws PrismException
	{
		if(transitionListBuilt) {
			return transitionList.getTransitionModuleOrAction(index);
		}
		else {
			return "[Absorbing_State]";
		}
	}
	/**
	 * Gets the action of the transition list at an index and offset. If no transition list exists
	 * then state is absorbing, so returns [Absorbing_State].
	 * @param index The index to look at.
	 * @param offset The offset to look at.
	 * @return The action at that location of the transition list.
	 * @throws PrismException
	 */
	@Override
	public String getTransitionAction(int index, int offset) throws PrismException
	{
			
		if(transitionListBuilt) {
			return transitionList.getTransitionModuleOrAction(transitionList.getTotalIndexOfTransition(index, offset));
		}
		else {
			return "[Absorbing_State]";
		}

	}
	/**
	 * Gets the choice action at an index. TODO: Why does this return null?
	 * @param index Index to look at.
	 * @return null
	 * @throws PrismException
	 */
	@Override
	public String getChoiceAction(int index) throws PrismException
	{
		return null;
	}

	/**
	 * Gets the probability that a particular choice and offset are taken.
	 * If the transistion list is not built, then the state is absorbing and
	 * the probability is 1.0.
	 * @param index The index of the choice.
	 * @param offset The offset of the choice to get the probability from.
	 * @return The probability of transition.
	 * @throws PrismException
	 */
	@Override
	public double getTransitionProbability(int index, int offset) throws PrismException
	{
		if(transitionListBuilt) {
			return transitionList.getChoice(index).getProbability(offset);
		}
		else {
			return 1.0;
		}
	}
	/**
	 * Gets the transition probability using just an index.
	 * @param index The index to get the transition probability.
	 * @return The probability. TODO: not implemented.
	 * @throws PrismException 
	 */
	//@Override
	public double getTransitionProbability(int index) throws PrismException
	{
		throw new PrismException("Not Implemented");
	}
	/**
	 * Computes the target state of a transition from the current state based on 
	 * the transition list. If state is absorbing, will always return a pointer to the
	 * current state.
	 * @param index The index of the choice.
	 * @param offset The offset from that index to compute the target state for.
	 * @return The state we will be travelling to.
	 * @throws PrismException
	 */
	@Override
	public State computeTransitionTarget(int index, int offset) throws PrismException
	{	
		if(transitionListBuilt) {
			State st = transitionList.getChoice(index).computeTarget(offset, exploreState);
			
			ProbState prbSt = globalStateSet.get(st);
			
			if(globalStateSet.get(exploreState).isStateAbsorbing()) return exploreState;
			else {
				if(prbSt == null) return absorbingState;
				else return st;
			}
		}
		else {
			
			return absorbingState;
		}
		
	}
	/**
	 * Computes the transition target just based on a single index.
	 * @param index The index of the choice.
	 * @return TODO: not implemented
	 * @throws PrismException
	 */
	//@Override
	public State computeTransitionTarget(int index) throws PrismException
	{
		throw new PrismException("Not Implemented");
	}
	/**
	 * Evaluates a label at an index and indicates whether it is currently true for the exploreState
	 * @param i The index of the label.
	 * @return Whether or not that label is currently true.
	 * @throws PrismException
	 */
	@Override
	public boolean isLabelTrue(int i) throws PrismException
	{
		Expression expr = labelList.getLabel(i);
		return expr.evaluateBoolean(exploreState);
	}
	/**
	 * Gets the total state reward at a specific index of rewards structure and a specific state.
	 * @param r The index of the reward structure.
	 * @param state The state to evaluate the rewards structure at.
	 * @return The total state reward at the given state.
	 * @throws PrismException
	 */
	@Override
	public double getStateReward(int r, State state) throws PrismException
	{
		RewardStruct rewStr = modulesFile.getRewardStruct(r);
		int n = rewStr.getNumItems();
		double d = 0;
		for (int i = 0; i < n; i++) {
			if (!rewStr.getRewardStructItem(i).isTransitionReward()) {
				Expression guard = rewStr.getStates(i);
				if (guard.evaluateBoolean(modulesFile.getConstantValues(), state)) {
					double rew = rewStr.getReward(i).evaluateDouble(modulesFile.getConstantValues(), state);
					if (Double.isNaN(rew))
						throw new PrismLangException("Reward structure evaluates to NaN at state " + state, rewStr.getReward(i));
					d += rew;
				}
			}
		}
		return d;
	}
	/**
	 * Gets the rewards from a rewards structure witha specific index associated with being at a certain state and
	 * taking a certain action.
	 * @param r The index of the rewards structure.
	 * @param state The state we are at.
	 * @param action The action we wish to take.
	 * @return The total reward associated with that state-action combination.
	 * @throws PrismException
	 */
	@Override
	public double getStateActionReward(int r, State state, Object action) throws PrismException
	{
		RewardStruct rewStr = modulesFile.getRewardStruct(r);
		int n = rewStr.getNumItems();
		double d = 0;
		for (int i = 0; i < n; i++) {
			if (rewStr.getRewardStructItem(i).isTransitionReward()) {
				Expression guard = rewStr.getStates(i);
				String cmdAction = rewStr.getSynch(i);
				if (action == null ? (cmdAction.isEmpty()) : action.equals(cmdAction)) {
					if (guard.evaluateBoolean(modulesFile.getConstantValues(), state)) {
						double rew = rewStr.getReward(i).evaluateDouble(modulesFile.getConstantValues(), state);
						if (Double.isNaN(rew))
							throw new PrismLangException("Reward structure evaluates to NaN at state " + state, rewStr.getReward(i));
						d += rew;
					}
				}
			}
		}
		return d;
	}
	/**
	 * Calculates the state rewards associated with a specific state and store array.
	 * @param state The state the rewards are associated with
	 * @param store The array of stored probabilities. TODO: not 100% sure this is correct
	 * @throws PrismLangException
	 */
	//@Override
	public void calculateStateRewards(State state, double[] store) throws PrismLangException
	{
		updater.calculateStateRewards(state, store);
	}
	/**
	 * Provides the list of variables associated with the PRISM model.
	 * TODO: why is this named createVarList() and not getVarList()?
	 * @return The list of variables.
	 */
	@Override
	public VarList createVarList()
	{
		return varList;
	}
	
	// Miscellaneous (unused?) methods
	/**
	 * Gets a random initial state given a random number generator.
	 * @param rng The RandomNumberGenerator to pass into the method.
	 * @param initialState A pointer to the current initial state.
	 * @throws PrismException Multiple initial states not yet supported.
	 */
	//@Override
	public void getRandomInitialState(RandomNumberGenerator rng, State initialState) throws PrismException
	{
		if (modulesFile.getInitialStates() == null) {
			initialState.copy(modulesFile.getDefaultInitialState());
		} else {
			throw new PrismException("Random choice of multiple initial states not yet supported");
		}
	}

	// Local utility methods
	/**
	 * Determines if a rewards structure at a specific index has transition rewards.
	 * @param i The index of the rewards structure.
	 * @return Whether or not that structure has transition rewards associated with it.
	 */
	//@Override
	public boolean rewardStructHasTransitionRewards(int i)
	{
		return modulesFile.rewardStructHasTransitionRewards(i);
	}
	
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * Replaces the label on an expression.
	 * @param expr Expression to replace the label on.
	 * @return The new expression, with replaced label.
	 * @throws PrismException
	 */
	Expression replaceLabel(Expression expr) throws PrismException {
		
		if(expr instanceof ExpressionUnaryOp) {
			Expression op = ((ExpressionUnaryOp) expr).getOperand();
			
			if(op instanceof ExpressionLabel) {
				
				if(((ExpressionLabel) op).getName().equals("absorbingState")) {
					((ExpressionUnaryOp) expr).setOperand(new ExpressionLiteral(TypeBool.getInstance(), false, "false"));
				}
			}
			else {
				replaceLabel(op);
			}
			
		}
		else if(expr instanceof ExpressionBinaryOp) {
			Expression op1 = ((ExpressionBinaryOp) expr).getOperand1();
			if(op1 instanceof ExpressionLabel) {
				
				if(((ExpressionLabel) op1).getName().equals("absorbingState")) {
					((ExpressionBinaryOp) expr).setOperand1(new ExpressionLiteral(TypeBool.getInstance(), false, "false"));
				}
				
			}
			else {
				 replaceLabel(op1);
			}
			
			Expression op2 = ((ExpressionBinaryOp) expr).getOperand2();
			
			if(op2 instanceof ExpressionLabel) {
				
				if(((ExpressionLabel) op2).getName().equals("absorbingState")) {
					((ExpressionBinaryOp) expr).setOperand2(new ExpressionLiteral(TypeBool.getInstance(), false, "false"));
				}
				
			}
			else {
				 replaceLabel(op2);
			}
			
		}
		else if(expr instanceof ExpressionTemporal) {
			Expression op1 = ((ExpressionTemporal) expr).getOperand1();
			
			if(op1 instanceof ExpressionLabel) {
				
				if(((ExpressionLabel) op1).getName().equals("absorbingState")) {
					((ExpressionTemporal) expr).setOperand1(new ExpressionLiteral(TypeBool.getInstance(), false, "false"));
				}
				
			}
			else {
				 replaceLabel(op1);
			}
			
			Expression op2 = ((ExpressionTemporal) expr).getOperand2();
			
			if(op2 instanceof ExpressionLabel) {
				
				if(((ExpressionLabel) op2).getName().equals("absorbingState")) {
					((ExpressionTemporal) expr).setOperand2(new ExpressionLiteral(TypeBool.getInstance(), false, "false"));
				}
				
			}
			else {
				 replaceLabel(op2);
			}
			
		}
		return expr;
	}
	
	/**
	 * Does reachability analysis and truncates state space based on values of Kappa (&kappa;), and
	 * its reduction factor. This method performs a breadth first search to find most of the probability
	 * mass of the state space.
	 * @throws PrismException Does not support anything other than CTMCs.
	 */
	public void doReachabilityAnalysis() throws PrismException {
		
	 	
		// Model gen from file
	 	ModulesFileModelGenerator modelGen = new ModulesFileModelGenerator(modulesFile, parent);
	
		//VarList varList = modelGen.createVarList();
		if (modelGen.containsUnboundedVariables())
			parent.getLog().printWarning("Infinite State system: Reachability analysis based on reachabilityThreshold=" + reachabilityThreshold);
	
		ProgressDisplay progress = new ProgressDisplay(parent.getLog());
		progress.start();
		
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Initia;ize set
		if (modelType != ModelType.CTMC) {
			throw new PrismNotSupportedException("Probabilistic model construction not supported for " + modelType + "s");
		}
		
		HashSet<ProbState> statesK = new HashSet<ProbState>();
		LinkedList<ProbState> exploredK = new LinkedList<ProbState>();
		
		
		//int globalIterationCount = 1;
		
		//Get initial state and set reach_prob
		State initState = modelGen.getInitialState();
		ProbState probInitState = null;
		if(globalStateSet.containsKey(initState)) {
			probInitState = globalStateSet.get(initState);
		}
		else {
			probInitState = new ProbState(initState);
			probInitState.setCurReachabilityProb(1.0);
			probInitState.updateShortestTime(0.0);
			// Add initial state(s) to 'explore', 'states' and to the model
			globalStateSet.put(initState, probInitState);
			
		}
		// Add state to exploration queue
		
		exploredK.add(probInitState);
		statesK.add(probInitState);
		
		// Start the exploration
		
		int prevStateCount = globalStateSet.size();
		double perimReachability = 1;
		// State Search 
		while(perimReachability >= Options.getProbErrorWindow() / Options.getMispredictionFactor()) {
			/*double startTime = System.currentTimeMillis();
			double predMapTime = 0;// Explore...
			double prismTime = 0.0;
			double propCheckTime = 0.0;*/
			while (!exploredK.isEmpty()) {
				ProbState curProbState = exploredK.removeFirst();
				//statesK.remove(curProbState);
				
				//System.out.println("\nExplored exactly one time\n");
				// Explore all choices/transitions from this state
				//double trackPrismTime = System.currentTimeMillis();
				modelGen.exploreState(curProbState);
				//prismTime += System.currentTimeMillis() - trackPrismTime;
				
				/////////////////////////////////////////////
				//double propCheckTimeHelper = System.currentTimeMillis();
				if (propertyExpression != null) {
					
					ExpressionTemporal tempProp = (ExpressionTemporal) propertyExpression;
					
					boolean b1 = (boolean) tempProp.getOperand1().evaluate(mfConstants, curProbState);
					boolean b2 = (boolean) tempProp.getOperand2().evaluate(mfConstants, curProbState);
					
					if(!(b1 && (!b2))) {
						curProbState.setStateAbsorbing(true);
						perimeterStates.addElement(curProbState.toString());
						curProbState.setStateTerminal(false);
						continue;
					}
				}
				//propCheckTime += System.currentTimeMillis() - propCheckTimeHelper;
				
				////////////////////////////////////////////
				
				
				double curStateReachability = curProbState.getCurReachabilityProb();
				double curTime = curProbState.getShortestTime();
				if (curTime > 12) {
					System.out.println("It actually ran");
					curProbState.setCurReachabilityProb(0.0);
					continue;
				}
				/*System.out.println("\nState");
				System.out.println(curProbState);
				System.out.println(curStateReachability);*/
				if( (!curProbState.isStateTerminal() || curStateReachability >= reachabilityThreshold)) {
					//prismTime += System.currentTimeMillis() - trackPrismTime

					if(curStateReachability == 0) {
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
						double exitRateInverse = 1.0/exitRateSum;


						for (int i = 0; i < nc; i++) {
							// Look at each transition in the choice
							int nt = modelGen.getNumTransitions(i);
							for (int j = 0; j < nt; j++) {
								State nxtSt = modelGen.computeTransitionTarget(i, j);
								boolean stateIsExisting = globalStateSet.containsKey(nxtSt);
								if(stateIsExisting) {
									ProbState nxtProbState = globalStateSet.get(nxtSt);
									nxtProbState.updateShortestTime(curTime + exitRateInverse);
									if(statesK.add(nxtProbState)) {
										exploredK.addLast(nxtProbState);
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
						double exitRateInverse = 1.0/exitRateSum; 
						for (int i = 0; i < nc; i++) {
							// Look at each transition in the choice
							//System.out.println("\nA choice\n");
							int nt = modelGen.getNumTransitions(i);
							for (int j = 0; j < nt; j++) {
								//trackPrismTime = System.currentTimeMillis();
								State nxtSt = modelGen.computeTransitionTarget(i, j);
								//prismTime += System.currentTimeMillis() - trackPrismTime;

								boolean stateIsExisting = globalStateSet.containsKey(nxtSt);
								
								
								if (stateIsExisting) {

			
									ProbState nxtProbState = globalStateSet.get(nxtSt);

									//trackPrismTime = System.currentTimeMillis();
									double tranRate = modelGen.getTransitionProbability(i, j);
									//prismTime += System.currentTimeMillis() - trackPrismTime;
									//compute next reachability probability for nextState
									double tranProb = tranRate / exitRateSum;
									//double curProb = nxtProbState.getCurReachabilityProb();

									//double mapStart = System.currentTimeMillis();
									
									double leavingProb = tranProb * curStateReachability;
									nxtProbState.addToReachability(leavingProb);
									nxtProbState.updateShortestTime(curTime + exitRateInverse);
									//curProbState.subtractFromReachability(leavingProb);
									//nxtProbState.setNextReachabilityProbToCurrent();

									//predMapTime += System.currentTimeMillis() - mapStart;		
									
									// Is this a new state?
									//if (nxtProbState.getCurReachabilityProb() >= reachabilityThreshold) {
									// If so, add to the explore list
									if(statesK.add(nxtProbState)) {
										exploredK.addLast(nxtProbState);
									}
										
									//}
									
									// Increment fired tran
								
								}
								else {
									//if(!curProbState.isStateAbsorbing()) {
									ProbState nxtProbState = new ProbState(nxtSt);
									//trackPrismTime = System.currentTimeMillis();
									double tranRate = modelGen.getTransitionProbability(i, j);
									//prismTime += System.currentTimeMillis()-trackPrismTime;
									//compute next reachability probability for nextState
									double tranProb = tranRate/exitRateSum;
									/*if(curProbState.equals(initState)) {
										System.out.println("\nFrom init to other");
										System.out.println(nxtProbState.toString());
										System.out.println(tranProb);
									}*/
									//double mapStart = System.currentTimeMillis();
									double leavingProb = tranProb*curStateReachability;
									nxtProbState.addToReachability(leavingProb);
									nxtProbState.updateShortestTime(curTime + exitRateInverse);
									//curProbState.subtractFromReachability(leavingProb);
									//nxtProbState.setNextReachabilityProbToCurrent();
									//predMapTime += System.currentTimeMillis() - mapStart;
									
									//Update the global state graph
									globalStateSet.put(nxtSt, nxtProbState);
									
									// Is this a new state?
									//if (statesK.add(nxtProbState)) {
										// If so, add to the explore list
									statesK.add(nxtProbState);
									exploredK.addLast(nxtProbState);
									//}
									
									// Increment fired tran
											
										
									//}
								}						
							}
						}
					}
					curProbState.setCurReachabilityProb(0.0);
					curProbState.setStateTerminal(false);
					curProbState.setShortestTime(Double.POSITIVE_INFINITY);	
				}
				

				
				
				//if(numEnabledTrans < numFiredTrans)  throw new PrismException("Fired more transitions than enabled!!!!!!!");
				
				
				// Print some progress info occasionally
				progress.updateIfReady(globalStateSet.size() + 1);
			}
			
			//statesK.clear();
			exploredK.clear();
			statesK.clear();
			
			perimReachability = 0;
			int count = 0;
			//double findPerimTime = System.currentTimeMillis();
			for (ProbState localSt: globalStateSet.values()) {
				
				//ProbState localSt = globalStateSet.get(st);
				
				if (localSt.isStateTerminal()) {
					perimReachability += reachabilityThreshold; 
				}
				
				if (localSt.getCurReachabilityProb() > 0) {
					exploredK.add(localSt);
					statesK.add(localSt);
				}
				
			}
		
			//findPerimTime = System.currentTimeMillis() - findPerimTime;



			/*System.out.print("\n\nPerim Reachability Estimate: ");
			System.out.println(perimReachability);	
			System.out.print("Time in map: ");
			System.out.println(predMapTime/1000.0);
			System.out.print("Time finding perim: ");
			System.out.println(findPerimTime/1000.0);
			System.out.print("Time in prism: ");
			System.out.println(prismTime/1000.0);
			System.out.print("Prop Check time: ");
			System.out.println(propCheckTime/1000.0);*/
			//Prepare for next itr or terminate
			/*int curStateCount = globalStateSet.size();
			double stateChange = ((double)(curStateCount-prevStateCount)/(double)prevStateCount);
			System.out.print("State Change: ");
			System.out.println(stateChange);
			System.out.print("Time taken: ");
			double curTime = System.currentTimeMillis();
			System.out.println((curTime-startTime)/1000.0);
			System.out.println();
			prevStateCount = curStateCount;*/		
				
			reachabilityThreshold /= Options.getKappaReductionFactor();
			// Pick next state to explore                           
			/*if(Options.getRankTransitions()) {
				exploredK.sort(new Comparator<ProbState>(){
					@Override
					public int compare(ProbState l, ProbState r) {
						if (r.getCurReachabilityProb() > l.getCurReachabilityProb()) {
							return 1;
						} else if (r.getCurReachabilityProb() < l.getCurReachabilityProb()) {
							return -1;
						} else {
							return 0;
						}
					}
				});
			}*/
			
			//++globalIterationCount;
		}
		
		Options.setReachabilityThreshold(reachabilityThreshold);
			
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
		// Finish progress display
		//progress.update(globalIterationCount);
		progress.update(globalStateSet.size()+1);
		progress.end(" states");
		
		
		/////////////////reset proprty expression
		propertyExpression = null;
		
	}
}
