# The main STAMINA command-line class

This is the main class which takes arguments via the command line and instantiates all other classes to perform state space truncation. This is the class which contains the `main()` function and is the entry point to the program. This class contains the following public methods:

### `main()` (Main function)
Entry point to program. It creates a shutdown hook, and then instantiates an instance of `StaminaCL` (its own class as it is a `static` method), which it calls the `run()` method of.

### `run(String[])`
This function is called by `main()`. The command line arguments are passed in as a parameter, and this method initializes the model, parses properties, and sets constants. This method will read as many models as are passed in. This is also the catching point for any `PrismException` thrown by `StaminaModelGenerator` and its methods.

### `initializeSTAMINA()`
Called by `run()`, this method creates a `PrismFileLog` for logging messages and initializes the stored instance of `StaminaModelChecker`.
<!--
### `processOptions()`
Processes command line options to be passed into STAMINA. -->

### `parseArguments(String[])`
This method is used to parse all of the arguments passed into STAMINA via the command line. This method will also report whether or not certain required inputs are defined.
<div style="background-color: #ffaa66; padding: 10px; color: #222222; border-radius: 5px;">

<center> <b>NOTE</b> </center>
</div>

<div style="background-color: #ffeecc; padding: 10px; color: #222222; border-radius: 5px;">

For full details about command line options that can be passed into STAMINA, please refer to the usage documentation.

</div>

### `parseModelProperties()`
This method parses both the model and properties files passed into STAMINA. PRISM models and properties are brought in and all properties are verified unless explicitly specified.
