# MDP-algorithm

## Aim ##
The algorithm is responsible for maneuvering the robot around the map autonomously, detecting and avoiding any obstacles and eventually, formulating the fastest path from the start zone to the goal zone. To achieve this aim, the algorithm team had to first create a robot simulator on their PCs to verify the correctness of the algorithm before integration can be done with the other subcomponents. After integration, through even more thorough testing, optimization can be made to the algorithm to further improve the efficiency, correctness, and reliability of the overall system. 

## Planning ##
The planning phase was especially important as we had to interact with all the other subsystems. Before work on the simulator even began, we had to thoroughly understand the capabilities of the sensors and movement capabilities of the robot. We also had to understand how to establish a connection with Raspberry Pi (RPi), as well as the format of transmission to the tablet (Android). As such, we took extra time to plan out the methodologies as well as setting up the transmission mediums and format before work could begin. The following list shows the major steps that we took to implement and complete the algorithm subsystem:
1. Establish a development platform and language 
2. Establish mode of communication with RPi and transmission format with android an Arduino
3. Identify the physical capabilities and limitations of the robot 
4. Identify an exploration algorithm
5. Identify fastest path algorithm
6. Develop simulator to verify the correctness of the algorithm 
7. Integrate with all the other subsystems 
8. Optimization of the algorithm  

## Design and Implementation ##
The development and implementation of the algorithm come in four phases: development of algorithm and simulator, modify simulator to achieve assessment checklist components, integration with the entire subsystem, and finally, optimization based on thorough testing. 

### Phase 1: Development of algorithm and simulator ###
We decided to use **Wall Follower algorithm** for the exploration phase because it is the simplest to implement, as well as the most reliable. Given that the arena is an enclosed (connected) maze, Wall Follower will always reliably find a path from one end of the maze to the other by keeping one side of the robot ‘glued’ to the wall. We used left-hand rule, which means the robot will always aim to hug to the left side of the wall while traversing around the maze as we found out that the robot is more reliable turning left than right. By using left-hand rule, we can minimize the number of left turns incurred by the robot. 
Initially, for the fastest path, we used **A * algorithm** because it is complete, and will always find the shortest path to the algorithm. However, we soon found out that even though A * will give us the shortest path, it may not be the fastest as turns incur more time than straight lines. During testing, we observed that even though the robot may reliability travel in a straight line in continuous motion, any inconsistencies during turning may set the robot off the course significantly, jeopardizing the Fastest Path leaderboard component. 
After some thought, we decided to change A * to **Dijkstra’s algorithm** instead, with the heuristics defined as number of turns incurred at current path + number of turns required to move to the next grid. With that, we were able to significantly reduce the number of turns required by the robot during the fastest path. 

As for the simulator, we developed it abiding by the **Model-View-Controller (MVC) architecture**. This allows us to keep the original implementation open for modifications which came in useful later when we decided to make changes to the existing exploration as well as fastest path algorithm. We also developed the program in an object-oriented manner, which allows us to easily interpret real-life objects (obstacles, arena, robot) into logical objects. 
For example, the Robot object (Robot.java) simulates a robot, consisting of many Sensors (Sensors.java). During simulation, we decided to have another class, called SensorSimulator (SensorSimulator.java) to simulate sensor readings that would be given by Arduino. This SensorSimulator class was able to read predefined obstacles from a file on disk and serve as a point-of-feedback to the virtual robot of the whereabouts of the obstacles. This gave us the means of validating the correctness of our algorithms before the projected integration date in Recess Week. After integration, the SensorSimulator class can be swapped out and replaced with actual readings from the Arduino, providing us a seamless way of integration with the other subsystems without making any modifications to the existing code. This proved useful to us even after integration, where we can validate any changes to the algorithm locally, on the simulator, before subjecting the robot to full runs.  

### Phase 2: Modify simulator to achieve assessment checklist components ###
The assessment checklists served as a guide of the minimal functionalities that the subcomponents should achieve. However, as some of the functionalities may not be used by us in the actual competition runs, we left out the non-essential parts of the checklist during Phase 1 of development and only added in during Phase 2. This allowed us to better focus on the core functionalities required for the robot to complete the leaderboard challenges. Nevertheless, the modifications that we had to make to our original simulator are stated below: 

No.  | Functional Specifications | Changes
------------- | ------------- | -------------
B.1 | Arena Exploration Simulator| Functionality already exists
B.2 | Fastest Path Computation Simulator | Functionality already exists
B.3 | Generate Map Descriptor | Functionality already exists
B.4 | Time and Coverage-limited Exploration Simulation | Required to implement selectors on the user interface for the input of steps per second, automatic termination via coverage and time limit. 
B.5 | Extension Beyond the Basics | Functionality already exists

### Phase 3: Integration with the other subsystems ###
Through careful planning and frequent communications within the members of the subsystems, we were able fully work on the integration during Recess Week as the requirements of each subsystem were carefully specified earlier. However, we did encounter into some issues that required both trial-and-error and stringent troubleshooting. Due to the complexity of the subsystems, this phase arguably took the most time (and frustration) to complete. Here are the challenges that the algorithm encountered during integration, and the steps we took to solve them: 

Issue | Explanation / Solution
------------- | ------------- 
Phantom Blocks (Arduino) | **Solutions:** Threshold reduction: as Arduino was figuring out an optimal and reliable threshold for the validity of sensor readings, we had to make slight modifications to the code as well. Thankfully, the threshold of sensor readings was all in a single file in the form of static variables, hence very little change was needed. Arduino team eventually found out the optimal threshold where phantom blocks occur the least, and the algorithm was always updated to reflect that.  Phantom block acceptance: we decided to implement re-updating of grids, which allowed the robot to re-update the state of the grids (unexplored / explored / obstacle) as it was being covered during exploration. However, no obstacles can be plotted on grids that the robot previously traversed over. These reduced the occurrence of phantom blocks by quite a bit. 
Concatenating Strings (Raspberry) | **Problem explanation:** We found out that when messages were being sent at too high of a frequency from us to Raspberry, it may result in Raspberry receiving messages concatenated with the current one in the transmission buffer. This may cause issues for other subsystems as the messages were corrupted during this transmission. <br>**Solutions:** Delay sending: we identified the ‘hotspots’ of the algorithm where multiple, simultaneous messages have to be sent out (either to Arduino or Android) and added delays to space out the transmission. Extra care was taken to ensure that the delay incurred will not affect the progress of the robot during the exploration or fastest path phases (only in between or after).  
Repeated or simultaneous Messages (Raspberry & Arduino) | **Problem explanation:** We identified instances where sensor readings may be transmitted to us simultaneously or rapidly, which may result in the possibility whereby the order of sensor readings may be lost during transmission. <br>**Solutions:** Transmission counter: a counter was maintained by both Arduino and Algorithms to keep track of the sequence of sensor readings being transmitted between the two subsystems, which was incremented whenever the robot moves around the arena. For our side, we order the sensor readings sequentially in a queue, which allows us to reliably read the sensor readings in order. 

### Phase 4: Optimization  ###
This final phase is only started when all the existing issues in the previous phases are fixed. During this phase, we revisited our algorithm to identify areas of improvement. Changes were then carefully tested and verified on the simulator before being merged into our version control. Only then, will the testing be done on the physical robot to maximize the productivity of meet-up hours. 

Optimization | Implementation
------------- | ------------- 
Optimization: Continuous movement for Fastest Path phase | **Implementation:** A major optimization that we made was to the Fastest Path phase. Arduino team indicated the possibility of having continuous, forward movements for the robot which takes lesser time compared to regular, discrete (grid-by-grid) movements. We settled on a sequence of characters, each encoding a series of continuous straight-line movements incrementing in length and modified the Fastest Path phase to utilize these characters. We saw huge improvements to the timing for Fastest Path. 
Continuous movement for Exploration | **Implementation:** Utilizing the encoding from discrete to continuous forward movements, we implemented this for use in the Exploration phase as well. The condition for using continuous forward movement is as follows: <br>*For a set of traversable grids directly in front of the middle of the robot, if the area that will be covered if the robot had traversed onto that grid is already covered, then there isn’t a need for the robot to stop at that grid, in which, the grid can be effectively ‘skipped’ over.* <br>Using this condition, we managed to optimize the exploration phase and see significant improvements (approx. -1 minute on average) to the timing. 

## Image Recognition ##
Initial thought: Whenever Rpi camera detects an image, return a usable value.
Problem: It is difficult to determine the exact coordinates of the arrow.
Solution & Implementation: Since Left-Wall Hugging is used in exploration, the Rpi Camera will be placed on the robot, facing the left side. It should limit its search radius to a more specific spot on the map to aid in pinpointing its exact location. Rpi will simply send a true/false value to the Algorithm team. From there, given the robots, current location and direction, the location of the image will be triangulated and added to an arraylist.

