# The `Options` Class

This class holds basic options for the CTMC model generator. It is primarily composed of getters and setters for certain properties important to STAMINA. This class contains the following methods associated with each of the following data members:

## Reachability Threshold (&kappa;)

<div style="background-color: #ffaa66; padding: 10px; color: #222222; border-radius: 5px;">

<center> <b>NOTE</b> </center>
</div>

<div style="background-color: #ffeecc; padding: 10px; color: #222222; border-radius: 5px;">

The reachability threshold is passed into STAMINA via the `kappa` command line argument.

</div>

**Definition:** The threshold probability that a particular state must be at to be considered "reachable". Since we are in a Continuous Time Markov Chain, transitions are represented by a "rate" rather than a flat probability, meaning that a rate of 0.0 is undefined for a state transition. However, the number of states that the "reduced" state space model can reach is determined by this threshold. In the reachability analysis method of the `StaminaModelGenerator()` class, a truncated state space is created, based on this reachability threshold.

STAMINA only includes states where the probability mass is clustered, starting breadth-first from the initial state. This means that states less likely to be chosen and included in the truncated model if:

1. They are farther away from the initial state.
2. There is a low transition rate to that state.

<div style="background-color: #ff6666; padding: 10px; border-radius: 5px;">

<center> <b>WARNING</b> </center>
</div>

<div style="background-color: #ffaaaa; padding: 10px; border-radius: 5px; color: black;">

The probability threshold is the probabilistic state search termination value; i.e., the lower the reachability threshold, the more states are to be explored, and the longer the simulation could take. If the probability threshold is set too low, then very little state space truncation could occur, causing the system to run out of resources. Do *not* set the value of the reachability threshold to 0, as this will result in an *infinite* (and therefore uncomputable) state space.

</div>


**Associated methods:**

1. `getReachabilityThreshold()`: returns the reachability threshold.
2. `setReachabilityThreshold(double)`: sets the reachability threshold. Currently does not check the range that it ought to be in.
    - TODO: maybe implement a check that warns about memory limits for too small of a reachability threshold.?

## Kappa (&kappa;) reduction factor (r<sub>&kappa;</sub>)

**Definition:** the kappa reduction factor is the reduction factor on the reachability threshold, `kappa` (see above). It defines how much the reachability threshold is reduced by: &kappa;<sub>min</sub> = &kappa; / r<sub>&kappa;</sub>. As mentioned above, too low of a reachability threshold could cause overuse of system resources, meaning that the kappa reduction factor ought not to be excessively high.

The &kappa; reduction factor is used to compute the lower bound probability P<sub>min</sub>, the lower bound of the probability that &Phi;, the state formula, being satisfied within the state space.

**Associated methods:**

1. `getKappaReductionFactor()`: returns the kappa reduction factor.
2. `setKappaReductionFactor(double)`: sets the kappa reduction factor.


## Misprediction Factor (m)

**Definition:** This is used in determining the termination of the exploration. If &Pi; is the probability we are in a terminal state, then exploration is terminated when &Pi; < w / m. Additionally, if the bound between  P<sub>max</sub> and P<sub>min</sub> is greater than the window, the misprediction factor will be updated accordingly. This updating is done in `StaminaModelGenerator.doReachabilityAnalysis()`.

**Associated methods:**

1. `getMispredictionFactor()`: returns the misprediction factor.
2. `setMispredictionFactor(double)`: sets the misprediction factor.

## Max Approximate Count

**Definition:** The approximate count of states in the (truncated) CTMC.

**Associated methods:**

1. `getMaxApproxCount()`: returns the max approximate count of states in the CTMC.

## Max Refinement Count

**Definition:** The maximum number of iterations used in computing the upper and lower bounds of the probabilities of reaching a certain state, P<sub>max</sub> and P<sub>min</sub>.

**Associated methods:**

1. `setMaxRefinementCount()`: sets the max refinement count.

## Probability Error Window (w)

**Definition:** The defined maximum difference between P<sub>max</sub> and P<sub>min</sub>. This is user specified, and affects the computation of the misprediction factor, m.

**Associated methods:**

1. `getProbErrorWindow()`: gets the probability error window.
2. `setProbErrorWindow(double)`: sets the probability error window.

## No Property Refining

**Definition:** Whether or not to use property based refinement.

**Associated methods:**

1. `setNoPropRefine(boolean)`: Sets whether or not there is property refining.
2. `getNoPropRefine()`: Gets whether or not there is property refining.

## Rank Transitions

**Definition:** a `boolean` representing whether or not rank transitions will be used.

**Associated methods:**

1. `setRankTransitions(boolean)`: sets whether or not there are rank transitions.
2. `getRankTransitions()`: gets whether or not there are rank transitions.

## CUDD Memory Limit

**Definition:** A string representing the memory limit available to STAMINA and the associated PRISM model we are building.

<div style="background-color: #ff6666; padding: 10px; border-radius: 5px;">

<center> <b>WARNING</b> </center>
</div>

<div style="background-color: #ffaaaa; padding: 10px; border-radius: 5px; color: black;">
Problems could arise if there are more states needed to be built than memory available, so make sure that the reachability threshold and its reduction factor are reflective of your memory limits. STAMINA will terminate if a memory limit is hit, regardless of whether or not the probability window is found tight enough.
</div>

**Associated methods:**

1. `setCuddMemoryLimit(String)`: sets the CUDD memory limit. The input string should be formatted as `<number><unit prefix>`. Acceptable inputs are `10g`, `100M`, etc.
2. `getCuddMemoryLimit()`: returns the CUDD memory limit.

## Export Model

**Definition:** a `boolean` representing whether or not we are going to export the model.

**Associated methods:**

1. `getExportModel()`: gets whether or not we are going to export the model.
2. `setExportModel(boolean)`: sets whether or not we are going to export the model.

## Export Filename

**Definition:** If we are going to export the model, to what filename will we export it?

**Associated methods:**

1. `getExportFileName()`: gets the filename where the model would be exported to if `exportModel` is set to `true`.
2. `setExportFileName(String)`: sets the filename associated with where we will export the model if `exportModel` is set to `true`.

## Export Perimeter States

**Definition:** A `boolean` representing whether or not we are going to export the perimeter states to file.

**Associated methods:**

1. `getExportPerimeterStates()`: gets whether or not we are going to export the perimeter states.
2. `setExportPerimeterStates(boolean)`: sets whether or not we are going to export the perimeter states.

## Export Perimeter Filename

**Definition:** if we are going to export the perimeter states to a file, what shall be the filename?

**Associated methods:**

1. `getExportPerimeterFilename()`: gets the filename to which we would export the perimeter states.
2. `setExportPerimeterFilename(String)`: sets the filename to which the perimeter states would be exported.

## Import Model

**Definition:** a `boolean` representing whether or not we are going to import a model file from PRISM. Acceptable file extensions are `.pm`, `.prism`, and others associated with the PRISM modelling system. However, this tool *does not* check the file extension, or its contents before trying to import, so importing an incompatible file will raise a `PrismException`.

**Associated methods:**

## Import Filename

**Definition:** The name of the file whose models STAMINA is going to import.

**Associated methods:**

1. `getImportFileName()`: returns the name of the model file to be imported.
2. `setImportFileName(String)`: lets the user set the name of the model file to be imported.

## Specific Property

**Definition:** Whether or not this is associated with a specific property.

**Associated methods:**

1. `getSpecificProperty()`: gets whether or not this is a specific property.
2. `setSpecificProperty(boolean)`: sets whether or not this is a specific property.

## Property Name

**Definition:** The name of the specific property associated with these options.

**Associated methods:**

1. `getPropertyName()`: gets the name of the specific property associated with these options.

## Export Transitions to File

**Definition:** The filename where the export transitions will be sent.

**Associated methods:**

1. `getExportTransitionsToFile()`: gets the filename where the transitions will be exported to.
2. `setExportTransitionsToFile(String)`: sets the filename where the transitions will be exported to.
