# The Stamina Model Checker Class (extension of `prism.Prism`)

This class is the main class instantiated by `StaminaCL` and the class that contains the `InfCTMCModelGenerator` that performs the reachability analysis. This class has the following public methods:

### Constructor
The constructor extends the constructor of the `prism.Prism` constructor by first calling the superclass's constructor, and then attempting to set the max CUDD memory limit.

### `modelCheckStamina(PropertiesFile, Property)`
This actually performs the model check on the currently loaded model. It returns a result of type `Result`.