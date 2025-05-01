1.Clone the project and set up the Gradle configuration.

-Ensure that the required dependencies for Logger and SQLite are installed.

2.In the main class, go to line 53 and modify the last parameter:

-Set it to false to run the algorithm locally using data from the emulator.

⚠️ Make sure the emulator is running on port 5000.

3.If you're using the hybrid solution (with a frontend):

-Set the parameter on line 53 to true.

-Run the React frontend app provided by my colleague.

4.Change the seed and paramaters 
-In Service/EmulatorService - line 34 you can change the paramaters you want to run the reset with 


The algorithm used to solve the dispatches was a greedy one, base on the distance between the target event and the closest source of cars.
