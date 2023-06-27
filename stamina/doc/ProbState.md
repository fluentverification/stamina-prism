# The Probability State class (extension of `parser.State`)
This class is a representation of each state in the CTMC we are building. It has very few methods and no public data members. It has the following public methods:

### Constructor
The constructor takes one input, a `State`, which it passes into the `parser.State` constructor. The constructor also sets the reachability probability of this state, as well as whether or not the state is terminal or absorbint to `false`.

### `isStateTerminal()`
Returns a `boolean` indicating whether or not the state is terminal. Default is set to `false`.

### `setStateTerminal(boolean)`
Sets whether or not this state is going to be terminal.

### `isStateAbsorbing()`
Returns whether or not this is an absorbing state.

### `setStateAbsorbing(boolean)`
Sets whether or not the state is an absorbing state.

### `getCurReachabilityProb()`
Returns the current reachability probability.

<div style="background-color: #ffaa66; padding: 10px; color: #222222; border-radius: 5px;">

<center> <b>NOTE</b> </center>
</div>

<div style="background-color: #ffeecc; padding: 10px; color: #222222; border-radius: 5px;">

This method does **not** perform a reachability analysis on the entire CTMC. It simply returns the current reachability probability of the current state we are looking at, last modified by the previous reachability analysis done on the CTMC (last done by `StaminaModelGenerator.doReachabilityAnalysis()`).

</div>

### `setCurReachabilityProb(double)`
Sets the current reachability probabilty. This method is used by `StaminaModelGenerator.doReachabilityAnalysis()` when performing a reachability analysis for the entire CTMC.

### `addToReachability(double)`
Adds probabilty to reachability. If probability exceeds `1.0`, maxes to `1.0` so as not to break the rules of probability.

### `subtractFromReachability(double)`
Subtracts probability from reachability. If probability is lower than `0.0`, floors probability to `0.0`, because negative probabilities are not allowed.
