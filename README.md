## STAMINA - STochastic Approximate Model-checker for INfinite-state Analysis
STAMINA is a new infinite-state CTMC model-checker integrated with PRISM. During state space exploration, it estimates path probabilities of reaching each state on-the-fly and terminates exploration of a path when the cumulative estimated probability along such a path drops below a predefined value. Each terminated path is routed to an absorbing state, which is used to accumulate probability leakage for the truncated state space in the subsequent CTMC analysis. The resulting finite state space is analyzed using stochastic model checking for CTMCs provided by the PRISM to determine the satisfiability probability for a given CSL property. 

##### Contact: Thakur Neupane (@thakurneupane) thakur.neupane@aggiemail.usu.edu Zhen Zhang (@zgzn) zhen.zhang@usu.edu
               

Contributor(s): Thakur Neupane, Chris Myers, Curtis Madsen, Hao Zheng, Zhen Zhang

## Installing STAMINA

1. Download a copy of PRISM from GitHub and build it
  	* ``git clone https://github.com/prismmodelchecker/prism prism``
  	* ``cd prism/prism``
  	* ``make``

  	More details about installing PRISM can be found [here](http://www.prismmodelchecker.org/).

2. Download the STAMINA from GitHub and build 
  	* ``git clone https://github.com/thakurneupane/stamina.git``
  	* ``cd stamina/stamina``
  	* ``make PRISM_HOME=/path/to/prism/directory``

## Running STAMINA

``stamina/stamina/bin`` contains the executable ``stamina``. You can run STAMINA using following command: 

``/path/to/stamina/executable <model-file> <properties-file> [options]``. Please refer to the following section for details about all the options. 


## All command line options

```
Usage: stamina <model-file> <properties-file> [options]

<model-file> .................... Prism model file. Extensions: .prism, .sm
<properties-file> ............... Property file. Extensions: .csl

Options:
========

-kappa <k>.......................... ReachabilityThreshold [default: 1.0e-6]
-reducekappa <f>.................... Reduction factor for ReachabilityThreshold(kappa) for refinement step.  [default: 1000.0]
-pbwin <e>.......................... Probability window between lower and upperbound for termination. [default: 1.0e-3]
-maxrefinecount <n>................. Maximum number of refinement iteration. [default: 10]
-noproprefine ...................... Do not use property based refinement. If given, model exploration method will reduce the kappa and do the property independent refinement. [default: off]
-const <vals> ...................... Comma separated values for constants
	Examples:
	-const a=1,b=5.6,c=true

Other Options:
========

-maxiters <n> ...................... Maximum iteration for solution. [default: 10000]
-power ............................. Power method
-jacobi ............................ Jacobi method
-gaussseidel ....................... Gauss-Seidel method
-bgaussseidel ...................... Backward Gauss-Seidel method
```

## Running case studies
There are few case studies form different domain included with STAMINA. You can run the toggle example from stamina/stamina directory using ``bin/stamina -kappa 1e-03 -reducekappa 1000 -maxrefinecount 5 -pbwin 0.001  ../case-studies/Toggle/toggle_IPTG_100.prism ../case-studies/Toggle/toggle_IPTG_100.csl``. Please refer to the description of case studies for more details about the examples.
