# Case Studies

## Toggle
This is a genetic circuit model for the toggle switch. Two input toggle switch can be set to the OFF state by supplying it with aTc and set to ON state by supplying IPTG. The inputs are IPTG and aTc and the outputs are LacI, TetR and GFP.

### Parameters
* IPTG : Initial value of IPTG. IPTG=0 is logic OFF and IPTG=100 is logic ON.
* T    : Time parameter for properties

## Tandem
This PRISM case study represents a CTMC which consists of a M/Cox₂/1-queue (Coxian distribution with two phases) sequentially composed with a M/M/1-queue. The servers process the tasks of the queues.

### Parameters
* c : Queue capacity
* T : Time parameter for properties

## RobotWorld
This PRISM case study considers an n×n grid world with a robot moving from the bottom left corner to the top right corner, first along the bottom edge and then along the right edge. The time taken by the robot to move from one square to another is exponentially distributed. There is a janitor moving randomly around the grid (initially in the top right hand corner), and the robot cannot move into a square occupied by the janitor. The robot also randomly sends a signal to the base station.

### Parameters
* n	: Size of the grid


## Polling
This PRISM case study represents a model of cyclic server polling system. 

### Parameters
* N : The number of stations handled by the polling server
* T : Time parameter for properties