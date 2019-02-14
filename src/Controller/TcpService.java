package Controller;

import Model.Variables;
import View.DisplayMapGUI;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class TcpService {

    private static final String RPI_IP_ADDRESS = Variables.RPI_IP_ADDRESS;
    private static final int RPI_PORT = Variables.RPI_PORT;
    private boolean prevReceiver = false;

    private TcpService tcp;

    private static TcpService instance;
    private Socket clientSocket;
    private PrintWriter toRPi;
    private Scanner fromRPi;
    private final int DELAY_IN_SENDING_MESSAGE = 2;
    private static boolean isConnected = false;
    private static int tries = 0;
    private ImageRecognition iR;

    private final int TIME_TO_RETRY = 1000;       //wait for 1 second to retry

    private Exploration exploration;
    public TcpService(){
        connectToHost();
        tcp = this;
        // tcp.sendMessage("ard; M");
        // tcp.sendMessage("ard; IDGAHDRf");
        /*AndroidServiceInterface androidService = RealAndroidService.getInstance();
        RPiServiceInterface rpiService = RealRPiService.getInstance();*/
        int tests = 0;

        try{
            Thread.sleep(500);
            //System.out.println("Message from RPI:" +readMessage());
        }catch (InterruptedException ite){
            System.out.println("Unable to establish connection!");
            //ite.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String cmd = "";
                while(cmd != "KILL") {
                    try {
                        //TimeUnit.SECONDS.sleep(1);
                        TimeUnit.MILLISECONDS.sleep(10);
                        //System.out.println("Reading from RPI....");
                        cmd = readMessage();
                        String parsedCMD = cmdParse(cmd);
                        switch (parsedCMD){
                            case "ex":
                                if (Thread.activeCount() < 5) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //ImageRecognition.resetImages();
                                            //iR = new ImageRecognition();
                                            System.out.println("[TcpService.java] Running exploration!");
                                            exploration = new Exploration(tcp);
                                        }
                                    }).start();
                                }
                                break;
                            case "fp":
                                if(Thread.activeCount() < 10){
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Variables.FASTEST_PATH = true;
                                            System.out.println("[TcpService.java] Running fastest path!");
                                            new Algorithm(exploration.getRobotInstance(), DisplayMapGUI.getWayPoint(), false, false);
                                        }
                                    }).start();
                                }
                                break;
                            case "wp":
                                cmd = cmd.substring(3);
                                String[] stringCMD = cmd.split(",");
                                DisplayMapGUI.setWayPoint(Integer.parseInt(stringCMD[1]),Integer.parseInt(stringCMD[0]));
                                break;
                            case "sp":
                                // CAN WE JUST SET OUR OWN Starting Position ALL THE TIME?
                                // IF SO, THEN WE DON'T NEED THIS.
                                break;
                            case "TURN_LEFT":
                                sendMessage("ard; s");
                                break;
                            case "START_CALI":
                                sendMessage("ard; M");
                                break;
                            case "roboto":
                                // Else, RPI is sending sensor readings to us. We need to parse those.
                                String counterStr = cmd.substring(14);
                                int counter = Integer.parseInt(counterStr);
                                System.out.println("[TcpService.java] Current global counter: "+Variables.counter);
                                System.out.println("[TcpService.java] Current incoming counter: "+counter);
                                if(counter == Variables.counter){
                                    System.out.println("[[###]] [TcpService.java] \tReceived from Arduino: "+cmd+" ###");
                                    Variables.SENSOR_QUEUE.add(cmd);
                                    //if(cmd.substring(16).equals('x')){
                                    //
                                    //}
                                    Variables.counter++;
                                    System.out.println("[TcpService.java] Global counter increased: "+Variables.counter);
                                }else{
                                    System.out.println("[TcpService.java] Ignore!");
                                }
                                break;
                            case "img":
                                System.out.println("[TcpService.java] Img Current global counter: "+Variables.counter);
                                ImageRecce.imageFoundArrayList.add(true);
                                System.out.println("[TcpService.java] Img Current global counter: "+Variables.counter);
                                break;
                        }
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                closeConnection();
            }
        }).start();
    }

    private void connectToHost(){
        // System.out.println("[TcpService.java] Trying to connect...");
        try {
            clientSocket = new Socket(RPI_IP_ADDRESS, RPI_PORT);
            isConnected = true;
            toRPi = new PrintWriter(clientSocket.getOutputStream());
            fromRPi = new Scanner(clientSocket.getInputStream());
        } catch (IOException ioe){
            //System.out.println("[TCP.java] Unable to establish connection! Here");
            isDisconnected();
            try{
                Thread.sleep(TIME_TO_RETRY);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            connectToHost();
        }
        System.out.println("[TcpService.java] RPi successfully connected");
        DisplayMapGUI.openConnection();
    }

    public void closeConnection(){
        try {
            if (!clientSocket.isClosed()){
                clientSocket.close();
            }
        }catch (IOException ioe){
            //ioe.printStackTrace();
            try{
                Thread.sleep(TIME_TO_RETRY);
            }catch (InterruptedException e){
                //e.printStackTrace();
            }
            closeConnection();
        }
        System.out.println("[TcpService.java] Connection closed");
    }

    public static boolean getStatus(){
        return isConnected;
    }

    public void sendMessage(String message){
        try {
            if(!Variables.FASTEST_PATH){
                // From 800 to 200.
                Thread.sleep(100);
            }
            // true for Arduino
            // false for Android
            toRPi.write(message);
            toRPi.flush();
            System.out.println("[TcpService.java] Message sent: >>"+message+"<<");
        } catch (Exception e){
            e.printStackTrace();
            try{
                Thread.sleep(TIME_TO_RETRY);
            }catch (InterruptedException ite){
                ite.printStackTrace();
            }
            connectToHost();
            //sendMessage(message);
        }
    }

    public String readMessage(){
        String messageReceived = "";
        try {
            messageReceived = fromRPi.nextLine();
            messageReceived = messageReceived.replace("\n","");
            System.out.println("[TcpService.java] Message received: >>"+messageReceived+"<<");
        }catch (Exception e){
            //e.printStackTrace();
            /*try{
                Thread.sleep(50);
            }catch (InterruptedException ite){
                //ite.printStackTrace();
            }*/
            connectToHost();
            readMessage();
        }
        return messageReceived;
    }

    public String getConnectionDetails(){
        return RPI_IP_ADDRESS+":"+RPI_PORT;
    }

    private void isDisconnected(){
        if(isConnected)
            DisplayMapGUI.closedConnection();
    }

    private String cmdParse(String cmd){
        if(cmd.equals("r;A")){
            System.out.println("Sending 's' to Arduino.");
            return "TURN_LEFT";
        }else if(cmd.equals("r;W")){
            System.out.println("Sending 'W' to Arduino");
            return "START_CALI";
        }else if (cmd.contains("EX_START")){
            return "ex";
        }else if(cmd.contains("FP_START")){
            return "fp";
        }else if(cmd.contains("wp")){
            return "wp";
        }else if(cmd.contains("sp")){
            return cmd;
        }else if(cmd.contains("r")){
            return "roboto";
        }else if(cmd.contains("img")){
            return "img";
        }
        return "nop";
    }
}

