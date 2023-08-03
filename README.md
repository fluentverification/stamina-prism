## STAMINA - STochastic Approximate Model-checker for INfinite-state Analysis

STAMINA is an infinite-state CTMC model-checker integrated with the PRISM probabilistic model checker. It deploys a state truncation-based approach. It estimates path probabilities of reaching each state on-the-fly and terminates exploration of a path when the cumulative estimated probability along such a path drops below a predefined threshold. Each terminated path is routed to an absorbing state, in order to estimate the error probability in subsequent CTMC analysis.  After all paths have been explored or truncated, transient Markov chain analysis is applied to determine the probability of a transient property of interest specified using Continuous Stochastic Logic (CSL).  The calculated probability forms a lower bound on the probability, while the upper bound also includes the probability of the absorbing state. The actual probability of the CSL property is guaranteed to be within this range. If the probability bound is still too large compared to a user-provided probability precision value (default is 10^(-3)), STAMINA employs a property property-guided refinement technique to expand the state space to tighten the reported probability range incrementally.


## Parent Repository

To get both STAMINA/PRISM and STAMINA/STORM, please clone the parent repository, at [https://github.com/fluentverification/stamina-prism](https://github.com/fluentverification/stamina-prism)

##### Contact: Riley Roberts (riley.roberts@usu.edu) and Zhen Zhang (zhen.zhang@usu.edu)

##### Contributor(s):  Riley Roberts, Trent Wall, Brett Jepsen, Thakur Neupane, Josh Jeppson, Zhen Zhang, Chris Myers, Curtis Madsen, Hao Zheng, and Chris Winstead

#### To cite the most recent release of STAMINA/PRISM, STAMINA 2.0, please use the tool paper from VMCAI'22:
Roberts R., Neupane T., Buecherl L., Myers C.J., Zhang Z. (2022) STAMINA 2.0: Improving Scalability of Infinite-State Stochastic Model Checking. In: Bernd F., Wies, T. (eds) Verification, Model Checking, and Abstract Interpretation. VMCAI 2022. Lecture Notes in Computer Science, Springer, Cham. Download link: https://link.springer.com/chapter/10.1007/978-3-030-94583-1_16

#### To cite the previous version of STAMINA/PRISM, STAMINA 1.0, please use the tool paper from CAV'19:
Neupane T., Myers C.J., Madsen C., Zheng H., Zhang Z. (2019) STAMINA: STochastic Approximate Model-Checker for INfinite-State Analysis. In: Dillig I., Tasiran S. (eds) Computer Aided Verification. CAV 2019. Lecture Notes in Computer Science, vol 11561. Springer, Cham. Download link: https://link.springer.com/chapter/10.1007/978-3-030-25540-4_31

## Installing STAMINA

Prism and Stamina expect Git and Java to be installed. You will need sudo access to install these packages. On Ubuntu or other similar enviornments you can install them by running the following:

```bash
sudo apt-get update && sudo apt-get -y install openjdk-11-jre openjdk-11-jdk git build-essential
```

Once the above pre-requisites are installed you can use the install script below or make prism and stamina manually.

Easy Way
1. An install script is provided and can be used to run all the commands below
```shell
perl -e $(curl https://raw.githubusercontent.com/fluentverification/stamina-prism/main/install.pl)
```
2. This will install PRISM and STAMINA into the CWD of the script

Harder Way
1. Download a copy of PRISM from GitHub and build it
```shell
git clone https://github.com/prismmodelchecker/prism prism
cd prism/prism
git checkout v4.8 # Stamina should work with the latest version of PRISM but v4.8 is PRISM's latest official release
make -j$(nproc --ignore=1)
export PRISM_HOME=$(pwd)
```

  	More details about installing PRISM can be found [here](http://www.prismmodelchecker.org/).

2. Download the STAMINA from GitHub and build
```shell
git clone https://github.com/fluentverification/stamina-prism.git
cd stamina/stamina
make PRISM_HOME=$PRISM_HOME # This variable should have been set above
```

## Running STAMINA

``./stamina/bin`` contains the executable `pstamina`. You can run STAMINA using following command:

`pstamina [MODEL FILE] [PROPERTIES FILE] [OPTIONS...]`. Please refer to the following section for details about all the options. Please see the [Prism Language Manual page](https://www.prismmodelchecker.org/manual/ThePRISMLanguage/Introduction) for information about how to create Prism model files and the [Property Specification Manual page](https://www.prismmodelchecker.org/manual/PropertySpecification/Introduction) for information about how to create property files.

Currently, STAMINA does not inherit options in PRISM. To customize the maximum heap size for Java, use command such as `export _JAVA_OPTIONS=-Xmx12g` to set it to be 12G.

You can add `pstamina` to your `PATH` (Linux and MacOS) using the following command:

```bash
# Find out what shell you are using via:
echo $SHELL # Will give you /usr/bin/bash or /bin/bash if on bash, and /usr/bin/zsh or /bin/zsh if on zsh
# For zsh users (MacOS default and many Linux distributions)
echo "export PATH=\$PATH:$(pwd)/stamina/bin" >> .zshrc
# For bash users (Default on most Linux distributions)
echo "export PATH=\$PATH:$(pwd)/stamina/bin" >> .bashrc
# If you're on fish, dash, etc., we assume you know enough about your shell's .*rc file to modify the commands above as needed
```

## All command line options

```
USAGE: pstamina [MODEL FILE] [PROPERTIES FILE] [OPTIONS...]
------------------------------------------------------------------------
MODEL FILE (string)..............Prism model file. Extensions: .prism, .sm
PROPERTIES FILE (string).........Property file. Extensions: .csl
------------------------------------------------------------------------
OPTIONS:
kappa (double)...................Reachability threshold for first iteration [default:
                                 1.0]
rKappa (double)..................Reduction factor for ReachabilityThreshold (kappa) for
                                 refinement step. [default: 1.25]
approxFactor (double)............Factor to estimate how far off our reachability predictions
                                 will be [default: 2.0]
probWin (double).................Probability window between lower and upper bound for
                                 termination. [default: 1.0e-3]
cuddMaxMemory (string)...........Maximum cudd memory. Expects the same format as PRISM
                                 [default: "1g"]
export (string)..................Export model to a series of files with provided name
                                 (no extension)
exportPerimeterStates (string)...Export perimeter states to a file. Please provide a filename.
                                 This will append to the file if it is existing.
import (string)..................Import model to a file. Please provide a filename without
                                 an extension
property (string)................Choose a specific property to check in a model file that
                                 contains many
noPropRefine.....................Do not use property based refinement. If given, model
                                 exploration method will reduce the kappa and do the
maxApproxCount (int).............Maximum number of approximation iterations. [default:
                                 10]
maxIters (int)...................Maximum number of iterations to find solution. [default:
                                 10000]
method (string)..................Method to solve CTMC. Supported methods are 'power',
                                 'jacobi', 'gaussseidel', and 'bgaussseidel'.
const (string)...................Comma separated values for constants (ex: "a=1,b=5.6,c=true")
rankTransitions..................Rank transitions before expanding [default: false]
exportTrans (string).............Export the list of transitions and actions to a specified
                                 file name, or to trans.txt if no file name is specified.
                                 teIndex actionLabel
mrmc.............................Exports an MRMC file, only works if `export` also selected
------------------------------------------------------------------------
To show this message again, use the '-help'/'--help' flags. To show usage, use the '-usage'/'--usage' flags. To show an 'about' message, use the '-about'/'--about' flags.

```

## Running case studies

Case studies are included in the parent directory.
