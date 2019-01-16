## Basic instructions

Download a copy of PRISM from GitHub and build it

* ``git clone https://github.com/prismmodelchecker/prism prism``
* ``cd prism/prism``
* ``make``

Download the ``stamina`` repo and build the source.

* ``git clone https://github.com/thakurneupane/stamina.git``
* ``cd stamina/stamina``
* ``make PRISM_DIR=/path/to/prism/directory``

## Running STAMINA

``stamina/stamina/bin`` contains the executable ``stamina``. You can run using following command: 

``/path/to/stamina/executable <model-file> <properties-file> [options]``

There are few case studies form different domain included with the source. You can run the toggle example from ``stamina/stamina`` directory using ``bin/stamina ../case-studies/Toggle/toggle.prism ../case-studies/Toggle/toggle.csl -kappa 1.0e-3 -const IPTG=100 -const T=800``. Please refer to the description of case studies for details about the parameters. 

## Genaral usage 

```
Usage: stamina <model-file> <properties-file> [options]

<model-file> .................... Prism model file. Extensions: .prism, .sm
<properties-file> ............... Property file. Extensions: .csl

Options:
========
-kappa <k>.......................... ReachabilityThreshold [default: 1.0e-6]
-reducekappa <f>.................... Reduction factor for ReachabilityThreshold(kappa) for refinement step.  [default: 1000.0]
-pbwin <e>.......................... Probability window between lower and upperbound for termination. [default: 1.0e-3]
-maxappref <n>...................... Maximum number of approximation and refinement iteration. [default: 10]
-maxiters <n>....................... Maximum iteration for solution. [default: 10000]
-const <vals> ...................... Comma separated values for constants
	Examples:
	-const a=1,b=5.6,c=true

Other Options:
========
-power .......................... Power method
-jacobi ......................... Jacobi method
-gaussseidel .................... Gauss-Seidel method
-bgaussseidel ................... Backward Gauss-Seidel method
```