package Controller;


import Model.*;
import Model.Map;
import Model.Robot;
import View.DisplayMapGUI;

import java.awt.*;
import java.util.*;

/**
 * Fastest path algorithm that utilizes the number of turns as heuristics
 * This is used during exploration (to cover unexplored areas)
 * as well as during fastest path itself.
 */

public class Dijkstra {

    /**
     * For easier access of map functions. This will be a copy of robot's internal map.
     */
    private Map m;

    /**
     * The list of grids that can be traversed over by the robot
     */
    private ArrayList<Grid> currentAvailableGrids;

    /**
     * A priority queue sorted by the number of turns required
     */
    private PriorityQueue<Grid> unvisitedQueue;


    /**
     * @param m                     Takes in the current state of the map of robot
     */
    public Dijkstra(Map m) {
        this.m = m;
        currentAvailableGrids = new ArrayList<>();
        unvisitedQueue = new PriorityQueue<Grid>();

        // Initialize the set of traversable grids
        initializeAvailableGrids();
    }


    /**
     * Finds a path with least turns from start to target
     * @param start                 The starting point
     * @param startingDirection     The starting direction
     * @param target                The target point
     * @return                      Returns a string of discrete movements as fastest path
     */
    public String start(Point start, Direction startingDirection, Point target){
        Grid currentGrid;
        HeuristicResult temp;
        Stack<Grid> pathToTake;

        if(start.equals(target)){
            System.out.println("Reached!");
            return "";
        }

        // Assign the number of turns for all available grids to 1000 (MAX)
        for(Grid g: currentAvailableGrids){
            g.setNoOfTurns(500);
        }

        // Assign number of turns at current grid to be 0
        currentAvailableGrids.get(currentAvailableGrids.indexOf(new Grid(start.x, start.y))).setNoOfTurns(0);

        // Update current grid's location
        currentAvailableGrids.get(currentAvailableGrids.indexOf(new Grid(start.x, start.y))).setDirection(startingDirection);

        // Add all nodes to priority queue
        unvisitedQueue.addAll(currentAvailableGrids);

        // Check priority queue:
        for(Grid u: unvisitedQueue){
            //System.out.println(u.getPosition().x+", "+u.getPosition().y+" -> "+u.getNoOfTurns());
        }

        // While we have not visited all nodes...
        while(!unvisitedQueue.isEmpty()){
            // Choose and remove the node with least no. of turns from the queue
            currentGrid = unvisitedQueue.remove();

            // If we are at target, break
            if(currentGrid.equals(new Grid(target.x, target.y))) break;

            // For every available neighbour
            for(Grid neighbour: getNeighbours(currentGrid)){
                // Calculate no. of turns required to travel from current grid, to neighbour grid
                temp = noOfTurns(currentGrid, neighbour);
                // If a better path is found from current grid to neighbour grid, update parents
                if(temp.getNoOfTurns() + currentGrid.getNoOfTurns() < neighbour.getNoOfTurns()){
                    //neighbour.setNoOfTurns(temp.getNoOfTurns() + currentGrid.getNoOfTurns());
                    neighbour.setNoOfTurns(temp.getNoOfTurns()*10 + 1 + currentGrid.getNoOfTurns());
                    neighbour.setParent(currentGrid);
                    neighbour.setDirection(temp.getEndDirection());

                    // Remove and reinsert element
                    unvisitedQueue.remove(neighbour);
                    unvisitedQueue.add(neighbour);
                }
            }
        }

        System.out.println("Processed all nodes. Getting path... :");
        DisplayMapGUI.highlightGrid(new Grid((int) target.getX(), (int) target.getY()), Color.red);
        pathToTake = getPath(start, target);
        String pathX = (getMovements(pathToTake));
        System.out.println(pathX);
        //Scanner s = new Scanner(System.in);
        //s.nextLine();

        // Parse the path into a string of movements
        return (pathX);
    }

    /**
     * Returns the number of turns and ending direction for the robot to traverse from current to target grid
     * @param current       The current grid we are processing
     * @param target        The target grid we want to go
     * @return              Returns a HeuristicResult object which contains no. of turns and end direction
     */
    private HeuristicResult noOfTurns(Grid current, Grid target){
        // Get current direction
        Direction cDirec = current.getDirection();
        // Number of turns
        int noOfTurns = 0;
        Direction endDirec = Direction.W;

        // Start search in current direction first, then clockwise
        switch(cDirec){
            case W:
                // Move forward
                if(current.getPosition().x - 1 == target.getPosition().x && current.getPosition().y == target.getPosition().y){
                    noOfTurns = 0;
                    endDirec = Direction.W;
                }else{
                    if(current.getPosition().x == target.getPosition().x && current.getPosition().y - 1 == target.getPosition().y){
                        // Turn left
                        noOfTurns = 1;
                        endDirec = Direction.A;
                    }else if(current.getPosition().x == target.getPosition().x && current.getPosition().y + 1 == target.getPosition().y){
                        // Turn right
                        noOfTurns = 1;
                        endDirec = Direction.D;
                    }else{
                        // Reverse
                        noOfTurns = 2;
                        endDirec = Direction.S;
                    }
                }
                break;
            case A:
                // Move forward
                if(current.getPosition().x == target.getPosition().x && current.getPosition().y - 1 == target.getPosition().y){
                    noOfTurns = 0;
                    endDirec = Direction.A;
                }else{
                    if(current.getPosition().x + 1 == target.getPosition().x && current.getPosition().y == target.getPosition().y){
                        // Turn left
                        noOfTurns = 1;
                        endDirec = Direction.S;
                    }else if(current.getPosition().x - 1 == target.getPosition().x && current.getPosition().y == target.getPosition().y){
                        // Turn right
                        noOfTurns = 1;
                        endDirec = Direction.W;
                    }else{
                        // Reverse
                        noOfTurns = 2;
                        endDirec = Direction.D;
                    }
                }
                break;
            case D:
                // Move forward
                if(current.getPosition().x == target.getPosition().x && current.getPosition().y + 1 == target.getPosition().y){
                    noOfTurns = 0;
                    endDirec = Direction.D;
                }else{
                    if(current.getPosition().x - 1 == target.getPosition().x && current.getPosition().y == target.getPosition().y){
                        // Turn left
                        noOfTurns = 1;
                        endDirec = Direction.W;
                    }else if(current.getPosition().x + 1 == target.getPosition().x && current.getPosition().y == target.getPosition().y){
                        // Turn right
                        noOfTurns = 1;
                        endDirec = Direction.S;
                    }else{
                        // Reverse
                        noOfTurns = 2;
                        endDirec = Direction.A;
                    }
                }
                break;
            case S:
                // Move forward
                if(current.getPosition().x + 1== target.getPosition().x && current.getPosition().y == target.getPosition().y){
                    noOfTurns = 0;
                    endDirec = Direction.S;
                }else{
                    if(current.getPosition().x  == target.getPosition().x && current.getPosition().y + 1 == target.getPosition().y){
                        // Turn left
                        noOfTurns = 1;
                        endDirec = Direction.D;
                    }else if(current.getPosition().x == target.getPosition().x && current.getPosition().y - 1 == target.getPosition().y){
                        // Turn right
                        noOfTurns = 1;
                        endDirec = Direction.A;
                    }else{
                        // Reverse
                        noOfTurns = 2;
                        endDirec = Direction.W;
                    }
                }
                break;
        }
        return new HeuristicResult(noOfTurns, endDirec);
    }

    /**
     * Retrieves the grids traversed to go from start to target
     * @param start     The starting grid
     * @param target    The target grid we want to move to
     * @return          A stack of grids that the robot must move over
     */
    private Stack<Grid> getPath(Point start, Point target){
        Stack<Grid> toRet = new Stack<>();
        // Get target grid
        Grid targetGrid = currentAvailableGrids.get(currentAvailableGrids.indexOf(new Grid(target.x, target.y)));
        Variables.waypointDirection = targetGrid.getDirection();
        // Get starting grid
        Grid startGrid = currentAvailableGrids.get(currentAvailableGrids.indexOf(new Grid(start.x, start.y)));
        Grid tempGrid = targetGrid;
        while(!tempGrid.equals(startGrid)){
            toRet.push(tempGrid);
//            DisplayMapGUI.highlightGrid(tempGrid, Color.GREEN);
            tempGrid = tempGrid.getParent();
        }
        toRet.push(tempGrid);
        return toRet;
    }

    /**
     * Parses a set of grids into discrete movements for the robot
     * @param path      The set of grids traversed over by the algorithm
     * @return          Returns the a set of movements for the robot in compliance with Arduino requirements
     */
    private String getMovements(Stack path){
        String movements = "";
        Grid current, target;

        // Get current grid
        current = (Grid) path.pop();

        while(!path.empty()){
            // Check what movement it needs to go from current grid, to the next grid
            if(path.peek() != null){
                target = (Grid) path.peek();
                if(current.getPosition().x - 1 == target.getPosition().x && current.getPosition().y == target.getPosition().y){
                    // Needs to move forward
                    switch(current.getDirection()){
                        case W:
                            movements+="W";
                            break;
                        case A:
                            movements+="D";
                            movements+="W";
                            break;
                        case S:
                            movements+="D";
                            movements+="D";
                            movements+="W";
                            break;
                        case D:
                            movements+="A";
                            movements+="W";
                            break;
                    }
                }else if(current.getPosition().x + 1 == target.getPosition().x && current.getPosition().y == target.getPosition().y){
                    // Needs to move backwards
                    switch(current.getDirection()){
                        case W:
                            movements+="D";
                            movements+="D";
                            movements+="W";
                            break;
                        case A:
                            movements+="A";
                            movements+="W";
                            break;
                        case S:
                            movements+="W";
                            break;
                        case D:
                            movements+="D";
                            movements+="W";
                            break;
                    }
                }else  if(current.getPosition().x == target.getPosition().x && current.getPosition().y -1 == target.getPosition().y){
                    // Needs to move left
                    switch(current.getDirection()){
                        case W:
                            movements+="A";
                            movements+="W";
                            break;
                        case A:
                            movements+="W";
                            break;
                        case S:
                            movements+="D";
                            movements+="W";
                            break;
                        case D:
                            movements+="D";
                            movements+="D";
                            movements+="W";
                            break;
                    }
                }else{
                    // Needs to move right
                    switch(current.getDirection()){
                        case W:
                            movements+="D";
                            movements+="W";
                            break;
                        case A:
                            movements+="D";
                            movements+="D";
                            movements+="W";
                            break;
                        case S:
                            movements+="A";
                            movements+="W";
                            break;
                        case D:
                            movements+="W";
                            break;
                    }
                }
            }
            current = (Grid) path.pop();
        }

        return movements;
    }


    /**
     * Retrieves the neighbouring grids that have not to be processed (still inside queue)
     *
     * @param current       The current grid to search neighbours for
     * @return              Returns the set of neighbours that have not been processed
     */
    private ArrayList<Grid> getNeighbours(Grid current){
        ArrayList<Grid> toReturn = new ArrayList<>();
        // Get set of available grids around the current grid
        if(currentAvailableGrids.contains(new Grid(current.getPosition().x-1, current.getPosition().y))){
            toReturn.add(currentAvailableGrids.get(currentAvailableGrids.indexOf(new Grid(current.getPosition().x-1, current.getPosition().y))));
        }
        if(currentAvailableGrids.contains(new Grid(current.getPosition().x+1, current.getPosition().y))){
            toReturn.add(currentAvailableGrids.get(currentAvailableGrids.indexOf(new Grid(current.getPosition().x+1, current.getPosition().y))));
        }
        if(currentAvailableGrids.contains(new Grid(current.getPosition().x, current.getPosition().y-1))){
            toReturn.add(currentAvailableGrids.get(currentAvailableGrids.indexOf(new Grid(current.getPosition().x, current.getPosition().y-1))));
        }
        if(currentAvailableGrids.contains(new Grid(current.getPosition().x, current.getPosition().y+1))){
            toReturn.add(currentAvailableGrids.get(currentAvailableGrids.indexOf(new Grid(current.getPosition().x, current.getPosition().y+1))));
        }

        // Get set of available grids that are still inside the queue (have not been processed)
        Iterator<Grid> iter = toReturn.iterator();
        while(iter.hasNext()){
            Grid g = iter.next();
            if(!unvisitedQueue.contains(g)){
                iter.remove();
            }
        }

        return toReturn;
    }

    /**
     * Initializes the set of grids that are traversable by the robot.
     * This is characterized as: a 3 x 3 set of grid that is (1) explored and (2) has no obstacles.
     * The set of grid is then added to currentAvailableGrids.
     */
    private void initializeAvailableGrids() {
        for (int i = 1; i < 19; i++) {
            for (int j = 1; j < 14; j++) {
                if (!m.hasObstacle(i, j) && m.isExplored(i, j)) {
                    if (!m.hasObstacle(i - 1, j) && m.isExplored(i - 1, j)
                            && !m.hasObstacle(i + 1, j) && m.isExplored(i + 1, j)
                            && !m.hasObstacle(i, j - 1) && m.isExplored(i, j - 1)
                            && !m.hasObstacle(i, j + 1) && m.isExplored(i, j + 1)
                            && !m.hasObstacle(i - 1, j - 1) && m.isExplored(i - 1, j - 1)
                            && !m.hasObstacle(i - 1, j + 1) && m.isExplored(i - 1, j + 1)
                            && !m.hasObstacle(i + 1, j - 1) && m.isExplored(i + 1, j - 1)
                            && !m.hasObstacle(i + 1, j + 1) && m.isExplored(i + 1, j + 1)) {
                        currentAvailableGrids.add(new Grid(i, j));
                    }
                }
            }
        }
    }
}
