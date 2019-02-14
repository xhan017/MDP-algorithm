package Controller;
import Model.*;
import Model.Robot;
import TestPackage.SensorSimulator;
import View.*;
import Model.Variables;

import java.awt.*;
import java.util.ArrayList;

public class Exploration {
    Robot r;
    Map m;

    public Exploration(TcpService tcp){
        // Load maps first
        String s = Controller.MapStorage.readMap(1, Variables.INPUT_FILE_NAME);
        System.out.println(s);

        Stopwatch sw = new Stopwatch();
        sw.start();

        if(!tcp.getStatus()){
            System.out.println("[Exploration.java] Unable to establish connection through TCP.");
            tcp = null;
        }

        // Initialize robot
        this.r = new Robot(new Point(18, 1), Direction.W,  tcp);
        WallHugging wh = new WallHugging(r);
        wh.start();
        Variables.TIMER_STOP = true;
    }

    public Robot getRobotInstance(){
        return r;
    }


}
