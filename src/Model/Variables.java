package Model;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;


public class Variables {
    // Set goal point
    public static Point GOAL_POINT = new Point(1, 13);

    // Set whether to use simulation or robot
    public static boolean SIMULATION = false;

    // For fastest path, remove all delays
    public static boolean FASTEST_PATH = false;

    // Static variables for simulation
    // If Arduino returns 3, it means the sensor is not picking up any obstacles
    // Therefore, only set max range to the max number they will return!
    public static final int SENSOR_FRONT_RANGE = 4;
    public static final int SENSOR_RANGE_A = SENSOR_FRONT_RANGE;
    public static final int SENSOR_RANGE_B = SENSOR_FRONT_RANGE;
    public static final int SENSOR_RANGE_C = SENSOR_FRONT_RANGE;
    public static final int SENSOR_RANGE_D = 3;
    public static final int SENSOR_RANGE_E = 3;
    public static final int SENSOR_RANGE_F = 5;

    // This is important for transmission sequencing
    public static int counter = 0;

    // For manual control via UI. Actual run will not depend on these.
    public static boolean COMPLETED_EXPLORATION = false;
    public static boolean COMPLETED_FP = false;
    public static boolean STOP_ROBOT = false;

    // To check if the goal has been reached in the run
    public static boolean reachedGoal = false;

    // Sets sending to false during fastest path so that we won't send double commands to Arduino
    public static boolean PREVENT_SEND = false;
    public static boolean noUpdate = false;
    public static boolean noUpdateUI = false;

    // Set exploration threshold
    public static final double explorationThreshold = 100;

    // Static variable for file i/o for testing
    public static final String INPUT_FILE_NAME = "sample_arena_2";

    // Static variable for selecting algorithm (defunct)
    public static final String ALGORITHM = "A_Star";

    // Connection details
    public static final String RPI_IP_ADDRESS = "192.168.13.13";
    public static final int RPI_PORT = 5182;

    // Timer stuff
    public static int stepsPerSec = 1000;

    public static String fullPath = "";

    // Sensor queue
    public static Queue<String> SENSOR_QUEUE = new LinkedList<>();

    // Set to true to debug A * algo
    public static boolean debugAlgo = true;
    // Set to true to debug Wallhugging
    public static boolean debugWH = false;
    // Set to true to debug Robot
    public static boolean debugRobot = false;

    //
    public static Direction waypointDirection;
    public static ArrayList<Grid> traversedOver = new ArrayList<>();
    public static boolean TIMER_STOP = false;

    //
    public static boolean DELAY_INDUCER = false;
}
