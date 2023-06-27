# The Infinite CTMC Model Generator

The Infinite CTMC (Continuous Time Markov Chain) Model Generator is implemented in class `StaminaModelGenerator`, and generates an infinite Continuous Time Markov Chain which is then used in the PRISM model checker. It implements the `ModelChecker` abstract class (Interface in Java) from the PRISM API. It has the following public methods:

## Built in methods

### Constructor
This takes a `ModulesFile` as a parameter and (optionally) a parent of type `PrismComponent`. From this, it sets the instances parent to `parent` and its modules file to `modulesFile` (a file of additionall PRISM modules used for this simulation). PTAs and `system` are not supported yet.

### `getAbsorbingState()`: return type `State`
This method returns the stored absorbing state for the instance of InftCTMCModelGenerator.

### `setReachabilityThreshhold(double)`
This method sets the reachability threshold, also known as the state space termination value.

### `setPropertyExpression(parser.ast.ExpressionTemporal)`
Sets the property expression (a temporal expression from the `parser` package).

### `clearPerimeterStatesVector()`
Clears the vector of perimeter states stored in the class.

### `getPerimeterStatesVector()`: return type `Vector<String>`
Returns a pointer to the vector of perimeter states. Perimiter states are all of the states on the perimeter of STAMINA's state space truncation. They are set as absorbing by the STAMINA model.

### `getGlobalStateSet()`: return type `HashMap<parser.State, parser.ProbState>`
Returns a pointer to the hashmap of global state sets.

### `finalModelHasAbsorbing()`: return type `boolean`
This method checks all of the instance's `globalStateSet`, set of all states to see if any of them are terminal, i.e., if the model has an "absorbing" state. If any of them are indeed terminal, then the method returns `true`. Otherwise, the method returns `false`, meaning that the global state set has no terminal states, and that the model has no absorbing states.

### `initialise()`
This method resets the transition list, rebuilds the variables, labels, and label names from the modules file so that the instance of the class is ready for model exploration.

## Methods overriden from `ModelInfo` Interface

### `getModelType()`: return type `ModelType`
Returns the model type of this instance.

### `setSomeUndefinedConstants(Values, boolean)`
Allows you to set undefined constants (is a wrapper function). `boolean exact` is an optional parameter. This also re-initializes the instance of this class.

### `getConstantValues()`: return type `Values`
This method returns the module file's constant values.

### `containsUnboundedVariables()`
Wrapper for `ModulesFile.containsUnboundedVariables()`. Indicates whether or not there are unbounded variables in the modules file.

### `getNumVars()`
Wrapper for `ModulesFile.getNumVars()`. Returns the number of variables in the modules file.

### `getVarNames()`
Wrapper for `ModulesFile.getVarNames()` Returns a list of type `String` of the names of each of the variables in the module files.

### `getVarTypes()`
Wrapper for `ModulesFile.getVarTypes()`. Returns a list of type `Type` with the types of each of the variables in the modules file.

### `getNumLabels()`
Returns the size of the list of PRISM labels associated with the model file.

### `getLabelNames()`
Returns a list of type `String` with the names of all of the labels used in the PRISM model file.

### `getLabelName(int)`
Returns the name of a label at a certain index passed in via parameter.

### `getLabelIndex(String)`
When passed in the string of a label name, returns the index of that label within the PRISM model.

### `getNumRewardStructs()`
Gets the number of reward structures from the PRISM model.

### `getRewardStructNames()`
Returns a list of type `String` with the names of all of the rewards structures associated with this PRISM model.

### `getRewardStructIndex(String)`
Given a name of reward structure from within the PRISM model, finds the index of that rewards structure and returns it.

### `getRewardStruct(int)`
Given an index, returns the rewards struct associated with that index.

## Methods overriden from `ModelGenerator` Interface

### `hasSingleInitialState()`
Returns whether or not the model has a single initial state;

### `getInitialState()`
Performs a reachability analysis and then gets the Modules file's default initial state. Returns this value.

### `getInitialStateForTransitionFile()`
Returns the Modules file's default initial state *without* first performing a reachability analysis.

### `getInitialStatesForTransitionFile()`
Does the same thing as `getInitialStateForTransitionFile()`, but returns a list of all initial states.

### `getInitialStates()`
Returns all initial states.

### `exploreState(State)`
Builds transition list and explores state. If the state passed in is exactly equal to the stored absorbing state, clears the transition list as this state cannot be explored since it is absorbing and we must be in CTMC.

### `getExploreState()`
Returns the Model Generator's explore state. This is the state which we are exploring.

### `getNumChoices()`
Returns the number of choices for the current explore state. If there is no transition list built, that means we are in an absorbing state and there is no nondeterminism, i.e., there is exactly one nondeterministic choice. This means that in this case, this method will always return `1`.

### `getNumTransitions()`
Not yet implemented

### `getNumTransitions(int)`
Gets the number of transitions for the state at the index passed in.

### `getTransitionAction(int, int)`
Gets the transition action (of type `String`) for the current state. If the state is absorbing, returns `"[Absorbing_State]"`. The second parameter (offset) is optional.

### `getChoiceAction(int)`
Not implemented. Currently just returns `null`.

### `getTransitionProbability(int, int)`
Given an index and an offset, finds the transition probability of that particular transition. If we are in an absorbing state, returns `1.0`, as an absorbing state always defaults back on itself. Currently there is a version of this function which only takes an index, but it is not implemented yet and will throw an exception.

### `computeTransitionTarget(int, int)`
Computes the target state to go to next after current state. If we are in an absorbing state, then we just return back to the absorbing state. There is a version which does not take the second parameter (offset), but it is not yet implemented.

### `isLabelTrue(int)`
For current state, checks if label at label index is true.

### `getStateReward(int, State)`
Returns the reward associated with being at the current state. Throws exception if reqard evaluates to `NaN`.

### `getStateActionReward(int, State, Object)`
Returns the reward associated with being at a state and taking an action.

### `calculateStateRewards(State, double[])`
Calculates the rewards associated with going to a specific state.

### `createVarList()`
Creates the variable list associated with the CTMC.

### `getRandomInitialState(RandomNumberGenerator, State)`
Randomly chooses an initial state, given a random number generator.

### `rewardStructHasTransitionRewards(int)`
Given an index, returns whether or not the `State` at that index has transition rewards associated with it.

### `replaceLabel(Expression)`
Given an expression, replaces the label of it. Handles instances of `ExpressionUnaryOp` which has only one operand, `ExpressionBinaryOp`, which has two operands, and `ExpressionTemporal`, which also has two operands. Returns the modified expression.

### `doReachabilityAnalysis()`
This method is the heart and soul of this class. It performs a reachability analysis, based on the initial state of the CTMC, to all other states. This method *does not support* anything other than CTMCs. This method performs a breadth first traversal of the state tree, keeping track of the transition probabilities to each state, and setting each probability as it reaches each state. When enough of the probability mass has been found, the state space is truncated.

The reachability analysis is where the reachability threshold (&kappa;, passed into STAMINA using`-kappa`) and the kappa reduction factor come into play. If the reachability threshold is lower, or the reduction factor is higher, then more states are explored. For full details, please read https://digitalcommons.usu.edu/cgi/viewcontent.cgi?article=8740&context=etd.

<!-- TODO: Once Riley's paper is approved, add link to that, rather than the STAMINA 1.0 stuff. -->
