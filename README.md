## STAMINA - STochastic Approximate Model-checker for INfinite-state Analysis

STAMINA is an infinite-state CTMC model-checker integrated with the PRISM probabilistic model checker. It deploys a state truncation-based approach. It estimates path probabilities of reaching each state on-the-fly and terminates exploration of a path when the cumulative estimated probability along such a path drops below a predefined threshold. Each terminated path is routed to an absorbing state, in order to estimate the error probability in subsequent CTMC analysis.  After all paths have been explored or truncated, transient Markov chain analysis is applied to determine the probability of a transient property of interest specified using Continuous Stochastic Logic (CSL).  The calculated probability forms a lower bound on the probability, while the upper bound also includes the probability of the absorbing state. The actual probability of the CSL property is guaranteed to be within this range. If the probability bound is still too large compared to a user-provided probability precision value (default is 10^(-3)), STAMINA employs a property property-guided refinement technique to expand the state space to tighten the reported probability range incrementally.

##### Contact: Zhen Zhang (@zgzn) zhen.zhang@usu.edu, Brett Jepsen (@brettjepsen) brett.jepsen@aggiemail.usu.edu, Thakur Neupane (@thakurneupane) thakur.neupane@aggiemail.usu.edu 
               

Contributor(s): Brett Jepsen, Thakur Neupane, Zhen Zhang, Chris Myers, Curtis Madsen, Hao Zheng, and Chris Winstead

## Installing STAMINA

Prism and Stamina expect Git and Java to be installed. You will need sudo access to install these packages. On Ubuntu or other similar enviornments you can install them by running the following:

``sudo apt-get update && sudo apt-get -y install openjdk-11-jre openjdk-11-jdk git build-essential``

Once the above pre-requisites are installed you can use the install script below or make prism and stamina manually.

Easy Way
1. An install script is provided and can be used to run all the commands below
2. perl install.pl
3. This will install PRISM and STAMINA into the CWD of the script

Harder Way
1. Download a copy of PRISM from GitHub and build it
  	* ``git clone https://github.com/prismmodelchecker/prism prism``
  	* ``cd prism/prism``
    * ``git checkout v4.5``
  	* ``make``

  	More details about installing PRISM can be found [here](http://www.prismmodelchecker.org/).

2. Download the STAMINA from GitHub and build 
  	* ``git clone https://github.com/fluentverification/stamina.git``
  	* ``cd stamina/stamina``
  	* ``make PRISM_HOME=/path/to/prism/directory``

## Running STAMINA

``stamina/stamina/bin`` contains the executable ``stamina``. You can run STAMINA using following command: 

``/path/to/stamina/executable <model-file> <properties-file> [options]``. Please refer to the following section for details about all the options. Please see the [Prism Language Manual page](https://www.prismmodelchecker.org/manual/ThePRISMLanguage/Introduction) for information about how to create Prism model files and the [Property Specification Manual page](https://www.prismmodelchecker.org/manual/PropertySpecification/Introduction) for information about how to create property files.

Currently, STAMINA does not inherit options in PRISM. To customize the maximum heap size for Java, use command such as ``export _JAVA_OPTIONS=-Xmx12g`` to set it to be 12G. 


## All command line options

```
Usage: stamina <model-file> <properties-file> [options]

<model-file> .................... Prism model file. Extensions: .prism, .sm
<properties-file> ............... Property file. Extensions: .csl

Options:
========

-kappa <k>.......................... Probability estimate threshold [default: 1.0e-6]
-reducekappa <f>.................... Reduction factor for probability estimate threshold for refinement step.  [default: 1000.0]
-pbwin <e>.......................... Probability precision - probability window between lower and upper bound. [default: 1.0e-3]
-maxapproxcount <n>................. Maximum number of approximation iteration. [default: 10]
-noproprefine ...................... Do not use property-guided refinement. State exploration performs property-agnostic state expansion. [default: off]
-const <vals> ...................... Comma separated values for constants
	Examples:
	-const a=1,b=5.6,c=true

Other Options:
========

-rankTransitions ................... Rank transitions before expanding. [default: false]
-maxiters <n> ...................... Maximum iteration for solution. [default: 10000]
-power ............................. Power method
-jacobi ............................ Jacobi method
-gaussseidel ....................... Gauss-Seidel method
-bgaussseidel ...................... Backward Gauss-Seidel method
```

## Running case studies
There are few case studies form different domain included with STAMINA. You can run the toggle example from stamina/stamina directory using ``./bin/stamina -kappa 1e-03 -reducekappa 1000 -maxapproxcount 5 -pbwin 0.001  ../case-studies/Toggle/toggle_IPTG_100.prism ../case-studies/Toggle/toggle_IPTG_100.csl``. Please refer to the description of case studies for more details about the examples.
