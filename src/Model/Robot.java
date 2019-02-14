package Model;
import Controller.ImageRecce;
import Controller.MapStorage;
import View.DisplayMapGUI;
import Controller.TcpService;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Robot {
    Point currentLoc, startLoc = new Point(18,1);
    Direction currDirec;
    Direction prevDirec;
    Map m;
    Sensor s;
    TcpService tcp;
    int a, b, c, d, e, f;
    ArrayList<Grid> exploredGrids;

    /*
      A robot occupies 6 physical grid
      The center of the robot is denoted by point c
      + - - - - - +
      |   |   |   |
      |   | c |   |
      |   |   |   |
      + - - - - - +
     */

    // Initialize position of robot
    public Robot (Point c, Direction direc, TcpService tcp){
        if(tcp == null){
            System.out.println("[Robot.java] Unable to establish connection through TCP.");
            Variables.SIMULATION = true;
        }else{
            this.tcp = tcp;
            tcp.sendMessage("ard; X");
            // tcp.sendMessage("ard; M");
        }
        exploredGrids = new ArrayList<>();
        this.currentLoc = c;
        this.currDirec = direc;
        MapStorage.currentDirection = direc;
        MapStorage.currentLocation = c;
        this.m = new Map();
        this.s = new Sensor(tcp);
        checkSensors();
        updateRobotPosition(currentLoc.x, currentLoc.y);
        DisplayMapGUI.refreshGrid(m, currentLoc, currDirec);
        System.out.println("[Robot.java] Robot initialized! numberOfExploredGrid: "+m.getNumberOfExploredGrid());
    }

    // Update robot position
    public void robotMoveForward(int steps){
        // If PREVENT_SEND is true, do not send!
        // We do not resend movement to RPI during fastest path!
        if(tcp != null && !Variables.PREVENT_SEND){
            System.out.println("[Robot.java] Send: and; W");
            sendToArd("W");
        }else{
            System.out.println("[Robot.java] Fake send: ard; W");
            // System.out.println("[Robot] Fake send: and; "+mdf);
        }

        switch(currDirec){
            case W : // If robot is facing north (W)
                currentLoc.x-=steps;
                break;
            case A : // If robot is facing west (A)
                currentLoc.y-=steps;
                break;
            case S: // If robot is facing south (S)
                currentLoc.x+=steps;
                break;
            case D: // If robot is facing east (D)
                currentLoc.y+=steps;
                break;
        }

        //System.out.println("[console] Moved forward. New position: "+currentLoc);
        updateRobotDirection(currDirec);
        updateRobotPosition(currentLoc.x, currentLoc.y);
        if(!Variables.noUpdateUI){
            DisplayMapGUI.refreshGrid(m, currentLoc, currDirec);
        }
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Point getStartLoc(){
        return startLoc;
    }

    public Point getCurrentLocation(){
        return currentLoc;
    }

    public Direction getCurrentDirection(){
        return currDirec;
    }

    public Map getMap(){
        return m;
    }

    public void turn(String s){
        if(s == "E"){
            tcp.sendMessage("ard; E");
        }else if(s == "Q"){
            tcp.sendMessage("ard; Q");
        }
        if(s == "LEFT"){
            sendToArd("A");
            switch(currDirec){
                case W:
                    updateRobotDirection(Direction.A);
                    break;
                case A:
                    updateRobotDirection(Direction.S);
                    break;
                case S:
                    updateRobotDirection(Direction.D);
                    break;
                case D:
                    updateRobotDirection(Direction.W);
                    break;
            }
        }else if(s == "RIGHT"){
            sendToArd("D");
            switch(currDirec){
                case W:
                    updateRobotDirection(Direction.D);
                    break;
                case A:
                    updateRobotDirection(Direction.W);
                    break;
                case S:
                    updateRobotDirection(Direction.A);
                    break;
                case D:
                    updateRobotDirection(Direction.S);
                    break;
            }
        }
    }

    public void sendToArd(String d){
        // If PREVENT_SEND is true, do not send!
        if(tcp!=null && !Variables.PREVENT_SEND){
            tcp.sendMessage("ard; "+d);
        }else{
            if(!Variables.PREVENT_SEND){
                System.out.println("[Robot.java] FAKE SEND TO ROBOTO: "+d);
            }
        }
    }

    public void updateRobotPosition(int x, int y){
        if(Variables.SIMULATION){
            try{
                TimeUnit.MILLISECONDS.sleep(1000/Variables.stepsPerSec);
            }catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }

        // Create and mark those grids as traversed

        markTraversed(currentLoc.x, currentLoc.y);
        markTraversed(currentLoc.x, currentLoc.y+1);
        markTraversed(currentLoc.x, currentLoc.y-1);
        markTraversed(currentLoc.x+1, currentLoc.y);
        markTraversed(currentLoc.x-1, currentLoc.y);
        markTraversed(currentLoc.x-1, currentLoc.y+1);
        markTraversed(currentLoc.x-1, currentLoc.y-1);
        markTraversed(currentLoc.x+1, currentLoc.y+1);
        markTraversed(currentLoc.x+1, currentLoc.y-1);
        m.markExplored(currentLoc.x, currentLoc.y);
        m.markExplored(currentLoc.x, currentLoc.y+1);
        m.markExplored(currentLoc.x, currentLoc.y-1);
        m.markExplored(currentLoc.x+1, currentLoc.y);
        m.markExplored(currentLoc.x-1, currentLoc.y);
        m.markExplored(currentLoc.x-1, currentLoc.y+1);
        m.markExplored(currentLoc.x-1, currentLoc.y-1);
        m.markExplored(currentLoc.x+1, currentLoc.y+1);
        m.markExplored(currentLoc.x+1, currentLoc.y-1);
        MapStorage.currentLocation.setLocation(x,y);
        // Mark when robot reaches goal
        if(!m.isGoal(new Point(x, y))) {
            Variables.reachedGoal = true;
            //System.out.println(">>>>>>>>>>>>>>>>>>>>>> REACHED GOAL!");
        }
        Variables.traversedOver.add(new Grid(x, y));
    }

    private void markTraversed(int x, int y){
        if(exploredGrids.contains(new Grid(x, y))){
            exploredGrids.get(exploredGrids.indexOf(new Grid(x, y))).traversedOver = true;
        }else{
            Grid tempGrid = new Grid(x, y);
            tempGrid.traversedOver = true;
            exploredGrids.add(tempGrid);
        }
    }

    // Update the direction of robot
    public void updateRobotDirection(Direction direc){
        if(Variables.SIMULATION){
            try{
                TimeUnit.MILLISECONDS.sleep(1000/Variables.stepsPerSec);
            }catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
        if(Variables.DELAY_INDUCER){
            try{
                System.out.println("WAITING FOR 2 SECONDS");
                TimeUnit.MILLISECONDS.sleep(2000);
                Variables.DELAY_INDUCER = false;
            }catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
        currDirec = direc;
        MapStorage.currentDirection = direc;
        if(!Variables.noUpdate){
            checkSensors();
        }
        if(!Variables.PREVENT_SEND){
            DisplayMapGUI.refreshGrid(m, currentLoc, currDirec);
        }

        MapStorage.storeMap(getMap().getMap());
        // After updating map input, send to android
        String mdf = MapStorage.storeMap(getMap().getMap());
        // If Variables.nextMove is true, that means it's from fastest path.
        // We do not resend movement to RPI during fastest path!
        if(tcp != null){
            System.out.println("[Robot.java] Send: and; "+mdf);
            tcp.sendMessage("and; "+mdf);
        }else{
            //System.out.println("[Robot] Fake send: and; "+mdf);
        }
    }

    // Get distances from sensors
    public void checkSensors(){
        ImageRecce.imageFoundArrayList = new ArrayList<>();
        System.out.println("[Robot.java] ### Checking sensors. ");
        Scanner scanner = new Scanner(System.in);
        if(tcp != null){
            while (Variables.SENSOR_QUEUE.isEmpty()) {
               // System.out.print("WAITING FOR QUEUE: ");
                //scanner.nextLine();
                // r;
            }
            System.out.println("[Robot.java] #### Received new sensor reading. ");
            a = s.checkSensorA(currentLoc, currDirec);
            b = s.checkSensorB(currentLoc, currDirec);
            c = s.checkSensorC(currentLoc, currDirec);
            d = s.checkSensorD(currentLoc, currDirec);
            e = s.checkSensorE(currentLoc, currDirec);
            f = s.checkSensorF(currentLoc, currDirec);


            System.out.println("[Robot.java] #### Counter:"+Variables.counter+" - " + a + " " + b + " " + c + " " + d + " " + e + " " + f + " " );

            // Parse sensors
            parseDistances(a, 'a');
            parseDistances(b, 'b');
            parseDistances(c, 'c');
            parseDistances(d, 'd');
            parseDistances(e, 'e');
            parseDistances(f, 'f');
            // If no obstacle, no delay
            if(checkImageObstacle()){
                parseImage(currentLoc, currDirec);
                System.out.println("Obstacle.");
            }else{
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            Variables.SENSOR_QUEUE.clear();
        }else{
            a = s.checkSensorA(currentLoc, currDirec);
            b = s.checkSensorB(currentLoc, currDirec);
            c = s.checkSensorC(currentLoc, currDirec);
            d = s.checkSensorD(currentLoc, currDirec);
            e = s.checkSensorE(currentLoc, currDirec);
            f = s.checkSensorF(currentLoc, currDirec);

            // Parse sensors
            parseDistances(a, 'a');
            parseDistances(b, 'b');
            parseDistances(c, 'c');
            parseDistances(d, 'd');
            parseDistances(e, 'e');
            parseDistances(f, 'f');
            if(checkImageObstacle()){
                parseImage(currentLoc, currDirec);
                System.out.println("No obstacle.");
            }else{
                System.out.println("Obstacle.");
            }
        }
        System.out.println("RCL: " + currentLoc + currDirec);
        DisplayMapGUI.refreshGrid(m, currentLoc, currDirec);
    }

    private boolean checkImageObstacle(){
        int x = (int) getCurrentLocation().getX();
        int y = (int) getCurrentLocation().getY();
        switch(getCurrentDirection()){
            case W:
                if(m.isValid(x+1, y-2)){
                    if(m.hasObstacle(x+1, y-2)){
                        return true;
                    }
                }else{
                    return false;
                }
                break;
            case A:
                if(m.isValid(x+2, y+1)){
                    if(m.hasObstacle(x+2, y+1)){
                        return true;
                    }
                }else{
                    return false;
                }
                break;
            case S:
                if(m.isValid(x-1, y+2)){
                    if(m.hasObstacle(x-1, y+2)){
                        return true;
                    }
                }else{
                    return false;
                }
                break;
            case D:
                if(m.isValid(x-2, y-1)){
                    if(m.hasObstacle(x-2, y-1)){
                        return true;
                    }
                }else{
                    return false;
                }
                break;
        }
        return false;
    }

    private boolean markObstacle(int x, int y){
        if(exploredGrids.contains(new Grid(x, y))){
            // If grid has been explored, check
            if(exploredGrids.get(exploredGrids.indexOf(new Grid(x, y))).traversedOver){
                // If grid has been traversed over, do not mark
                // This means phantom block!
                return false;
            }else{
                return true;
            }
            /*else{
                if(exploredGrids.get(exploredGrids.indexOf(new Grid(x, y))).hasObstacle){
                    // If previous state of grid has obstacle, mark (or do nothing)
                    return true;
                }else{
                    // If previous state of grid has no obstacle, do not mark
                    // return false;
                    return true;
                }
            }
            */
        }else{
            // If grid has not been explored before, mark with obstacle
            Grid tempGrid = new Grid(x, y);
            tempGrid.hasObstacle = true;
            exploredGrids.add(tempGrid);
            return true;
        }
    }

    private boolean markExplored(int x, int y){
        if(exploredGrids.contains(new Grid(x, y))){
            // If grid has been explored, check
            /*if(exploredGrids.get(exploredGrids.indexOf(new Grid(x, y))).hasObstacle){
                // If previous state of grid has obstacles, update it as no obstacle
                exploredGrids.get(exploredGrids.indexOf(new Grid(x, y))).setHasObstacle(false);
                m.markExplored(x, y);
                return true;
            }else{
                // If previous state of grid has no obstacles, do nothing
                return false;
            }
            */
            return true;
        }else{
            // If grid has not been explored before, mark as explored
            Grid tempGrid = new Grid(x, y);
            tempGrid.hasObstacle = false;
            exploredGrids.add(tempGrid);
            return true;
        }
    }

    // Parse sensors to update GUI and the robot'sS internal map
    private void parseDistances(int distance, char sensorType){
        int noOfGrids = 0;
        // Assuming 10 cm = 1 grid
        noOfGrids = distance / 10;
        // System.out.println("Distances cleared: " +noOfGrids);
        // If sensor returns 0 cm = directly infront got obstacle / wall
        // If sensor returns > 10 cm = obstacle detected at that distance (noOfGrids)
        // Which grid to clear depends on which direction the robot is facing
        switch(currDirec){
            case W:
                if(sensorType == 'a'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x-(2),currentLoc.y-1))
                            return;
                        else {
                            if(markObstacle(currentLoc.x-(2),currentLoc.y-1)){
                                m.markHasObstacles(currentLoc.x-(2),currentLoc.y-1);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_A){ // means no obstacle
                    }else if(currentLoc.x-(2+noOfGrids) > 19){
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = 19 - currentLoc.x-(2+noOfGrids);
                    }else{
                        if(Map.isValid(currentLoc.x-(2+noOfGrids), currentLoc.y-1)){
                            if(markObstacle(currentLoc.x-(2+noOfGrids), currentLoc.y-1)){
                                m.markHasObstacles(currentLoc.x-(2+noOfGrids), currentLoc.y-1);
                                DisplayMapGUI.updateObstacles(currentLoc.x-(2+noOfGrids), currentLoc.y-1);
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x-(2+i), currentLoc.y-1)){
                            if(markExplored(currentLoc.x-(2+i), currentLoc.y-1)){
                                m.markExplored(currentLoc.x-(2+i), currentLoc.y-1);
                                DisplayMapGUI.updateExplored(currentLoc.x-(2+i), currentLoc.y-1);
                            }
                        }
                    }
                }else if(sensorType == 'b'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x-2,currentLoc.y))
                            return;
                        else {
                            if(markObstacle(currentLoc.x-2,currentLoc.y)){
                                m.markHasObstacles(currentLoc.x-2,currentLoc.y);
                            }
                            return;
                        }
                    }

                    if(noOfGrids == Variables.SENSOR_RANGE_B){ // means no obstacle
                        //System.out.println("No obstacle for sensor B!");
                    }else if(currentLoc.x-(2+noOfGrids) > 19){
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = 19 - currentLoc.x-(2+noOfGrids);
                    }else{
                        if(Map.isValid(currentLoc.x-(2+noOfGrids), currentLoc.y)){
                            if(markObstacle(currentLoc.x-(2+noOfGrids), currentLoc.y)){
                                m.markHasObstacles(currentLoc.x-(2+noOfGrids), currentLoc.y);
                                DisplayMapGUI.updateObstacles(currentLoc.x-(2+noOfGrids), currentLoc.y);
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x-(2+i), currentLoc.y)){
                            if(markExplored(currentLoc.x-(2+i), currentLoc.y)){
                                m.markExplored(currentLoc.x-(2+i), currentLoc.y);
                                DisplayMapGUI.updateExplored(currentLoc.x-(2+i), currentLoc.y);
                            }
                        }
                    }
                }else if(sensorType == 'c'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x-2,currentLoc.y+1))
                            return;
                        else {
                            if(markObstacle(currentLoc.x-2,currentLoc.y+1)){
                                m.markHasObstacles(currentLoc.x-2,currentLoc.y+1);
                            }
                            return;
                        }
                    }

                    if(noOfGrids == Variables.SENSOR_RANGE_C){ // means no obstacle
                        //System.out.println("No obstacle for sensor C!");
                    }else if(currentLoc.x-(2+noOfGrids) > 19){
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = 19 - currentLoc.x-(2+noOfGrids);
                    }else{
                        if(Map.isValid(currentLoc.x-(2+noOfGrids), currentLoc.y+1)){
                            if(markObstacle(currentLoc.x-(2+noOfGrids), currentLoc.y+1)){
                                m.markHasObstacles(currentLoc.x-(2+noOfGrids), currentLoc.y+1);
                                DisplayMapGUI.updateObstacles(currentLoc.x-(2+noOfGrids), currentLoc.y+1);
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x-(2+i), currentLoc.y+1)){
                            if (markExplored(currentLoc.x-(2+i), currentLoc.y+1)) {
                                m.markExplored(currentLoc.x-(2+i), currentLoc.y+1);
                                DisplayMapGUI.updateExplored(currentLoc.x-(2+i), currentLoc.y+1);
                            }
                        }
                    }
                }else if(sensorType == 'd'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x-1,currentLoc.y-2))
                            return;
                        else {
                            if(markObstacle(currentLoc.x-1,currentLoc.y-2)){
                                m.markHasObstacles(currentLoc.x-1,currentLoc.y-2);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_D){ // means no obstacle
                        //System.out.println("No obstacle for sensor D!");
                    }else if(currentLoc.y-(2+noOfGrids) < 0){
                        // currentLoc.y = 2 + noOfGrids
                        // currentLoc.y - 2 = noofGrids
                        // # math
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = currentLoc.y - 2;
                    }else{
                        if(Map.isValid(currentLoc.x-1, currentLoc.y-(2+noOfGrids))){
                            if(markObstacle(currentLoc.x-1, currentLoc.y-(2+noOfGrids))){
                                m.markHasObstacles(currentLoc.x-1, currentLoc.y-(2+noOfGrids));
                                DisplayMapGUI.updateObstacles(currentLoc.x-1, currentLoc.y-(2+noOfGrids));
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x-1, currentLoc.y-(2+i))){
                            if (markExplored(currentLoc.x-1, currentLoc.y-(2+i))) {
                                m.markExplored(currentLoc.x-1, currentLoc.y-(2+i));
                                DisplayMapGUI.updateExplored(currentLoc.x-1, currentLoc.y-(2+i));
                            }
                        }
                    }
                }else if(sensorType == 'e'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x+1,currentLoc.y-2))
                            return;
                        else {
                            if (markObstacle(currentLoc.x+1,currentLoc.y-2)) {
                                m.markHasObstacles(currentLoc.x+1,currentLoc.y-2);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_E){ // means no obstacle
                        //System.out.println("No obstacle for sensor E!");
                    }else if(currentLoc.y-(2+noOfGrids) < 0){
                        // currentLoc.y = 2 + noOfGrids
                        // currentLoc.y - 2 = noofGrids
                        // # math
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = currentLoc.y - 2;
                    }else{
                        if(Map.isValid(currentLoc.x+1, currentLoc.y-(2+noOfGrids))){
                            if(markObstacle(currentLoc.x+1, currentLoc.y-(2+noOfGrids))){
                                m.markHasObstacles(currentLoc.x+1, currentLoc.y-(2+noOfGrids));
                                DisplayMapGUI.updateObstacles(currentLoc.x+1, currentLoc.y-(2+noOfGrids));
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x+1, currentLoc.y-(2+i))){
                            if(markExplored(currentLoc.x+1, currentLoc.y-(2+i))){
                                m.markExplored(currentLoc.x+1, currentLoc.y-(2+i));
                                DisplayMapGUI.updateExplored(currentLoc.x+1, currentLoc.y-(2+i));
                            }
                        }
                    }
                }else if(sensorType == 'f'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x-1,currentLoc.y+2))
                            return;
                        else {
                            if(markObstacle(currentLoc.x-1,currentLoc.y+2)){
                                m.markHasObstacles(currentLoc.x-1,currentLoc.y+2);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_F){ // means no obstacle
                        //System.out.println("No obstacle for sensor F!");
                    }else if(currentLoc.y-(2+noOfGrids) > 14){
                        // currentLoc.y - 2 - noOfGrids = 14
                        // -noOfGrids = 14 - currentLoc.y + 2
                        // noOfGrids = 14 + currentLoc.y - 2
                        // # math
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = 14 + currentLoc.y - 2;
                    }else{
                        if(Map.isValid(currentLoc.x-1, currentLoc.y+(2+noOfGrids))){
                            if(markObstacle(currentLoc.x-1, currentLoc.y+(2+noOfGrids))){
                                m.markHasObstacles(currentLoc.x-1, currentLoc.y+(2+noOfGrids));
                                DisplayMapGUI.updateObstacles(currentLoc.x-1, currentLoc.y+(2+noOfGrids));
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x-1, currentLoc.y+(2+i))){
                            if(markExplored(currentLoc.x-1, currentLoc.y+(2+i))){
                                m.markExplored(currentLoc.x-1, currentLoc.y+(2+i));
                                DisplayMapGUI.updateExplored(currentLoc.x-1, currentLoc.y+(2+i));
                            }
                        }

                    }
                }
                break;
            case A:
                if(sensorType == 'a'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x+1,currentLoc.y-2))
                            return;
                        else {
                            if(markObstacle(currentLoc.x+1,currentLoc.y-2)){
                                m.markHasObstacles(currentLoc.x+1,currentLoc.y-2);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_A){ // means no obstacle
                        //System.out.println("No obstacle for sensor A!");
                    }else if(currentLoc.y-(2+noOfGrids) < 0){
                        // 0 = currentLoc.y - 2 - noOfGrids
                        // noOfGrids = currentLoc.y - 2
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = currentLoc.y - 2;
                    }else{
                        if(Map.isValid(currentLoc.x+1, currentLoc.y-(2+noOfGrids))){
                            if (markObstacle(currentLoc.x+1, currentLoc.y-(2+noOfGrids))) {
                                m.markHasObstacles(currentLoc.x+1, currentLoc.y-(2+noOfGrids));
                                DisplayMapGUI.updateObstacles(currentLoc.x+1, currentLoc.y-(2+noOfGrids));
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x+1, currentLoc.y-(2+i))){
                            if(markExplored(currentLoc.x+1, currentLoc.y-(2+i))){
                                m.markExplored(currentLoc.x+1, currentLoc.y-(2+i));
                                DisplayMapGUI.updateExplored(currentLoc.x+1, currentLoc.y-(2+i));
                            }
                        }
                    }
                }else if(sensorType == 'b'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x,currentLoc.y-2))
                            return;
                        else {
                            if(markObstacle(currentLoc.x,currentLoc.y-2)){
                                m.markHasObstacles(currentLoc.x,currentLoc.y-2);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_B){ // means no obstacle
                        //System.out.println("No obstacle for sensor B!");
                    }else if(currentLoc.y-(2+noOfGrids) < 0){
                        // 0 = currentLoc.y - 2 - noOfGrids
                        // noOfGrids = currentLoc.y - 2
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        noOfGrids = currentLoc.y - 2;
                    }else{
                        if(Map.isValid(currentLoc.x, currentLoc.y-(2+noOfGrids))){
                            if(markObstacle(currentLoc.x, currentLoc.y-(2+noOfGrids))){
                                m.markHasObstacles(currentLoc.x, currentLoc.y-(2+noOfGrids));
                                DisplayMapGUI.updateObstacles(currentLoc.x, currentLoc.y-(2+noOfGrids));
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x, currentLoc.y-(2+i))){
                            if(markExplored(currentLoc.x, currentLoc.y-(2+i))){
                                m.markExplored(currentLoc.x, currentLoc.y-(2+i));
                                DisplayMapGUI.updateExplored(currentLoc.x, currentLoc.y-(2+i));
                            }
                        }
                    }
                }else if(sensorType == 'c'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x-1,currentLoc.y-2))
                            return;
                        else {
                            if(markObstacle(currentLoc.x-1,currentLoc.y-2)){
                                m.markHasObstacles(currentLoc.x-1,currentLoc.y-2);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_C){ // means no obstacle
                        //System.out.println("No obstacle for sensor C!");
                    }else if(currentLoc.y-(2+noOfGrids) < 0){
                        // 0 = currentLoc.y - 2 - noOfGrids
                        // noOfGrids = currentLoc.y - 2
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = currentLoc.y - 2;
                    }else{
                        if(Map.isValid(currentLoc.x-1, currentLoc.y-(2+noOfGrids))){
                            if(markObstacle(currentLoc.x-1, currentLoc.y-(2+noOfGrids))){
                                m.markHasObstacles(currentLoc.x-1, currentLoc.y-(2+noOfGrids));
                                DisplayMapGUI.updateObstacles(currentLoc.x-1, currentLoc.y-(2+noOfGrids));
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x-1, currentLoc.y-(2+i))) {
                            if(markExplored(currentLoc.x-1, currentLoc.y-(2+i))){
                                m.markExplored(currentLoc.x-1, currentLoc.y-(2+i));
                                DisplayMapGUI.updateExplored(currentLoc.x-1, currentLoc.y-(2+i));
                            }
                        }
                    }
                }else if(sensorType == 'd'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x+2,currentLoc.y-1))
                            return;
                        else {
                            if(markObstacle(currentLoc.x+2,currentLoc.y-1)){
                                m.markHasObstacles(currentLoc.x+2,currentLoc.y-1);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_D){ // means no obstacle
                        //System.out.println("No obstacle for sensor D!");
                    }else if(currentLoc.x+(2+noOfGrids) > 19){
                        // 0 = currentLoc.y - 2 - noOfGrids
                        // noOfGrids = currentLoc.y - 2
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = 19 - currentLoc.x - 2;
                    }else{
                        if(Map.isValid(currentLoc.x+(2+noOfGrids), currentLoc.y-1)){
                            if(markObstacle(currentLoc.x+(2+noOfGrids), currentLoc.y-1)){
                                m.markHasObstacles(currentLoc.x+(2+noOfGrids), currentLoc.y-1);
                                DisplayMapGUI.updateObstacles(currentLoc.x+(2+noOfGrids), currentLoc.y-1);
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x+(2+i), currentLoc.y-1)){
                            if(markExplored(currentLoc.x+(2+i), currentLoc.y-1)){
                                m.markExplored(currentLoc.x+(2+i), currentLoc.y-1);
                                DisplayMapGUI.updateExplored(currentLoc.x+(2+i), currentLoc.y-1);
                            }
                        }
                    }
                }else if(sensorType == 'e'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x+2,currentLoc.y+1))
                            return;
                        else {
                            if(markObstacle(currentLoc.x+2,currentLoc.y+1)){
                                m.markHasObstacles(currentLoc.x+2,currentLoc.y+1);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_E){ // means no obstacle
                        //System.out.println("No obstacle for sensor E!");
                    }else if(currentLoc.x+(2+noOfGrids) > 19){
                        // 0 = currentLoc.y - 2 - noOfGrids
                        // noOfGrids = currentLoc.y - 2
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = 19 - currentLoc.x - 2;
                    }else{
                        if(Map.isValid(currentLoc.x+(2+noOfGrids), currentLoc.y+1)){
                            if(markObstacle(currentLoc.x+(2+noOfGrids), currentLoc.y+1)){
                                m.markHasObstacles(currentLoc.x+(2+noOfGrids), currentLoc.y+1);
                                DisplayMapGUI.updateObstacles(currentLoc.x+(2+noOfGrids), currentLoc.y+1);
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x+(2+i), currentLoc.y+1)){
                            if(markExplored(currentLoc.x+(2+i), currentLoc.y+1)){
                                m.markExplored(currentLoc.x+(2+i), currentLoc.y+1);
                                DisplayMapGUI.updateExplored(currentLoc.x+(2+i), currentLoc.y+1);
                            }
                        }
                    }
                }else if(sensorType == 'f'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x-2,currentLoc.y-1))
                            return;
                        else {
                            if(markObstacle(currentLoc.x-2,currentLoc.y-1)){
                                m.markHasObstacles(currentLoc.x-2,currentLoc.y-1);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_F){ // means no obstacle
                        //System.out.println("No obstacle for sensor F!");
                    }else if(currentLoc.x-(2+noOfGrids) < 0){
                        // 0 = currentLoc.x - 2 - noOfGrids
                        // noOfGrids = currentLoc.y - 2
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = currentLoc.x - 2;
                    }else{
                        if(Map.isValid(currentLoc.x-(2+noOfGrids), currentLoc.y-1)){
                            if(markObstacle(currentLoc.x-(2+noOfGrids), currentLoc.y-1)){
                                m.markHasObstacles(currentLoc.x-(2+noOfGrids), currentLoc.y-1);
                                DisplayMapGUI.updateObstacles(currentLoc.x-(2+noOfGrids), currentLoc.y-1);
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x-(2+i), currentLoc.y-1)){
                            if(markExplored(currentLoc.x-(2+i), currentLoc.y-1)){
                                m.markExplored(currentLoc.x-(2+i), currentLoc.y-1);
                                DisplayMapGUI.updateExplored(currentLoc.x-(2+i), currentLoc.y-1);
                            }
                        }
                    }
                }
                break;
            case S:
                if(sensorType == 'a'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x+2,currentLoc.y+1))
                            return;
                        else {
                            if(markObstacle(currentLoc.x+2,currentLoc.y+1)){
                                m.markHasObstacles(currentLoc.x+2,currentLoc.y+1);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_A){ // means no obstacle
                        //System.out.println("No obstacle for sensor A!");
                    }else if(currentLoc.x+(2+noOfGrids) > 19){
                        // 19 = currentLoc.x + 2 + noOfGrids
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = 19 - currentLoc.x - 2;
                    }else{
                        if(Map.isValid(currentLoc.x+(2+noOfGrids), currentLoc.y+1)){
                            if(markObstacle(currentLoc.x+(2+noOfGrids), currentLoc.y+1)){
                                m.markHasObstacles(currentLoc.x+(2+noOfGrids), currentLoc.y+1);
                                DisplayMapGUI.updateObstacles(currentLoc.x+(2+noOfGrids), currentLoc.y+1);
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x+(2+i), currentLoc.y+1)){
                            if(markExplored(currentLoc.x+(2+i), currentLoc.y+1)){
                                m.markExplored(currentLoc.x+(2+i), currentLoc.y+1);
                                DisplayMapGUI.updateExplored(currentLoc.x+(2+i), currentLoc.y+1);
                            }
                        }
                    }
                }else if(sensorType == 'b'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x+2,currentLoc.y))
                            return;
                        else {
                            if(markObstacle(currentLoc.x+2,currentLoc.y)){
                                m.markHasObstacles(currentLoc.x+2,currentLoc.y);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_B){ // means no obstacle
                        //System.out.println("No obstacle for sensor B!");
                    }else if(currentLoc.x+(2+noOfGrids) > 19) {
                        // 19 = currentLoc.x + 2 + noOfGrids
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = 19 - currentLoc.x - 2;
                    }else{
                        if(Map.isValid(currentLoc.x+(2+noOfGrids), currentLoc.y)){
                            if(markObstacle(currentLoc.x+(2+noOfGrids), currentLoc.y)){
                                m.markHasObstacles(currentLoc.x+(2+noOfGrids), currentLoc.y);
                                DisplayMapGUI.updateObstacles(currentLoc.x+(2+noOfGrids), currentLoc.y);
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x+(2+i), currentLoc.y)){
                            if (markExplored(currentLoc.x+(2+i), currentLoc.y)) {
                                m.markExplored(currentLoc.x+(2+i), currentLoc.y);
                                DisplayMapGUI.updateExplored(currentLoc.x+(2+i), currentLoc.y);
                            }
                        }
                    }
                }else if(sensorType == 'c'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x+2,currentLoc.y-1))
                            return;
                        else {
                            if(markObstacle(currentLoc.x+2,currentLoc.y-1)){
                                m.markHasObstacles(currentLoc.x+2,currentLoc.y-1);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_C){ // means no obstacle
                        //System.out.println("No obstacle for sensor C!");
                    }else if(currentLoc.x+(2+noOfGrids) > 19) {
                        // 19 = currentLoc.x + 2 + noOfGrids
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        noOfGrids = 19 - currentLoc.x - 2;
                    }else{
                        if(Map.isValid(currentLoc.x+(2+noOfGrids), currentLoc.y-1)){
                            if(markObstacle(currentLoc.x+(2+noOfGrids), currentLoc.y-1)){
                                m.markHasObstacles(currentLoc.x+(2+noOfGrids), currentLoc.y-1);
                                DisplayMapGUI.updateObstacles(currentLoc.x+(2+noOfGrids), currentLoc.y-1);
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x+(2+i), currentLoc.y-1)){
                            if(markExplored(currentLoc.x+(2+i), currentLoc.y-1)){
                                m.markExplored(currentLoc.x+(2+i), currentLoc.y-1);
                                DisplayMapGUI.updateExplored(currentLoc.x+(2+i), currentLoc.y-1);
                            }
                        }
                    }
                }else if(sensorType == 'd'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x+1,currentLoc.y+2))
                            return;
                        else {
                            if(markObstacle(currentLoc.x+1,currentLoc.y+2)){
                                m.markHasObstacles(currentLoc.x+1,currentLoc.y+2);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_D){ // means no obstacle
                        //System.out.println("No obstacle for sensor D!");
                    }else if(currentLoc.y+(2+noOfGrids) > 14) {
                        // 19 = currentLoc.x + 2 + noOfGrids
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        noOfGrids = 14 - currentLoc.y - 2;
                    }else{
                        if(Map.isValid(currentLoc.x+1, currentLoc.y+(2+noOfGrids))){
                            if(markObstacle(currentLoc.x+1, currentLoc.y+(2+noOfGrids))){
                                m.markHasObstacles(currentLoc.x+1, currentLoc.y+(2+noOfGrids));
                                DisplayMapGUI.updateObstacles(currentLoc.x+1, currentLoc.y+(2+noOfGrids));
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x+1, currentLoc.y+(2+i))){
                            if(markExplored(currentLoc.x+1, currentLoc.y+(2+i))){
                                m.markExplored(currentLoc.x+1, currentLoc.y+(2+i));
                                DisplayMapGUI.updateExplored(currentLoc.x+1, currentLoc.y+(2+i));
                            }
                        }
                    }
                }else if(sensorType == 'e'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x-1,currentLoc.y+2))
                            return;
                        else {
                            if(markObstacle(currentLoc.x-1,currentLoc.y+2)){
                                m.markHasObstacles(currentLoc.x-1,currentLoc.y+2);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_E){ // means no obstacle
                        //System.out.println("No obstacle for sensor E!");
                    }else if(currentLoc.y+(2+noOfGrids) > 14) {
                        // 19 = currentLoc.x + 2 + noOfGrids
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        noOfGrids = 14 - currentLoc.y - 2;
                    }else{
                        if(Map.isValid(currentLoc.x-1, currentLoc.y+(2+noOfGrids))){
                            if(markObstacle(currentLoc.x-1, currentLoc.y+(2+noOfGrids))){
                                m.markHasObstacles(currentLoc.x-1, currentLoc.y+(2+noOfGrids));
                                DisplayMapGUI.updateObstacles(currentLoc.x-1, currentLoc.y+(2+noOfGrids));
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x-1, currentLoc.y+(2+i))){
                            if(markExplored(currentLoc.x-1, currentLoc.y+(2+i))){
                                m.markExplored(currentLoc.x-1, currentLoc.y+(2+i));
                                DisplayMapGUI.updateExplored(currentLoc.x-1, currentLoc.y+(2+i));
                            }
                        }
                    }
                }else if(sensorType == 'f'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x+1,currentLoc.y-2))
                            return;
                        else {
                            if(markObstacle(currentLoc.x+1,currentLoc.y-2)){
                                m.markHasObstacles(currentLoc.x+1,currentLoc.y-2);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_F){ // means no obstacle
                        //System.out.println("No obstacle for sensor F!");
                    }else if(currentLoc.y-(2+noOfGrids) < 0){
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = 14 - currentLoc.y+(2+noOfGrids);
                    }else{
                        if(Map.isValid(currentLoc.x+1, currentLoc.y-(2+noOfGrids))){
                            if(markObstacle(currentLoc.x+1, currentLoc.y-(2+noOfGrids))){
                                m.markHasObstacles(currentLoc.x+1, currentLoc.y-(2+noOfGrids));
                                DisplayMapGUI.updateObstacles(currentLoc.x+1, currentLoc.y-(2+noOfGrids));
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x+1, currentLoc.y-(2+i))){
                            if(markExplored(currentLoc.x+1, currentLoc.y-(2+i))){
                                m.markExplored(currentLoc.x+1, currentLoc.y-(2+i));
                                DisplayMapGUI.updateExplored(currentLoc.x+1, currentLoc.y-(2+i));
                            }
                        }

                    }
                }
                break;
            case D:
                if(sensorType == 'a'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x-1,currentLoc.y+2))
                            return;
                        else {
                            if(markObstacle(currentLoc.x-1,currentLoc.y+2)){
                                m.markHasObstacles(currentLoc.x-1,currentLoc.y+2);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_A){ // means no obstacle
                        //System.out.println("No obstacle for sensor A!");
                    }else if(currentLoc.y+(2+noOfGrids) > 14){
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = 14 - currentLoc.y+(2+noOfGrids);
                    }else{
                        if(Map.isValid(currentLoc.x-1, currentLoc.y+noOfGrids+2)) {
                            if (markObstacle(currentLoc.x-1, currentLoc.y+noOfGrids+2)) {
                                m.markHasObstacles(currentLoc.x-1, currentLoc.y+noOfGrids+2);
                                DisplayMapGUI.updateObstacles(currentLoc.x-1, currentLoc.y+noOfGrids+2);
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x-1, currentLoc.y+(2+i))){
                            if(markExplored(currentLoc.x-1, currentLoc.y+(2+i))){
                                m.markExplored(currentLoc.x-1, currentLoc.y+(2+i));
                                DisplayMapGUI.updateExplored(currentLoc.x-1, currentLoc.y+(2+i));
                            }
                        }
                    }

                }else if(sensorType == 'b'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x,currentLoc.y+2))
                            return;
                        else {
                            if(markObstacle(currentLoc.x,currentLoc.y+2)){
                                m.markHasObstacles(currentLoc.x,currentLoc.y+2);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_B){ // means no obstacle
                        //System.out.println("No obstacle for sensor B!");
                    }else if(currentLoc.y+(2+noOfGrids) > 14){
                        // If the sensor goes beyond the wall, then limit the sensor readings to the wall
                        // noOfGrids = 14 - currentLoc.y+(2+noOfGrids);
                    }else{
                        if(Map.isValid(currentLoc.x, currentLoc.y+(2+noOfGrids))){
                            if(markObstacle(currentLoc.x, currentLoc.y+(2+noOfGrids))){
                                m.markHasObstacles(currentLoc.x, currentLoc.y+(2+noOfGrids));
                                DisplayMapGUI.updateObstacles(currentLoc.x, currentLoc.y+(2+noOfGrids));
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x, currentLoc.y+(2+i))){
                            if(markExplored(currentLoc.x, currentLoc.y+(2+i))){
                                m.markExplored(currentLoc.x, currentLoc.y+(2+i));
                                DisplayMapGUI.updateExplored(currentLoc.x, currentLoc.y+(2+i));
                            }
                        }
                    }
                }else if(sensorType == 'c'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x+1,currentLoc.y+2))
                            return;
                        else {
                            if(markObstacle(currentLoc.x+1,currentLoc.y+2)){
                                m.markHasObstacles(currentLoc.x+1,currentLoc.y+2);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_C){ // means no obstacle
                        //System.out.println("No obstacle for sensor C!");
                    }else if(currentLoc.y+(2+noOfGrids) > 14){
                        // Need to catch if sensors go out of map
                    }else{
                        //System.out.println("Obstacle for D:C!");
                        if(Map.isValid(currentLoc.x+1, currentLoc.y+(2+noOfGrids))){
                            if(markObstacle(currentLoc.x+1, currentLoc.y+(2+noOfGrids))){
                                m.markHasObstacles(currentLoc.x+1, currentLoc.y+(2+noOfGrids));
                                DisplayMapGUI.updateObstacles(currentLoc.x+1, currentLoc.y+(2+noOfGrids));
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x+1, currentLoc.y+(2+i))){
                            if(markExplored(currentLoc.x+1, currentLoc.y+(2+i))){
                                m.markExplored(currentLoc.x+1, currentLoc.y+(2+i));
                                DisplayMapGUI.updateExplored(currentLoc.x+1, currentLoc.y+(2+i));
                            }
                        }
                    }
                }else if(sensorType == 'd'){
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x-2,currentLoc.y+1))
                            return;
                        else {
                            if(markObstacle(currentLoc.x-2,currentLoc.y+1)){
                                m.markHasObstacles(currentLoc.x-2,currentLoc.y+1);
                            }
                            return;
                        }
                    }
                    // Test begin
                    if(noOfGrids == Variables.SENSOR_RANGE_D){ // means no obstacle
                        //System.out.println("No obstacle for sensor D!");
                    }else if(currentLoc.x-(2+noOfGrids) > 19){
                        // Need to catch if sensors go out of map
                    }else{
                        if(Map.isValid(currentLoc.x-(2+noOfGrids), currentLoc.y+1)){
                            if(markObstacle(currentLoc.x-(2+noOfGrids), currentLoc.y+1)){
                                m.markHasObstacles(currentLoc.x-(2+noOfGrids), currentLoc.y+1);
                                DisplayMapGUI.updateObstacles(currentLoc.x-(2+noOfGrids), currentLoc.y+1);
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x-(2+i), currentLoc.y+1)){
                            if(markExplored(currentLoc.x-(2+i), currentLoc.y+1)){
                                m.markExplored(currentLoc.x-(2+i), currentLoc.y+1);
                                DisplayMapGUI.updateExplored(currentLoc.x-(2+i), currentLoc.y+1);
                            }
                        }
                    }
                }else if(sensorType == 'e'){
                    // Test begin
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x-2,currentLoc.y-1))
                            return;
                        else {
                            if(markObstacle(currentLoc.x-2,currentLoc.y-1)){
                                m.markHasObstacles(currentLoc.x-2,currentLoc.y-1);
                            }
                            return;
                        }
                    }
                    if(noOfGrids == Variables.SENSOR_RANGE_E){ // means no obstacle
                        //System.out.println("No obstacle for sensor E!");
                    }else if(currentLoc.x-(2+noOfGrids) > 19){
                        // Need to catch if sensors go out of map
                    }else{
                        if(Map.isValid(currentLoc.x-(2+noOfGrids), currentLoc.y-1)){
                            if(markObstacle(currentLoc.x-(2+noOfGrids), currentLoc.y-1)){
                                m.markHasObstacles(currentLoc.x-(2+noOfGrids), currentLoc.y-1);
                                DisplayMapGUI.updateObstacles(currentLoc.x-(2+noOfGrids), currentLoc.y-1);
                            }
                        }
                    }
                    // Test end
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x-(2+i), currentLoc.y-1)){
                            if(markExplored(currentLoc.x-(2+i), currentLoc.y-1)){
                                m.markExplored(currentLoc.x-(2+i), currentLoc.y-1);
                                DisplayMapGUI.updateExplored(currentLoc.x-(2+i), currentLoc.y-1);
                            }
                        }
                    }
                }else if(sensorType == 'f'){
                    if(noOfGrids == 0){ // Obstacle or Wall in front
                        if(!Map.isValid(currentLoc.x+2,currentLoc.y+1))
                            return;
                        else {
                            if(markObstacle(currentLoc.x+2,currentLoc.y+1)){
                                m.markHasObstacles(currentLoc.x+2,currentLoc.y+1);
                            }
                            return;
                        }
                    }
                    // Test begin
                    if(noOfGrids == Variables.SENSOR_RANGE_F){ // means no obstacle
                        // System.out.println("No obstacle for sensor F!");
                    }else if(currentLoc.x+(2+noOfGrids) > 19){
                        // Need to catch if sensors go out of map
                    }else{
                        if(Map.isValid(currentLoc.x+(2+noOfGrids), currentLoc.y+1)){
                            if(markObstacle(currentLoc.x+(2+noOfGrids), currentLoc.y+1)){
                                m.markHasObstacles(currentLoc.x+(2+noOfGrids), currentLoc.y+1);
                                DisplayMapGUI.updateObstacles(currentLoc.x+(2+noOfGrids), currentLoc.y+1);
                            }
                        }
                    }
                    for(int i = 0; i < noOfGrids; i++){
                        if(Map.isValid(currentLoc.x+(2+i), currentLoc.y+1)){
                            if(markExplored(currentLoc.x+(2+i), currentLoc.y+1)){
                                m.markExplored(currentLoc.x+(2+i), currentLoc.y+1);
                                DisplayMapGUI.updateExplored(currentLoc.x+(2+i), currentLoc.y+1);
                            }
                        }
                    }
                }
                break;
        }
        return;
    }

    private void parseImage(Point temp, Direction d){
        if(ImageRecce.imagePrevious) {
            ImageRecce.imagePrevious = false;
            return;
        }
        Point p = new Point();
        //delayForImages();
        if(!ImageRecce.imageFoundArrayList.isEmpty()){
            switch(d){
                case W:
                    p.y = temp.y - 2;
                    p.x = temp.x + 1;
                    break;
                case A:
                    p.y = temp.y + 1;
                    p.x = temp.x + 2;
                    break;
                case S:
                    p.y = temp.y + 2;
                    p.x = temp.x - 1;
                    break;
                case D:
                    p.y = temp.y - 1;
                    p.x = temp.x - 2;
                    break;
            }

            System.out.println("Image point = " + p);
            System.out.println(m.hasObstacle((int)p.x,(int)p.y));
            if(m.hasObstacle((int)p.x,(int)p.y)) {
                ImageRecce.imagePrevious = true;
                Image image = new Image(p, d, parseObstacleFace(d));
                System.out.println(image.getP() + " ==== " + image.getD() + "===" + image.getObstacleFace());
                if (!ImageRecce.imageArrayList.contains(image)) {
                    System.out.println("New Image Found!");
                    ImageRecce.imageArrayList.add(image);
                    ImageRecce.imagePointArrayList.add(p);
                    ImageRecce.imageCount++;
                }
            }
        }
    }

    private String parseObstacleFace(Direction d){
        switch(d){
            case W:
                return "R";
            case A:
                return "U";
            case S:
                return "L";
            case D:
                return "D";
        }
        return "LOL GG";
    }

    private void delayForImages(){
        try {
            // Changed from 500 to 200
            TimeUnit.MILLISECONDS.sleep(500);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}
