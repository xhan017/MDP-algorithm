package Model;

import View.DisplayMapGUI;

import java.awt.*;
import java.util.ArrayList;

public class Map {
    char[][] fixedMapArray;
    int numberOfExploredGrid;
    final Point goalPoint = new Point(1, 13);
    final Point startPoint = new Point(18, 1);
    Point waypoint = null;
    double numberOfExploredGrid2=0;

    /*
    The map is displayed in the following manner:
    1. Explored grids are displayed as 0
    2. Unexplored grids are displayed as an empty space
    3. Grids with obstacles are displayed with character 'x'
     */

    public Map(){
        fixedMapArray = new char[20][15];
        setMapArray("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        numberOfExploredGrid = 0;
        waypoint = null;
    }

    public Map(String s){
        fixedMapArray = new char[20][15];
        numberOfExploredGrid=0;
        setMapArray(s);
    }

    public boolean isGoal(Point p){
        if(p.x == goalPoint.x && p.y == goalPoint.y){
            //return true;
            System.out.println("Goal found!");
            return false;
        }
        return false;
    }

    public void setWaypoint(Point p){
        this.waypoint = p;
    }

    public boolean isStart(Point p){
        if(p.x == startPoint.x && p.y == startPoint.y){
            System.out.println("Back at starting!");
            return true;
        }
        return false;
    }

    // create the map array
    public void setMapArray(String s){
        int pos = 0, j = 19, k = 0;
        char[] mapArray = s.toCharArray();
        for (int i = 2; i < mapArray.length-2 ; i++){
            pos++;
            fixedMapArray[j][k] = mapArray[i];
            if(mapArray[i] == '1') numberOfExploredGrid++;
            k++;
            if(pos % 15 == 0){
                j--;
                k = 0;
            }
        }
    }

    public void populateObstacles(String s){
        // Decide how much of the string to splice based on the number of explored cells
        // As long as the number of explored grid is not divisible by 8, increase
        // After that, we should be able to figure out the no. of padding.
        int numberOfPadding = 0, numberOfExploredGrid_backup = numberOfExploredGrid;
        // test padding
        // numberOfExploredGrid = 209;
        System.out.println("[console] Number of explored grid: "+numberOfExploredGrid);
        while(numberOfExploredGrid%8 != 0){
            numberOfExploredGrid++;
            numberOfPadding++;
        }
        System.out.println("[console] Number of padding required: "+numberOfPadding);

        // Read the obstacle file and convert to character array
        char[] mapArray = Controller.MapStorage.readMap(2, Variables.INPUT_FILE_NAME).toCharArray();
        int pos = 0;

        for(int i = 19; i >= 0; i--){
            for(int j = 0; j < 15; j++){
                // if the current grid is explored, check if it has an obstacle
                // update the grid to 'x' for obstacle and '0' for no obstacle
                if(fixedMapArray[i][j] == '1'){
                    if(mapArray[numberOfPadding+pos] == '1'){
                        fixedMapArray[i][j] = 'x';
                        //DisplayMapGUI.updateObstacles(i, j);
                    }else{
                        fixedMapArray[i][j] = '0';
                    }
                    pos++;
                }else{
                    fixedMapArray[i][j] = '0';
                }
            }
        }

        System.out.println("[Map.java] Done creating obstacle map! ");
    }


    // other methods
    public void markExplored(int x, int y){
        // System.out.println(x +", "+y);
        if(fixedMapArray[x][y]!='1'){
            numberOfExploredGrid2++;
        }
        fixedMapArray[x][y] = '1';
    }

    public boolean isExplored(int x, int y){
        if (fixedMapArray[x][y] == '1') return true;
        return false;
    }

    public void markHasObstacles(int x, int y){
        if(fixedMapArray[x][y]!='x'){
            numberOfExploredGrid2++;
        }
        fixedMapArray[x][y] = 'x';
    }

    public boolean hasObstacle(int x, int y){
        if(!isValid(x, y)) return true;
        if(fixedMapArray[x][y] == 'x') return true;
        return false;
    }

    public int getNumberOfExploredGrid(){
        return numberOfExploredGrid;
    }

    public char[][] getMap(){
        return fixedMapArray;
    }

    public static boolean isValid(int x, int y){
        if((x >= 0 && x <= 19) && (y >= 0 && y <= 14)){
            return true;
        }
        //System.out.println("[Map] Invalid: "+x + " "+y);
        return false;
    }

    public ArrayList<Grid> getAvailablePaths(){
        ArrayList<Grid> toReturn = new ArrayList<>();
        for(int i = 1; i < 19; i++){
            for(int j = 1; j < 14; j++){
                // if current grid does not have obstacles & is explored, check if the robot can 'stand' on it
                if(!hasObstacle(i,j) && isExplored(i, j)){
                    if(!hasObstacle(i-1, j)  && isExplored(i-1, j)
                            && !hasObstacle(i+1, j) && isExplored(i+1, j)
                            && !hasObstacle(i, j-1) && isExplored(i, j-1)
                            && !hasObstacle(i, j+1) && isExplored(i, j+1)
                            && !hasObstacle(i-1, j-1) && isExplored(i-1, j-1)
                            && !hasObstacle(i-1, j+1) && isExplored(i-1, j+1)
                            && !hasObstacle(i+1, j-1) && isExplored(i+1, j-1)
                            && !hasObstacle(i+1, j+1) && isExplored(i+1, j+1)){
                        toReturn.add(new Grid(i, j));
                    }
                }
            }
        }
        return toReturn;
    }

    public double getPercentageExplored(){
        return numberOfExploredGrid2/300.0 * 100.0;
    }

    public ArrayList<Grid> getUnexploredGrids(){
        ArrayList<Grid> toReturn = new ArrayList<Grid>();
        for(int i = 0; i < 20; i++){
            for(int j = 0; j < 15; j++){
                if(fixedMapArray[i][j] == '0'){
                    toReturn.add(new Grid(i, j));
                }
            }
        }
        return toReturn;
    }

}
