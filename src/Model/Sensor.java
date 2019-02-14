package Model;

import Controller.TcpService;
import TestPackage.SensorSimulator;

import java.awt.*;

public class Sensor {
    SensorSimulator ss;
    TcpService tcp;

    // robot -> sensor -> connection manager -> rpi -> robot
    public Sensor(TcpService tcp){
        this.tcp = tcp;
        if(tcp == null){
            this.ss = new SensorSimulator();
        }else{
            Variables.SIMULATION = false;
            System.out.println("[Sensor.java] Using real sensor!");
        }
    }


    // Robot sensors marked in the following manner:
    /*
        A/D - B - C
        -   - - - F
        E   - - - -
     */
    // Sensors A, B, C able to detect 3 grid in front of the robot
    // Sensors D, E able to detect 2 grid to the left side of the robot
    // Sensor F able to detect 1 grid to the right side of the robot
    // Gets raw distances from sensor and parse it
    // E.g: x-cm < distance < y-cm = 1 grid ahead,
    // i-cm < distance < j-cm = 2 grid ahead and so on
    // Finally, return distances to objects in terms of no. of grids to caller
    // This needs calibration from arduino

    // Returns distance to object
    public int checkSensorA(Point p, Direction d){
        if(Variables.SIMULATION){
            return ss.getSimulatedSensor_A(p, d);
        }else{
            return parseReading('a') * 10;
        }
    }

    // Returns distance to object
    public int checkSensorB(Point p, Direction d){
        if(Variables.SIMULATION){
            return ss.getSimulatedSensor_B(p, d);
        }else{
            return parseReading('b') * 10;
        }
    }

    // Returns distance to object
    public int checkSensorC(Point p, Direction d){
        if(Variables.SIMULATION){
            return ss.getSimulatedSensor_C(p, d);
        }else{
            return parseReading('c') * 10;
        }
    }

    // Returns distance to object
    public int checkSensorD(Point p, Direction d){
        if(Variables.SIMULATION){
            return ss.getSimulatedSensor_D(p, d);
        }else{
            return parseReading('d') * 10;
        }
    }

    // Returns distance to object
    public int checkSensorE(Point p, Direction d){
        if(Variables.SIMULATION){
            return ss.getSimulatedSensor_E(p, d);
        }else{
            return parseReading('e') * 10;
        }
    }

    // Returns distance to object
    public int checkSensorF(Point p, Direction d){
        if(Variables.SIMULATION){
            return ss.getSimulatedSensor_F(p, d);
        }else{
            return parseReading('f') * 10;
        }
    }

    private int parseReading(char sensor){
        // Get TCP reading
        String input = Variables.SENSOR_QUEUE.peek();
        int i = -1;
        // If first digit is r;"10.12,2,3,4,5,6"
        // W A D
        // and;ANDROID
        // ard;W ard;A ard;D ard;Q ard;E
        // r;10.12,2,3,4,5,6
        String messageType = input.substring(0, 1);
        if(messageType.contains("r")){
            // Split up by commas
            String[] sensorReadings = input.substring(2, input.length()).split(",");
            if(sensor == 'a'){
                i = (int) Math.round(Double.parseDouble(sensorReadings[2]));
                if (i > Variables.SENSOR_FRONT_RANGE){
                    i = Variables.SENSOR_FRONT_RANGE;
                }
            }else if(sensor == 'b'){
                i = (int) Math.round(Double.parseDouble(sensorReadings[1]));
                if (i > Variables.SENSOR_RANGE_B){
                    i = Variables.SENSOR_RANGE_B;
                }
            }else if(sensor == 'c'){
                i = (int) Math.round(Double.parseDouble(sensorReadings[0]));
                if (i > Variables.SENSOR_FRONT_RANGE){
                    i = Variables.SENSOR_FRONT_RANGE;
                }
            }else if(sensor == 'd'){
                i = (int) Math.round(Double.parseDouble(sensorReadings[4]));
                if (i > Variables.SENSOR_RANGE_D){
                    i = Variables.SENSOR_RANGE_D;
                }
            }else if(sensor == 'e'){
                i = (int) Math.round(Double.parseDouble(sensorReadings[3]));
                if (i > Variables.SENSOR_RANGE_E){
                    i = Variables.SENSOR_RANGE_E;
                }
            }else if(sensor == 'f'){
                i = (int) Math.round(Double.parseDouble(sensorReadings[5]));
                if (i > Variables.SENSOR_RANGE_F){
                    i = Variables.SENSOR_RANGE_F;
                }
            }else{
                System.out.println("[Sensor.java] ?");
                return -1;
            }
        }else{
            System.out.println("[WallHugging.java] Not getting sensor readings.");
        }
        return i;
    }
}