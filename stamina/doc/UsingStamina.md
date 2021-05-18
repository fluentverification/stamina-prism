# Using STAMINA

<div style="background-color: #ffaa66; padding: 10px; color: #222222; border-radius: 5px;">

<center> <b>NOTE</b> </center>
</div>

<div style="background-color: #ffeecc; padding: 10px; color: #222222; border-radius: 5px;">

This assumes you have already compiled STAMINA. If not, please refer to the README file in the main directory.

</div>

The STAMINA executable is located in `./stamina/stamina/bin`. If you would like to install it to a system path and use it from there, you can do it with the following commands:

### Linux, BSD, etc.
```
cd /path/to/stamina
sudo mkdir /usr/bin/stamina
sudo mv stamina/stamina/bin/stamina /usr/bin/stamina
```
### MacOS
```
TODO
```
### Windows
```
TODO
```
## Running STAMINA
When this is done, you can use the STAMINA executable by simply typing `stamina` into your terminal or command line. STAMINA takes two parameters, the model file and the properties file, followed by options. Usage for STAMINA is approximately as follows:
```
stamina <model-file> <properties-file> [options]
```
**The model file** is a standard PRISM model file, but it *must* be of type `ctmc`. If not, STAMINA will refuse it. Acceptable extensions are `.prism` and `.sm`.

**The properties file** is a PRISM properties file. STAMINA takes files with extension `.csl`.

**Options** allow you to customize how STAMINA runs. STAMINA has the following options:
1. `-kappa`: the reachability threshold (&kappa;). For more information please read the [options documentation](Options.md).
2. `-reducekappa`: the reduction factor for the reachability threshold (r<sub>&kappa;</sub>).
3. `-pbwin`: the acceptable window between the upper and lower bound: (P<sub>max</sub> - P<sub>min</sub>)
4. `-maxapproxcount`: the maximum number of iterations in approximation.
5. `-noproprefine`: does not use property guided refinement.
6. `-cuddmaxmem`: the max amount of CUDD memory to allocate.
7. `-export`: file to export truncated model to.
8. `-exportPerimeterStates`: file to export the perimeter states to.
9. `-import`: model file (in CWD, and without file extension) to import model from.
10. `-property`: allows you to define a specific property to check from the model passed in.
11. `-const`: allows for constants to be specified. Constants must be separated by commas and named. 
    - Example usage: `-const a=3,b=2,c=true`
12. `-rankTransitions`: whether to use rank transitions before expanding
13. `-maxiters`: the maximum number of iterations to achieve a solution.
14. `-power`: use the Power method.
15. `-jacobi`: use the Jacobi method
16. `-gaussseidel`: use the Gauss-Seidel method
17. `-bgaussseidel`: use the backwards Gauss-Seidel method. 