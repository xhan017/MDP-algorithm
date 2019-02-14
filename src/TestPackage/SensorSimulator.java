package TestPackage;

import Model.Direction;
import Model.Map;
import Model.Variables;

import java.awt.*;

public class SensorSimulator {
    // Set sensor detection distances
    // This is used to simulate the effective (and reliable) range of sensors
    final int SENSOR_RANGE_A = Variables.SENSOR_RANGE_A;
    final int SENSOR_RANGE_B = Variables.SENSOR_RANGE_B;
    final int SENSOR_RANGE_C = Variables.SENSOR_RANGE_C;
    final int SENSOR_RANGE_D = Variables.SENSOR_RANGE_D;
    final int SENSOR_RANGE_E = Variables.SENSOR_RANGE_E;
    final int SENSOR_RANGE_F = Variables.SENSOR_RANGE_F;
    final int WALL_DISTANCE = 0;

    Map m;
    Direction d;

    public SensorSimulator(){
        String s = Controller.MapStorage.readMap(1, Variables.INPUT_FILE_NAME);
        this.m = new Map(s);
        m.populateObstacles(Controller.MapStorage.readMap(2, Variables.INPUT_FILE_NAME));
    }

    // Returns simulated distance to obstacle
    public int getSimulatedSensor_A(Point p, Direction d){
        // Performs calculation based on the sensor range and distance
        for(int i = 0; i < SENSOR_RANGE_A; i++){
            switch(d){
                case W:
                    if(!isValid(p.x-(2+i), p.y-1)) return 10*i;
                    if(m.hasObstacle(p.x-(2+i), p.y-1)){
                        return 10*i;
                    }
                    break;
                case A:
                    if(!isValid(p.x+1, p.y-(2+i))) return 10*i;
                    if(m.hasObstacle(p.x+1, p.y-(2+i))){
                        return 10*i;
                    }
                    break;
                case S:
                    if(!isValid(p.x+(2+i), p.y+1)) return 10*i;
                    if(m.hasObstacle(p.x+(2+i), p.y+1)){
                        return 10*i;
                    }
                    break;
                case D:
                    if(!isValid(p.x-1, p.y+(2+i))) return 10*i;
                    if(m.hasObstacle(p.x-1, p.y+(2+i))){
                        return 10*i;
                    }
                    break;
            }
        }
        return 10*SENSOR_RANGE_A;
    }

    public int getSimulatedSensor_B(Point p, Direction d){
        // Performs calculation based on the sensor range and distance
        for(int i = 0; i < SENSOR_RANGE_B; i++){
            switch(d){
                case W:
                    if(!isValid(p.x-(2+i), p.y)) return 10*i;
                    if(m.hasObstacle(p.x-(2+i), p.y)){
                        return 10*i;
                    }
                    break;
                case A:
                    if(!isValid(p.x, p.y-(2+i))) return 10*i;
                    if(m.hasObstacle(p.x, p.y-(2+i))){
                        return 10*i;
                    }
                    break;
                case S:
                    if(!isValid(p.x+(2+i), p.y)) return 10*i;
                    if(m.hasObstacle(p.x+(2+i), p.y)){
                        return 10*i;
                    }
                    break;
                case D:
                    if(!isValid(p.x, p.y+(2+i))) return 10*i;
                    if(m.hasObstacle(p.x, p.y+(2+i))){
                        return 10*i;
                    }
                    break;
            }
        }
        return 10*SENSOR_RANGE_B;
    }

    public int getSimulatedSensor_C(Point p, Direction d){
        // Performs calculation based on the sensor range and distance
        for(int i = 0; i < SENSOR_RANGE_C; i++){
            switch(d){
                case W:
                    if(!isValid(p.x-(2+i), p.y+1)) return 10*i;
                    if(m.hasObstacle(p.x-(2+i), p.y+1)){
                        return 10*i;
                    }
                    break;
                case A:
                    if(!isValid(p.x-1, p.y-(2+i))) return 10*i;
                    if(m.hasObstacle(p.x-1, p.y-(2+i))){
                        return 10*i;
                    }
                    break;
                case S:
                    if(!isValid(p.x+(2+i), p.y-1)) return 10*i;
                    if(m.hasObstacle(p.x+(2+i), p.y-1)){
                        return 10*i;
                    }
                    break;
                case D:
                    if(!isValid(p.x+1, p.y+(2+i))) return 10*i;
                    if(m.hasObstacle(p.x+1, p.y+(2+i))){
                        return 10*i;
                    }
                    break;
            }
        }
        return 10*SENSOR_RANGE_C;
    }

    public int getSimulatedSensor_D(Point p, Direction d){
        // Performs calculation based on the sensor range and distance
        for(int i = 0; i < SENSOR_RANGE_D; i++){
            switch(d){
                case W:
                    if(!isValid(p.x-1, p.y-(2+i))) return 10*i;
                    if(m.hasObstacle(p.x-1, p.y-(2+i))){
                        return 10*i;
                    }
                    break;
                case A:
                    if(!isValid(p.x+(2+i), p.y-1)) return 10*i;
                    if(m.hasObstacle(p.x+(2+i), p.y-1)){
                        return 10*i;
                    }
                    break;
                case S:
                    if(!isValid(p.x+1, p.y+(2+i))) return 10*i;
                    if(m.hasObstacle(p.x+1, p.y+(2+i))){
                        return 10*i;
                    }
                    break;
                case D:
                    if(!isValid(p.x-(2+i), p.y+1)) return 10*i;
                    if(m.hasObstacle(p.x-(2+i), p.y+1)){
                        return 10*i;
                    }
                    break;
            }
        }
        return 10*SENSOR_RANGE_D;
    }

    public int getSimulatedSensor_E(Point p, Direction d){
        // Performs calculation based on the sensor range and distance
        for(int i = 0; i < SENSOR_RANGE_E; i++){
            switch(d){
                case W:
                    if(!isValid(p.x+1, p.y-(2+i)))
                        return 10*i;
                    if(m.hasObstacle(p.x+1, p.y-(2+i))){
                        return 10*i;
                    }
                    break;
                case A:
                    if(!isValid(p.x+(2+i), p.y+1)) return 10*i;
                    if(m.hasObstacle(p.x+(2+i), p.y+1)){
                        return 10*i;
                    }
                    break;
                case S:
                    if(!isValid(p.x-1, p.y+(2+i))) return 10*i;
                    if(m.hasObstacle(p.x-1, p.y+(2+i))){
                        return 10*i;
                    }
                    break;
                case D:
                    if(!isValid(p.x-(2+i), p.y-1)) return 10*i;
                    if(m.hasObstacle(p.x-(2+i), p.y-1)){
                        return 10*i;
                    }
                    break;
            }
        }
        return 10*SENSOR_RANGE_E;
    }

    public int getSimulatedSensor_F(Point p, Direction d){
        // Performs calculation based on the sensor range and distance
        // Initialize i = 1 due to long range sensor's BLIND SPOT
        for(int i = 0; i < SENSOR_RANGE_F; i++){
            switch(d){
                case W:
                    if(!isValid(p.x-1, p.y+(2+i)))
                        return 10*i;
                    if(m.hasObstacle(p.x-1, p.y+(2+i))){
                        return 10*i;
                    }
                    break;
                case A:
                    if(!isValid(p.x-(2+i), p.y-1))
                        return 10*i;
                    if(m.hasObstacle(p.x-(2+i), p.y-1)){
                        return 10*i;
                    }
                    break;
                case S:
                    if(!isValid(p.x+1, p.y-(2+i)))
                        return 10*i;
                    if(m.hasObstacle(p.x+1, p.y-(2+i))){
                        return 10*i;
                    }
                    break;
                case D:
                    if(!isValid(p.x+(2+i), p.y+1))
                        return 10*i;
                    if(m.hasObstacle(p.x+(2+i), p.y+1)){
                        return 10*i;
                    }
                    break;
            }
        }
        return 10*SENSOR_RANGE_F;
    }

    // Simulates sensors
    // Returns distance to obstacle
    // Returns -1 if no obstacles found
    // TO CHANGE

    private boolean isValid(int x, int y){
        if((x >= 0 && x <= 19) && (y >= 0 && y <= 14)){
            return true;
        }
        return false;
    }
}
