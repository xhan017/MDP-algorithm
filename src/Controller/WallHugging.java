package Controller;


import Model.*;
import Model.Robot;
import View.DisplayMapGUI;
import Model.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class WallHugging {
    Map m;
    Robot r;
    Scanner s;

    private static Boolean robotStop = true;
    public static Boolean percentageLimitOn = false;
    private static float explorationPercentage = 0;
    public static Boolean timeLimitOn = false;
    private static int totalTime = 0;
    private boolean reachedGoal = false;

    // From here on, the algorithm interacts only with the map and robot (current position + direction)
    // Sensors are not used
    public WallHugging(Robot r) {
        this.m = r.getMap();
        this.r = r;
        this.s = new Scanner(System.in);
        System.out.println("[console] Wallhugging starting...");
    }

    public void start() {
        try {
            if (Variables.debugWH) System.out.println("[WallHugging.java] Wallhugging starting... 2");
            DisplayMapGUI.setStatusBar("[1/4] Started wallhugging! ", Color.LIGHT_GRAY);
            robotStop = false;
            leftWallHugging();
            DisplayMapGUI.setStatusBar("[2/4] Checking unexplored grids... ", Color.YELLOW);
            checkUnexploredGrids();
            m.markExplored(4, 10);
            DisplayMapGUI.setStatusBar("[3/4] Going back to start... ", Color.LIGHT_GRAY);
            Algorithm a = new Algorithm(r, new Point(18, 1), true, false);
            //
            Variables.PREVENT_SEND = false;
            System.out.println("1. Robot is facing: " + r.getCurrentDirection());
            System.out.println("1. PREVENT_SEND" + Variables.PREVENT_SEND);
            Variables.DELAY_INDUCER = true;
            if (r.getCurrentDirection() == Direction.D) {
                r.turn("LEFT");
            } else if (r.getCurrentDirection() == Direction.S) {
                r.turn("RIGHT");
                r.turn("RIGHT");
            } else if (r.getCurrentDirection() == Direction.A) {
                r.turn("RIGHT");
            } else {
                if (Variables.debugWH) System.out.println("[WallHugging.java] line 72");
            }
            System.out.println("2. Robot is facing: " + r.getCurrentDirection());
            System.out.println("2. PREVENT_SEND" + Variables.PREVENT_SEND);
            // rotate robot to face UP

            DisplayMapGUI.setStatusBar("[4/4] Exploration done! ", Color.GREEN);
            Variables.COMPLETED_EXPLORATION = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        r.sendToArd("M");
    }

    public static void manualTermination() {
        robotStop = true;
    }

    public static void selectPercent(int s) {
        explorationPercentage = s;
    }

    public static void selectTime(int s) {
        totalTime = s;
    }

    private boolean leftWallHugging() throws InterruptedException {
        // While we are not at goal
        while (!m.isGoal(r.getCurrentLocation()) && !robotStop && !timeLimitOn) {
            //s.nextLine();
            if (Variables.SIMULATION) {
                // Variables.stepsPerSec = 25;
            } else {
                String input;
                input = "";
                if (input == "Q") {
                    r.turn("Q");
                } else if (input == "E") {
                    r.turn("E");
                } else {
                    //
                }
            }
            if (percentageLimitOn) {
                float counter = 0; // Accounts for robots size of 3x3
                char[][] mapGrid = m.getMap();
                for (int i = 0; i < 20; i++) {
                    for (int j = 0; j < 15; j++) {
                        if (Character.toString(mapGrid[i][j]).matches("1"))
                            counter++;
                    }
                }
                if (((counter / 300) * 100) > explorationPercentage) {
                    if (Variables.debugWH)
                        System.out.println("[WallHugging.java] " + counter + " grids explored. " + Float.toString((counter / 300) * 100) + "%");
                    return false;
                }
            }

            if (!isLeftSideClear()) { // if left is not clear
                if (isFrontClear()) { // if front is clear
                    r.robotMoveForward(1);
                } else {              // if front is not clear
                    if (isRightSideClear()) { // if right is clear
                        r.turn("RIGHT");
                        r.robotMoveForward(1);
                    } else {          // if right is not clear
                        r.turn("RIGHT");
                        if (isFrontClear()) {
                            r.robotMoveForward(1);
                        } else {
                            if (Variables.debugWH) System.out.println("\t[WallHugging.java] I may be trapped.");
                        }
                    }
                }
            } else {
                r.turn("LEFT");
                r.robotMoveForward(1);
            }

            // Checks if continuous movement can be used. Use only during simuation for testing
            if(Variables.SIMULATION){
                // bottom row
                // left row
                if((r.getCurrentLocation().getX() == 18 )||
                        (r.getCurrentLocation().getY() == 1)||
                        (r.getCurrentLocation().getY() == 13)||
                        (r.getCurrentLocation().getX() == 1)){
                    lookahead();
                }
            }else{
                if((r.getCurrentLocation().getX() == 18 )||
                        (r.getCurrentLocation().getY() == 1)||
                        (r.getCurrentLocation().getY() == 13)||
                        (r.getCurrentLocation().getX() == 1)){
                    lookahead();
                }
            }

            // stop when robot reaches origin & after it has passed goal point
            if (m.isStart(r.getCurrentLocation()) && Variables.reachedGoal) {
                MapStorage.storeMap(m.getMap());
                break;
            }
        }

        while (!m.isGoal(r.getCurrentLocation()) && !robotStop && timeLimitOn) {
            if (!isLeftSideClear()) { // if left is not clear
                if (isFrontClear()) { // if front is clear
                    r.robotMoveForward(1);
                } else {              // if front is not clear
                    if (isRightSideClear()) { // if right is clear
                        r.turn("RIGHT");
                        r.robotMoveForward(1);
                    } else {          // if right is not clear
                        r.turn("RIGHT");
                        if (isFrontClear()) {
                            r.robotMoveForward(1);
                        } else {
                            //System.out.println("[Robot] I may be trapped.");
                        }
                    }
                }
            } else {
                r.turn("LEFT");
                r.robotMoveForward(1);
            }

            totalTime--;
            if (totalTime <= 0) {
                System.out.println("[WallHugging.java] Time has expired.");
                break;
            }

            if (m.isStart(r.getCurrentLocation())) {
                MapStorage.storeMap(m.getMap());
                break;
            }
        }
        return true;
    }
    /*
        Optimization
     */

    private void lookahead() {
        Direction lDirection = r.getCurrentDirection();
        Point lLocation = r.getCurrentLocation();
        int noOfContMovement = 0;
        while (Map.isValid((int) lLocation.getX(), (int) lLocation.getY())) {
            System.out.println("Looking ahead...: "+lLocation.getX()+", "+lLocation.getY()+ lookaheadChecker(lLocation, lDirection));
            if (lookaheadChecker(lLocation, lDirection) && !leftHasWall(lLocation, lDirection)) {
                // Move forward by 1 grid, depending on direction
                lLocation = lMoveRobot(lLocation, lDirection);
                noOfContMovement++;
            } else {
                break;
            }
        }
        noOfContMovement-=1;
        if(noOfContMovement < 1) return;
        System.out.println("Steps: " + noOfContMovement);
        // s.nextLine();

        // Send to arduino continuous movement
        // Divide into 4s
        int threshold = 6;
        if(noOfContMovement > threshold){
            int c = 0;
            for(int j = threshold; j < noOfContMovement; j+=threshold){
                c+=1;
                char continuousMovement = parser(threshold);
                r.sendToArd(continuousMovement + "");
                // Update (simulate) robot location
                for (int i = 0; i < threshold; i++) {
                    Point newRobotPosition = lMoveRobot(r.getCurrentLocation(), r.getCurrentDirection());
                    //DisplayMapGUI.updateRobotPosition((int) newRobotPosition.getX(), (int) newRobotPosition.getY());
                    DisplayMapGUI.refreshGrid(m, newRobotPosition, lDirection);
                    r.updateRobotPosition((int) newRobotPosition.getX(), (int) newRobotPosition.getY());
                }
                r.sendToArd("zEc");
            }
            if(noOfContMovement - (c*threshold) > 0){
                char continuousMovement = parser(noOfContMovement - (c*threshold));
                r.sendToArd(continuousMovement + "");
                // Update (simulate) robot location
                for (int i = 0; i < noOfContMovement - (c*threshold); i++) {
                    Point newRobotPosition = lMoveRobot(r.getCurrentLocation(), r.getCurrentDirection());
                    //DisplayMapGUI.updateRobotPosition((int) newRobotPosition.getX(), (int) newRobotPosition.getY());
                    DisplayMapGUI.refreshGrid(m, newRobotPosition, lDirection);
                    r.updateRobotPosition((int) newRobotPosition.getX(), (int) newRobotPosition.getY());
                }
                r.sendToArd("zEc");
            }
        }else{
            char continuousMovement = parser(noOfContMovement);
            r.sendToArd(continuousMovement + "");
            // Update (simulate) robot location
            for (int i = 0; i < noOfContMovement; i++) {
                Point newRobotPosition = lMoveRobot(r.getCurrentLocation(), r.getCurrentDirection());
                //DisplayMapGUI.updateRobotPosition((int) newRobotPosition.getX(), (int) newRobotPosition.getY());
                DisplayMapGUI.refreshGrid(m, newRobotPosition, lDirection);
                r.updateRobotPosition((int) newRobotPosition.getX(), (int) newRobotPosition.getY());
            }
        }

        //s.nextLine();
    }

    private boolean lookaheadChecker(Point p, Direction d) {
        int x = (int) p.getX();
        int y = (int) p.getY();
        boolean frontOK = false;
        boolean sideShortOK = false;
        boolean sideLongOK = false;
        int i = 0;
        switch (d) {
            case W:
                // Check sensors A, B, C (1 lookahead)
                if(!m.isValid(x - 2, y - 1) || !m.isValid(x - 2, y) || !m.isValid(x - 2, y + 1 )){
                    return false;
                }else if(m.hasObstacle(x - 2, y - 1) || m.hasObstacle(x - 2, y) || m.hasObstacle(x - 2, y + 1) ) {
                    return false;
                }else{
                    frontOK = checkExplored(x - 2, y - 1) && checkExplored(x - 2, y) && checkExplored(x - 2, y + 1);
                }
                // Check sensors D, E (1 lookahead)
                sideShortOK = checkExplored(x, y - 2) && checkExplored(x - 1, y - 2);
                // Check sensors F (up till sensor range lookahead)
                for (i = 0; i < Variables.SENSOR_RANGE_F; i++) {
                    if (m.isValid(x - 1, y + (2 + i))) {
                        if (m.hasObstacle(x - 1, y + (2 + i))) {
                            sideLongOK = true;
                            break;
                        }else{
                            if(m.isExplored(x - 1, y + (2 + i))){

                            }else{
                                return false;
                            }
                        }
                    } else {
                        break;
                    }
                }
                // At this point, i should give us the no. of grids that is cleared
                // Check if i == sensor range (this means all grids that are supposed to be detected, are detected
                // Else, check if the last grid has obtsacle (which is why we end up with i < sensor range.
                if (i == Variables.SENSOR_RANGE_F) {
                    sideLongOK = true;
                }
                return (frontOK && sideShortOK && sideLongOK);
            case A:
                if(!m.isValid(x + 1, y - 2) || !m.isValid(x, y - 2) || !m.isValid(x - 1, y - 2)){
                    return false;
                }else if(m.hasObstacle(x + 1, y - 2) || m.hasObstacle(x, y - 2) || m.hasObstacle(x - 1, y - 2) ) {
                    return false;
                }else{
                    frontOK = checkExplored(x + 1, y - 2) && checkExplored(x, y - 2) && checkExplored(x - 1, y - 2);
                }
                sideShortOK = checkExplored(x + 2, y - 1) && checkExplored(x + 2, y);
                // Check sensors F (up till sensor range lookahead)
                for (i = 0; i < Variables.SENSOR_RANGE_F; i++) {
                    if (m.isValid(x - (2 + i), y - 1)) {
                        if (m.hasObstacle(x - (2 + i), y - 1)) {
                            sideLongOK = true;
                            break;
                        }else{
                            if(m.isExplored(x - (2 + i), y - 1)){

                            }else{
                                return false;
                            }
                        }
                    } else {
                        break;
                    }
                }
                if (i == Variables.SENSOR_RANGE_F) {
                    sideLongOK = true;
                }
                //
                return (frontOK && sideShortOK && sideLongOK);
            case S:
                if(!m.isValid(x + 2, y - 1) || !m.isValid(x + 2, y) || !m.isValid(x + 2, y + 1)){
                    return false;
                }else if(m.hasObstacle(x + 2, y - 1) || m.hasObstacle(x + 2, y) || m.hasObstacle(x + 2, y + 1) ) {
                    return false;
                }else{
                    frontOK = checkExplored(x + 2, y - 1) && checkExplored(x + 2, y) && checkExplored(x + 2, y + 1);
                }
                sideShortOK = checkExplored(x, y + 2) && checkExplored(x + 1, y + 2);

                for (i = 0; i < Variables.SENSOR_RANGE_F; i++) {
                    if (m.isValid(x + 1, y - (2 + i))) {
                        if (m.hasObstacle(x + 1, y - (2 + i))) {
                            sideLongOK = true;
                            break;
                        }else{
                            if(m.isExplored(x + 1, y - (2 + i))){

                            }else{
                                return false;
                            }
                        }
                    } else {
                        break;
                    }
                }
                if (i == Variables.SENSOR_RANGE_F) {
                    sideLongOK = true;
                }
                // end
                return (frontOK && sideShortOK && sideLongOK);
            case D:
                if(!m.isValid(x - 1, y + 2) || !m.isValid(x, y + 2) || !m.isValid(x + 1, y + 2)){
                    return false;
                }else if(m.hasObstacle(x - 1, y + 2) || m.hasObstacle(x, y + 2) || m.hasObstacle(x + 1, y + 2) ) {
                    return false;
                }else{
                    frontOK = checkExplored(x - 1, y + 2) && checkExplored(x, y + 2) && checkExplored(x + 1, y + 2);
                }
                sideShortOK = checkExplored(x - 2, y + 1) && checkExplored(x - 2, y);
                // Check sensors F (up till sensor range lookahead)
                for (i = 0; i < Variables.SENSOR_RANGE_F; i++) {
                    if (m.isValid(x + (2 + i), y + 1)) {
                        if (m.hasObstacle(x + (2 + i), y + 1)) {
                            sideLongOK = true;
                            break;
                        }else{
                            if(m.isExplored(x + (2 + i), y + 1)){

                            }else{
                                return false;
                            }
                        }
                    } else {
                        break;
                    }
                }
                if (i == Variables.SENSOR_RANGE_F) {
                    sideLongOK = true;
                }
                // end

                return (frontOK && sideShortOK && sideLongOK);
        }
        return false;
    }

    private boolean leftHasWall(Point p, Direction d) {
        int x = (int) p.getX();
        int y = (int) p.getY();
        // This is same as isLeftSideClear, jus that we needed to change the point
        switch (d) {
            case W:
                if (m.hasObstacle(x - 1, y - 2) ||
                        m.hasObstacle(x, y - 2) ||
                        m.hasObstacle(x + 1, y - 2)) {
                    return false;
                } else {
                    return true;
                }
            case A:
                if (m.hasObstacle(x + 2, y + 1) ||
                        m.hasObstacle(x + 2, y) ||
                        m.hasObstacle(x + 2, y - 1)) {
                    return false;
                } else {
                    return true;
                }
            case S:
                if (m.hasObstacle(x - 1, y + 2) ||
                        m.hasObstacle(x, y + 2) ||
                        m.hasObstacle(x + 1, y + 2)) {
                    return false;
                } else {
                    return true;
                }
            case D:
                if (m.hasObstacle(x - 2, y - 1) ||
                        m.hasObstacle(x - 2, y) ||
                        m.hasObstacle(x - 2, y + 1)) {
                    return false;
                } else {
                    return true;
                }
        }
        return false;
    }

    // Helper function for lookahead (fill in description later)
    private boolean checkExplored(int x, int y) {
        /*
            Returns true if it is a wall / out of arena
            or if it has obstacles
            or it is explored (no obstacles)
            If it returns false (which means it does not have obstacle nor is it explored),
            then we couldn't use continuous movement as the sensors are still needed
         */
        if (m.isValid(x, y)) {
            return (m.hasObstacle(x, y) || m.isExplored(x, y));
        } else {
            return true;
        }
    }

    // Helper function for lookahead (fill in description later)
    private Point lMoveRobot(Point p, Direction d) {
        int x = (int) p.getX();
        int y = (int) p.getY();
        Point returnP = null;

        switch (d) {
            case W:
                returnP = new Point(x - 1, y);
                break;
            case A:
                returnP = new Point(x, y - 1);
                break;
            case S:
                returnP = new Point(x + 1, y);
                break;
            case D:
                returnP = new Point(x, y + 1);
                break;
        }
        return returnP;
    }

    // Parse discrete movements to continuous
    // Number of occurrences of W
    private char parser(int i) {
        switch (i) {
            case 1:
                return 'W';
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


    // Check if area in front of robot has obstacles
    // Returns true if area in front has obstacles
    private boolean isFrontClear() {
        // Checks if the front of the robot is clear.
        // Needs direction and position of robot
        // Checks only 1 grid!
        //System.out.println(">> Current robot location: "+r.getCurrentLocation().x + " " + r.getCurrentLocation().y);
        //System.out.println(">> Current robot direction: "+r.getCurrentDirection());
        switch (r.getCurrentDirection()) {
            case W:
                if (m.hasObstacle(r.getCurrentLocation().x - 2, r.getCurrentLocation().y - 1) ||
                        m.hasObstacle(r.getCurrentLocation().x - 2, r.getCurrentLocation().y) ||
                        m.hasObstacle(r.getCurrentLocation().x - 2, r.getCurrentLocation().y + 1)) {
                    return false;
                } else {
                    return true;
                }
            case A:
                if (m.hasObstacle(r.getCurrentLocation().x + 1, r.getCurrentLocation().y - 2) ||
                        m.hasObstacle(r.getCurrentLocation().x, r.getCurrentLocation().y - 2) ||
                        m.hasObstacle(r.getCurrentLocation().x - 1, r.getCurrentLocation().y - 2)) {
                    return false;
                } else {
                    return true;
                }
            case S:
                if (m.hasObstacle(r.getCurrentLocation().x + 2, r.getCurrentLocation().y + 1) ||
                        m.hasObstacle(r.getCurrentLocation().x + 2, r.getCurrentLocation().y) ||
                        m.hasObstacle(r.getCurrentLocation().x + 2, r.getCurrentLocation().y - 1)) {
                    return false;
                } else {
                    return true;
                }
            case D:
                if (m.hasObstacle(r.getCurrentLocation().x - 1, r.getCurrentLocation().y + 2) ||
                        m.hasObstacle(r.getCurrentLocation().x, r.getCurrentLocation().y + 2) ||
                        m.hasObstacle(r.getCurrentLocation().x + 1, r.getCurrentLocation().y + 2)) {
                    return false;
                } else {
                    return true;
                }
        }
        return false;
    }

    // Check if area at the left side got obstacle
    // Returns true if left side is clear
    private boolean isLeftSideClear() {
        switch (r.getCurrentDirection()) {
            case W:
                if (m.hasObstacle(r.getCurrentLocation().x - 1, r.getCurrentLocation().y - 2) ||
                        m.hasObstacle(r.getCurrentLocation().x, r.getCurrentLocation().y - 2) ||
                        m.hasObstacle(r.getCurrentLocation().x + 1, r.getCurrentLocation().y - 2)) {
                    return false;
                } else {
                    return true;
                }
            case A:
                if (m.hasObstacle(r.getCurrentLocation().x + 2, r.getCurrentLocation().y + 1) ||
                        m.hasObstacle(r.getCurrentLocation().x + 2, r.getCurrentLocation().y) ||
                        m.hasObstacle(r.getCurrentLocation().x + 2, r.getCurrentLocation().y - 1)) {
                    return false;
                } else {
                    return true;
                }
            case S:
                if (m.hasObstacle(r.getCurrentLocation().x - 1, r.getCurrentLocation().y + 2) ||
                        m.hasObstacle(r.getCurrentLocation().x, r.getCurrentLocation().y + 2) ||
                        m.hasObstacle(r.getCurrentLocation().x + 1, r.getCurrentLocation().y + 2)) {
                    return false;
                } else {
                    return true;
                }
            case D:
                if (m.hasObstacle(r.getCurrentLocation().x - 2, r.getCurrentLocation().y - 1) ||
                        m.hasObstacle(r.getCurrentLocation().x - 2, r.getCurrentLocation().y) ||
                        m.hasObstacle(r.getCurrentLocation().x - 2, r.getCurrentLocation().y + 1)) {
                    return false;
                } else {
                    return true;
                }
        }
        return false;
    }

    // Check if area at the right side got obstacles
    // Returns true if right side is clear
    private boolean isRightSideClear() {
        switch (r.getCurrentDirection()) {
            case W:
                if (m.hasObstacle(r.getCurrentLocation().x - 1, r.getCurrentLocation().y + 2) ||
                        m.hasObstacle(r.getCurrentLocation().x, r.getCurrentLocation().y + 2) ||
                        m.hasObstacle(r.getCurrentLocation().x + 1, r.getCurrentLocation().y + 2)) {
                    return false;
                } else {
                    return true;
                }
            case A:
                if (m.hasObstacle(r.getCurrentLocation().x - 2, r.getCurrentLocation().y - 1) ||
                        m.hasObstacle(r.getCurrentLocation().x - 2, r.getCurrentLocation().y) ||
                        m.hasObstacle(r.getCurrentLocation().x - 2, r.getCurrentLocation().y + 1)) {
                    return false;
                } else {
                    return true;
                }
            case S:
                if (m.hasObstacle(r.getCurrentLocation().x + 1, r.getCurrentLocation().y - 2) ||
                        m.hasObstacle(r.getCurrentLocation().x, r.getCurrentLocation().y - 2) ||
                        m.hasObstacle(r.getCurrentLocation().x - 1, r.getCurrentLocation().y - 2)) {
                    return false;
                } else {
                    return true;
                }
            case D:
                if (m.hasObstacle(r.getCurrentLocation().x + 2, r.getCurrentLocation().y + 1) ||
                        m.hasObstacle(r.getCurrentLocation().x + 2, r.getCurrentLocation().y) ||
                        m.hasObstacle(r.getCurrentLocation().x + 2, r.getCurrentLocation().y - 1)) {
                    return false;
                } else {
                    return true;
                }
        }
        return false;
    }

    public static float getExplorationPercentage() {
        return explorationPercentage;
    }

    private void checkUnexploredGrids() {
        if (robotStop)
            return;
        if (Variables.debugWH) System.out.println("Exploration percentage: " + r.getMap().getPercentageExplored());

        // Go to the first shit
        Grid toGoGrid = setUpUnexploredGrids();
        while (setUpUnexploredGrids() != null) {
            if (Variables.debugWH) System.out.println("[WallHugging.java] Going to: " + toGoGrid.getPosition());
            if (r.getCurrentLocation() == toGoGrid.getPosition()) {

            } else {
                Algorithm a = new Algorithm(r, toGoGrid.getPosition(), true, true);
            }
            // Pause to check.
            toGoGrid = setUpUnexploredGrids();
            //s.nextLine();
        }

        // Update
    }

    private Grid setUpUnexploredGrids() {
        // Identify unexplored grids
        ArrayList<Grid> unexplored = r.getMap().getUnexploredGrids();
        if (Variables.debugWH) System.out.println("[WallHugging.java] Unexplored grids: " + unexplored.size());

        // Identify available grids the robot can traverse over
        ArrayList<Grid> availablePaths = r.getMap().getAvailablePaths();
        if (Variables.debugWH) System.out.println("[WallHugging.java] Available grids: " + availablePaths.size());
        for (int i = 0; i < availablePaths.size(); i++) {
            //DisplayMapGUI.showExplorationAvailableGrids(availablePaths.get(i).getPosition().x, availablePaths.get(i).getPosition().y);
        }

        // Intersection between available grids and within unexplored... stuff
        ArrayList<Grid> toGo = new ArrayList<>();

        ArrayList<ExplorationHeuristic> explorationHeuristics = new ArrayList<>();

        // Identify availabe paths to unexplored grids
        // For each unexplored grid, slowly expand out in 4 directions (N/S/E/W)
        for (int i = 0; i < unexplored.size(); i++) {
            // DisplayMapGUI.highlightGrid(unexplored.get(i), Color.magenta);
            // How much further the robot can be from the unexplored grids depends on the sensor range
            // Check RIGHT
            int range = 0;
            while (range <= Variables.SENSOR_FRONT_RANGE) {
                // If it does not have obstacles and if it is within traversable paths, mark it
                // If it is not
                if (m.isValid(unexplored.get(i).getPosition().x, unexplored.get(i).getPosition().y + 1 + range)) {
                    if (m.hasObstacle(unexplored.get(i).getPosition().x, unexplored.get(i).getPosition().y + 1 + range)) {
                        break;
                    }
                    if (m.isExplored(unexplored.get(i).getPosition().x, unexplored.get(i).getPosition().y + 1 + range)
                            && availablePaths.contains(new Grid(unexplored.get(i).getPosition().x, unexplored.get(i).getPosition().y + 1 + range))) {
                        if (!toGo.contains(new Grid(unexplored.get(i).getPosition().x, unexplored.get(i).getPosition().y + 1 + range))) {
                            toGo.add(new Grid(unexplored.get(i).getPosition().x, unexplored.get(i).getPosition().y + 1 + range));
                            //DisplayMapGUI.showSecondaryExploration(unexplored.get(i).getPosition().x, unexplored.get(i).getPosition().y+1+range);
                        }
                    }
                }
                range++;
            }
            // Check south
            range = 0;
            while (range <= Variables.SENSOR_FRONT_RANGE) {
                // Only add the one closest to the obstacle that is explorable
                if (m.isValid(unexplored.get(i).getPosition().x + 1 + range, unexplored.get(i).getPosition().y)) {
                    if (m.hasObstacle(unexplored.get(i).getPosition().x + 1 + range, unexplored.get(i).getPosition().y)) {
                        break;
                    }
                    if (m.isExplored(unexplored.get(i).getPosition().x + 1 + range, unexplored.get(i).getPosition().y)
                            && availablePaths.contains(new Grid(unexplored.get(i).getPosition().x + 1 + range, unexplored.get(i).getPosition().y))) {
                        if (!toGo.contains(new Grid(unexplored.get(i).getPosition().x + 1 + range, unexplored.get(i).getPosition().y))) {
                            //DisplayMapGUI.showSecondaryExploration(unexplored.get(i).getPosition().x+1+range, unexplored.get(i).getPosition().y);
                            toGo.add(new Grid(unexplored.get(i).getPosition().x + 1 + range, unexplored.get(i).getPosition().y));
                        }
                    }
                }
                range++;
            }
            // Check East
            range = 0;
            while (range <= Variables.SENSOR_FRONT_RANGE) {
                // Only add the one closest to the obstacle that is explorable
                if (m.isValid(unexplored.get(i).getPosition().x, unexplored.get(i).getPosition().y - 1 - range)) {
                    if (m.hasObstacle(unexplored.get(i).getPosition().x, unexplored.get(i).getPosition().y - 1 - range)) {
                        break;
                    }
                    if (m.isExplored(unexplored.get(i).getPosition().x, unexplored.get(i).getPosition().y - 1 - range)
                            && availablePaths.contains(new Grid(unexplored.get(i).getPosition().x, unexplored.get(i).getPosition().y - 1 - range))) {
                        if (!toGo.contains(new Grid(unexplored.get(i).getPosition().x, unexplored.get(i).getPosition().y - 1 - range))) {
                            //DisplayMapGUI.showSecondaryExploration(unexplored.get(i).getPosition().x, unexplored.get(i).getPosition().y-1-range);
                            toGo.add(new Grid(unexplored.get(i).getPosition().x, unexplored.get(i).getPosition().y - 1 - range));
                        }
                    }
                }
                range++;
            }
            // Check West
            range = 0;
            while (range <= Variables.SENSOR_FRONT_RANGE) {
                // Only add the one closest to the obstacle that is explorable
                if (m.isValid(unexplored.get(i).getPosition().x - 1 - range, unexplored.get(i).getPosition().y)) {
                    if (m.hasObstacle(unexplored.get(i).getPosition().x - 1 - range, unexplored.get(i).getPosition().y)) {
                        break;
                    }
                    if (m.isExplored(unexplored.get(i).getPosition().x - 1 - range, unexplored.get(i).getPosition().y)
                            && availablePaths.contains(new Grid(unexplored.get(i).getPosition().x - 1 - range, unexplored.get(i).getPosition().y))) {
                        if (!toGo.contains(new Grid(unexplored.get(i).getPosition().x - 1 - range, unexplored.get(i).getPosition().y))) {
                            //DisplayMapGUI.showSecondaryExploration(unexplored.get(i).getPosition().x-1-range, unexplored.get(i).getPosition().y);
                            toGo.add(new Grid(unexplored.get(i).getPosition().x - 1 - range, unexplored.get(i).getPosition().y));
                        }
                    }
                }
                range++;
            }
        }

        if (Variables.debugWH) System.out.println("[WallHugging.java] toGo size: " + toGo.size());
        // At this step, the intersection should be established.

        // Remove current location from togo
        toGo.remove(new Grid(r.getCurrentLocation().x, r.getCurrentLocation().y));

        // Calculate heuristic. Get the grid that is closest to current grid
        Grid currentLocationGrid = new Grid((int) r.getCurrentLocation().getX(), (int) r.getCurrentLocation().getY());
        for (Grid g : toGo) {
            explorationHeuristics.add(new ExplorationHeuristic(g, currentLocationGrid, r.getCurrentDirection()));
        }

        for (ExplorationHeuristic e : explorationHeuristics) {
            e.updateHeuristics(e.getGrid(), new Grid((int) r.getCurrentLocation().getX(), (int) r.getCurrentLocation().getY()), r.getCurrentDirection());
        }

        if (explorationHeuristics.size() != 0) {
            Collections.sort(explorationHeuristics);
            if (explorationHeuristics.get(0).getPosition() == r.getCurrentLocation()) {
                if (Variables.debugWH) System.out.println("[WallHugging.java] Same spot!");
            }
            return explorationHeuristics.get(0).getGrid();
        } else {
            return null;
        }

    }
}
