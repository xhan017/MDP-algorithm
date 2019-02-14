package Controller;

import Model.*;
import Model.Robot;
import View.DisplayMapGUI;

import java.awt.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class Algorithm {
    Robot r;
    Stack path;
    Scanner s;
    Point goalPoint = new Point(1, 13);
    Boolean isExploration = false;
    Boolean discreteMovement = false;
    Point startingPosition;
    Direction startingDirection;
    ArrayList<Grid> pathTaken;


    /**
     * Uses the fastest path algorithm (Dijkstra) to move the robot from current location to a goal.
     *
     * @param r                 An instance of the robot
     * @param target            The target location to travel to
     * @param isExploration     If set to true = exploration. Else, = fastest path.
     * @param discreteMovement  If set to true = use discrete movement. Else = continuous.
     */
    public Algorithm(Robot r, Point target, boolean isExploration, boolean discreteMovement){
        this.r = r;
        this.isExploration = isExploration;
        this.discreteMovement = discreteMovement;

        // Clear existing path traversed over by the robot
        Variables.traversedOver.clear();

        // If using continuous movement, prevent any sensor readings from affecting current map
        if(!discreteMovement){
            System.out.println("SETTING noUpdate to TRUE");
            Variables.noUpdate = true;
        }

        // If using fastest path, start timer
        if(!isExploration){
            Variables.TIMER_STOP = false;
            Stopwatch sw = new Stopwatch();
            sw.start();
            System.out.println("Starting fastest path!");
            DisplayMapGUI.setStatusBar("Starting fastest path! ", Color.LIGHT_GRAY);
        }

        // Run the algorithm to the target
        run(Variables.ALGORITHM, target);

        // Stop the timer only if running fastest path
        if(!isExploration) Variables.TIMER_STOP = true;
    }

    /**
     * Controls the finer aspects of the algorithm. Dijkstra simply returns the set of movements
     * required for the robot to traverse from current location to the target. We control how we handle
     * that set of movements here.
     *
     * If isExploration is TRUE: Timer not used
     *    isExploration is FALSE: Timer started, target treated as way point
     *      -> means Dijkstra is called a second time to go from way point to goal zone
     *
     * If discreteMovement is TRUE: movement sent to robot 1 grid at a time
     *    discreteMovement is FALSE: movement sent to robot is continuous movement, map not updated (noUpdate = true)
     *
     * @param algorithm     The algorithm to use. For now, doesn't matter.
     * @param target        The target location the robot needs to move to
     */
    private void run(String algorithm, Point target) {
        s = new Scanner(System.in);
        String discretePath = "";
        String continuousPath = "";

        Dijkstra d = new Dijkstra(r.getMap());
        discretePath = d.start(r.getCurrentLocation(), r.getCurrentDirection(), target);

        System.out.println("PATH: "+discretePath);
        // Secondary set of movements from way point to goal is required if ran during FP
        if(!isExploration){
            d = new Dijkstra(r.getMap());
            discretePath += d.start(target, Variables.waypointDirection, new Point(1, 13));
        }

        // If path is empty, just return
        if(discretePath.length()==0){
            System.out.println("No movements needed! Returning... ");
            System.out.println("PREVENT_SEND: "+Variables.PREVENT_SEND);
            return;
        }

        // Parse discrete movement to continuous movement if required
        if(!discreteMovement){
            continuousPath = parseMotion(discretePath);
            System.out.println("Continuous movement: " + continuousPath);
        }else{
            // System.out.println("Discrete movement: " + discretePath);
        }

        // If it is exploration and using discrete movement, need to move robot step by step
        // Sensors are required before moving again after every step, as usual.
        // If it is using discrete movement, move robot
        if(isExploration && discreteMovement){
            moveRobot(discretePath);
            return;
        }

        // Append 'f' to string only if it is fastest path
        if(!isExploration) continuousPath = continuousPath+'f';

        // If we reached here, it means we are using continuous movement / fastest path
        if(!Variables.SIMULATION){
            r.sendToArd(continuousPath);
            // Set prevent_send to true to prevent further commands from being sent to Arduino
        }else{
            DisplayMapGUI.setStatusBar("Sent: "+continuousPath, Color.LIGHT_GRAY);
            System.out.println("[Algorithm.java] Fake send: "+continuousPath);
        }

        // Setting PREVENT_SEND to true will prevent subsequent commands from sending to Arduino.
        // This allows us to move the virtual robot sequentially with the actual physical robot, without
        // sending any commands to the robot
        if(!discreteMovement) Variables.PREVENT_SEND = true;
        moveRobot(discretePath);
        // Reset the flags to false so that we still can send shit later on, if needed
        if(!discreteMovement) Variables.PREVENT_SEND = false;

        if(!isExploration) DisplayMapGUI.setStatusBar("Finished fastest path!", Color.GREEN);

        // Print out robot path. Purely for display.
//        for(Grid g: Variables.traversedOver){
//            DisplayMapGUI.highlightGrid(g, Color.GREEN);
//        }
    }

    /**
     * Moves the virtual robot
     * If discrete movement is not used:
     * - noUpdate will be set to true. This means sensor readings does not affect the current map state.
     * - Sending of messages will also be disabled via PREVENT_SEND = true.
     *
     * If ran during fastest path:
     * - wait for sensor reading before moving 1 grid on display
     *
     * If ran during exploration:
     * - no need to wait for sensor readings - robot class handles that.
     *
     * @param path  Takes in a set of movements (e.g. WWWWDDAAAAWWWWWDDADADADA)
     */
    private void moveRobot(String path){
        // To test
        if(Variables.PREVENT_SEND){
            System.out.println("[Algorithm.java] Not sending anything to Arduino!");
        }else{
            System.out.println("[Algorithm.java] Sending stuff to Arduino!");
        }
        System.out.println("Path: "+path);
        char[] movements = path.toCharArray();

        // if(!Variables.SIMULATION)   Variables.SENSOR_QUEUE.add(" ");

        for(char c: movements){
            System.out.println(">>> Sending: "+c);
            //Scanner s = new Scanner(System.in);
            //s.nextLine();
            if(Variables.SIMULATION){
                if(Variables.PREVENT_SEND){
                    waitFor(1);
                }else{
                    waitFor(100);
                }
            }else{
                System.out.println("[Algorithm.java] Waiting for sensor!");
                // Wait for sensor
                if(!isExploration){
                    /*while(Variables.SENSOR_QUEUE.isEmpty()){
                    }
                    System.out.println("[Algorithm.java] Sensor received!");
                    // Remove 1 element
                    Variables.SENSOR_QUEUE.remove();*/
                }
                if(!discreteMovement){
                    waitFor(1500);
                }
            }
            switch(c){
                case 'W':
                    r.robotMoveForward(1);
                    break;
                case 'A':
                    r.turn("LEFT");
                    break;
                case 'D':
                    r.turn("RIGHT");
                    break;
            }
        }
        // If during exploration & continuous movement, means we are returning
        // to the end
//        if(isExploration && !discreteMovement){
//            Variables.PREVENT_SEND = false;
//            System.out.println("1. Robot is facing: "+r.getCurrentDirection());
//            System.out.println("1. PREVENT_SEND"+Variables.PREVENT_SEND);
//            waitFor(5000);
//            if (r.getCurrentDirection() == Direction.D) {
//                r.turn("LEFT");
//            } else if (r.getCurrentDirection() == Direction.S) {
//                r.turn("RIGHT");
//                r.turn("RIGHT");
//            } else if (r.getCurrentDirection() == Direction.A) {
//                r.turn("RIGHT");
//            } else {
//                if (Variables.debugWH) System.out.println("[WallHugging.java] line 72");
//            }
//            System.out.println("2. Robot is facing: "+r.getCurrentDirection());
//            System.out.println("2. PREVENT_SEND"+Variables.PREVENT_SEND);
//        }
    }

    // Parses multiple forward movements to make use of continuous motion
    private String parseMotion(String pathTakenToSend){
        char[] moves = pathTakenToSend.toCharArray();
        String toSend = "";
        String temp = "";
        for(int i = 0; i < moves.length; i++){
            if(moves[i] == 'W'){
                temp += 'w';
            }else{
                // Check if there are any previous W
                if(temp.length()==0){
                    // If no previous W, just add
                    if(moves[i] == 'A'){
                        toSend+='z';
                    }else{
                        toSend+='c';
                    }
                }else{
                    // If there are previous Ws, check how many
                    toSend+=parser(temp.length());
                    temp = "";
                    if(moves[i] == 'A'){
                        toSend+='z';
                    }else{
                        toSend+='c';
                    }
                }
            }
        }
        // At the end, check if there are any trailing Ws
        toSend+=parser(temp.length());
        return toSend;
    }

    // Number of occurrences of W
    private char parser(int i){
        switch(i){
            case 1:
                return 'w';
            case 2:
                return 'R';
            case 3:
                return 'T';
            case 4:
                return 'Y';
            case 5:
                return 'U';
            case 6:
                return 'I';
            case 7:
                return 'O';
            case 8:
                return 'P';
            case 9:
                return 'F';
            case 10:
                return 'G';
            case 11:
                return 'H';
            case 12:
                return 'J';
            case 13:
                return 'K';
            case 14:
                return 'L';
            case 15:
                return 'V';
            case 16:
                return 'B';
            case 17:
                return 'N';
        }
        return ' ';
    }

    // Returns number of Ws needed for given character
    private int reverseParser(char c){
        switch(c){
            case 'W':
                return 1;
            case 'R':
                return 2;
            case 'T':
                return 3;
            case 'Y':
                return 4;
            case 'U':
                return 5;
            case 'I':
                return 6;
            case 'O':
                return 7;
            case 'P':
                return 8;
            case 'F':
                return 9;
            case 'G':
                return 10;
            case 'H':
                return 11;
            case 'J':
                return 12;
            case 'K':
                return 13;
            case 'L':
                return 14;
            case 'V':
                return 15;
            case 'B':
                return 16;
            case 'N':
                return 17;
        }
        return ' ';
    }

    private void waitFor(int time){
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
