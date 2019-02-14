package View;

import Controller.*;
import Model.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.TimeUnit;

public class DisplayMapGUI {

    private JFrame mainWindow;
    private JPanel gridPanel;
    private static JButton[][] gridArray = new JButton[20][15];
    private JButton clearButton, plusButton1, minusButton1, plusButton2, minusButton2, terminateButton, exploreButton, fastestPathButton;
    private JRadioButton speedLabel, percentLabel, timeLabel;
    private JTextField speedField, percentField;
    private SpinnerModel minModel, secModel;
    private JSpinner minSpinner, secSpinner;
    public static int wayPointX, wayPointY;
    private Exploration exploration = null;
    private JLabel connectionStatus;
    private static Point waypoint;
    private static JProgressBar progressBar;
    private static JProgressBar statusBar;
    private TcpService tcp = null;
    public static boolean isImageFound = false;
    private ImageRecognition iR;
    private String fullPath = "";
    public static JLabel timerLabel;
    private JButton viewLogButton;
    private JFrame logViewerFrame;
    public JTextArea sentToArduinoPanel;
    public JTextArea sentToAndroidPanel;


    public DisplayMapGUI(String name){
        UIManager.put("ProgressBar.selectionForeground", Color.black);
        UIManager.put("ProgressBar.selectionBackground", Color.black);

        init();
        System.out.println("[DisplayMapGUI.java] Establishing TCP connection...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                progressBar.setIndeterminate(true);
                progressBar.setBackground(Color.WHITE);
                progressBar.setString("Trying to connect to: "+ Variables.RPI_IP_ADDRESS+ ":"+Variables.RPI_PORT);
                tcp = new TcpService();
                progressBar.setIndeterminate(false);
                progressBar.setString("Connected to: "+Variables.RPI_IP_ADDRESS+":"+Variables.RPI_PORT);
                progressBar.setBackground(new Color(121, 189, 143));
            }
        }).start();


        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WallHugging.manualTermination();
                Variables.STOP_ROBOT = true;
                try{
                    TimeUnit.MILLISECONDS.sleep(500);
                }catch (InterruptedException ex){
                    ex.printStackTrace();
                }
                for(int i=0;i<20;i++){
                    for(int j=0;j<15;j++){
                        clearGridPos(i,j);
                    }
                }
                exploration = null;
                Variables.COMPLETED_EXPLORATION = false;
                Variables.COMPLETED_FP = false;
            }
        });

        terminateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WallHugging.manualTermination();
                Variables.STOP_ROBOT = true;
                exploration = null;
                Variables.COMPLETED_EXPLORATION = false;
                Variables.COMPLETED_FP = false;
            }
        });

        exploreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(speedLabel.isSelected()) {
                    WallHugging.percentageLimitOn = false;
                    WallHugging.timeLimitOn = false;
                    Variables.stepsPerSec = (Integer.parseInt(speedField.getText()));
                }
                if(percentLabel.isSelected()){
                    WallHugging.percentageLimitOn = true;
                    WallHugging.timeLimitOn = false;
                    Variables.stepsPerSec =  (Integer.parseInt(speedField.getText()));
                    WallHugging.selectPercent(Integer.parseInt(percentField.getText()));
                }
                if(timeLabel.isSelected()){
                    WallHugging.percentageLimitOn = false;
                    WallHugging.timeLimitOn = true;
                    Variables.stepsPerSec = (Integer.parseInt(speedField.getText()));
                    int totalTime = Integer.parseInt(((JSpinner.DefaultEditor) minSpinner.getEditor()).getTextField().getText())*60
                            + Integer.parseInt(((JSpinner.DefaultEditor) secSpinner.getEditor()).getTextField().getText());
                    if(totalTime == 0){
                        JOptionPane.showMessageDialog(mainWindow,"Time Limit cannot be set to 0.","Alert", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    WallHugging.selectTime(totalTime);
                }
                if(speedLabel.isSelected() || percentLabel.isSelected() || timeLabel.isSelected()) {
                    Variables.COMPLETED_EXPLORATION = false;
                    Variables.COMPLETED_FP = false;
                    if (Thread.activeCount() < 7) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("[DisplayMapGUI.java] Running exploration!");
                                exploration = new Exploration(tcp);
                                waypoint = null;
                            }
                        }).start();
                    }
                } else {
                    JOptionPane.showMessageDialog(mainWindow,"Please select a modifier.","Alert",JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        fastestPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(Thread.activeCount() < 7){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(exploration == null) {
                                JOptionPane.showMessageDialog(mainWindow, "Please run exploration first.", "Alert", JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            if(!Variables.COMPLETED_EXPLORATION){
                                JOptionPane.showMessageDialog(mainWindow,"Map not fully explored.","Alert",JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            if(Variables.COMPLETED_FP){
                                JOptionPane.showMessageDialog(mainWindow,"Fastest Path already finished running.","Alert",JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            if(fastestPathButton.getText().matches("RACE")) {
                                System.out.println("[DisplayMapGUI.java] Running fastest path!");
                                if(getWayPoint() == null){
                                    JOptionPane.showMessageDialog(mainWindow, "Set waypoint first!");
                                }else{
                                    new Algorithm(exploration.getRobotInstance(), getWayPoint(), false, false);
                                }
                            }
                        }
                    }).start();
                }

            }
        });

        plusButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int speedValue = Integer.parseInt(speedField.getText());
                // Set limit to 50 steps/sec
                if(speedValue == 50)
                    return;
                speedValue++;
                speedField.setText(Integer.toString(speedValue));
            }
        });

        minusButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int speedValue = Integer.parseInt(speedField.getText());
                if(speedValue == 1)
                    return;
                speedValue--;
                speedField.setText(Integer.toString(speedValue));
            }
        });

        plusButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int percentValue = Integer.parseInt(percentField.getText());
                if(percentValue == 100)
                    return;
                percentValue++;
                percentField.setText(Integer.toString(percentValue));
            }
        });

        minusButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int percentValue = Integer.parseInt(percentField.getText());
                // Minimum exploration is 5%
                if(percentValue == 5)
                    return;
                percentValue--;
                percentField.setText(Integer.toString(percentValue));
            }
        });

        viewLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logViewerFrame.setVisible(true);
            }
        });

        // Sets look and feel of ui
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        // Closes the program when windows exit button is clicked
        mainWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        mainWindow.setLayout(null);
        mainWindow.setResizable(false);
        mainWindow.setVisible(true);
    }

    public static void updateObstacles(int x, int y){
        //System.out.println("[GUI] Updating obstacle: "+ x + " "+y);
        gridArray[x][y].setBackground(Color.BLACK);
    }

    public static void updateExplored(int x, int y){
        //System.out.println("[GUI] Updating: "+ x + " "+y);
        gridArray[x][y].setBackground(Color.green);
    }

    public static void updateUnexplored(int x, int y){
        gridArray[x][y].setBackground(Color.BLACK);
    }

    public static void showExplorationAvailableGrids(int x, int y){
        gridArray[x][y].setBackground(Color.ORANGE);
    }

    public static void updateRobotPosition(int x, int y){
        gridArray[x][y].setBackground(Color.YELLOW);
    }

    public static void updateRobotDirection(int x, int y){
        gridArray[x][y].setBackground(Color.BLUE);
    }

    public static void clearGridPos(int x, int y){
        gridArray[x][y].setBackground(Color.LIGHT_GRAY);
    }

    public static void showAvailableGrids(int x, int y){ gridArray[x][y].setBackground(Color.BLUE); }

    public static void refreshGrid(Map m, Point currentPosition, Direction d){
        for(int i = 0; i < 20; i++){
            for(int j = 0; j < 15; j++){
                if(m.isExplored(i, j)) {
                    gridArray[i][j].setBackground(Color.green);
                } else if(m.hasObstacle(i, j)) {
                    if(ImageRecce.imagePointArrayList.size() != 0){
                        if(ImageRecce.imagePointArrayList.contains(new Point(i, j))){
                            gridArray[i][j].setBackground(Color.RED);
                        }else{
                            gridArray[i][j].setBackground(Color.BLACK);
                        }
                    }else{
                        gridArray[i][j].setBackground(Color.BLACK);
                    }
                }else{
                    gridArray[i][j].setBackground(Color.LIGHT_GRAY);
                }
            }
        }
        updateRobotPosition(currentPosition.x, currentPosition.y);
        updateRobotPosition(currentPosition.x-1, currentPosition.y);
        updateRobotPosition(currentPosition.x+1, currentPosition.y);
        updateRobotPosition(currentPosition.x, currentPosition.y-1);
        updateRobotPosition(currentPosition.x, currentPosition.y+1);
        updateRobotPosition(currentPosition.x-1, currentPosition.y+1);
        updateRobotPosition(currentPosition.x-1, currentPosition.y-1);
        updateRobotPosition(currentPosition.x+1, currentPosition.y-1);
        updateRobotPosition(currentPosition.x+1, currentPosition.y+1);

        switch(d){
            case W:
                DisplayMapGUI.updateRobotDirection(currentPosition.x-1, currentPosition.y-1);
                DisplayMapGUI.updateRobotDirection(currentPosition.x-1, currentPosition.y);
                DisplayMapGUI.updateRobotDirection(currentPosition.x-1, currentPosition.y+1);
                break;
            case A:
                DisplayMapGUI.updateRobotDirection(currentPosition.x+1, currentPosition.y-1);
                DisplayMapGUI.updateRobotDirection(currentPosition.x, currentPosition.y-1);
                DisplayMapGUI.updateRobotDirection(currentPosition.x-1, currentPosition.y-1);
                break;
            case S:
                DisplayMapGUI.updateRobotDirection(currentPosition.x+1, currentPosition.y-1);
                DisplayMapGUI.updateRobotDirection(currentPosition.x+1, currentPosition.y);
                DisplayMapGUI.updateRobotDirection(currentPosition.x+1, currentPosition.y+1);
                break;
            case D:
                DisplayMapGUI.updateRobotDirection(currentPosition.x+1, currentPosition.y+1);
                DisplayMapGUI.updateRobotDirection(currentPosition.x, currentPosition.y+1);
                DisplayMapGUI.updateRobotDirection(currentPosition.x-1, currentPosition.y+1);
                break;
        }
    }

    // Try to fill the grid with panels
    private void fillGrid(){
        for(int i = 0; i < 20; i++){
            for(int j = 0; j < 15; j++) {
                gridArray[i][j] = new JButton();
                gridArray[i][j].setToolTipText(i + " " + j);
                gridArray[i][j].setBounds(i * 20, j * 20, 20, 20);
                gridArray[i][j].setBackground(Color.LIGHT_GRAY);
                gridPanel.add(gridArray[i][j]);
                final int temp_i = i;
                final int temp_j = j;
                gridArray[i][j].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(isWayPointValid(temp_i,temp_j)) {
                            for (int i = 0; i < 20; i++) {
                                for (int j = 0; j < 15; j++) {
                                    if (gridArray[i][j].getBackground() == Color.MAGENTA)
                                        removeWayPoint(i, j);
                                }
                            }
                            setWayPoint(temp_i, temp_j);
                        }else if(gridArray[temp_i][temp_j].getBackground() == Color.MAGENTA) {
                            removeWayPoint(temp_i, temp_j);
                        }
                    }
                });
            }
        }
    }

    public static void openConnection(){
        progressBar.setIndeterminate(false);
        progressBar.setString("Connected to: "+Variables.RPI_IP_ADDRESS+":"+Variables.RPI_PORT);
        progressBar.setBackground(new Color(121, 189, 143));
    }

    public static void closedConnection(){
        progressBar.setBackground(new Color(235, 97, 56));
        progressBar.setString("Disconnected!");
        System.out.println("[DisplayMapGUI.java] Connection closed by remote.");
    }

    public static void setStatusBar(String s, Color c){
        statusBar.setBackground(c);
        statusBar.setString(s);
    }

    private void init(){
        mainWindow = new JFrame("S A L L Y");

        clearButton = new JButton("CLEAR");
        plusButton1 = new JButton("+");
        minusButton1 = new JButton("-");
        plusButton2 = new JButton("+");
        minusButton2 = new JButton("-");
        terminateButton = new JButton("TERMINATE");
        exploreButton = new JButton("EXPLORE");
        fastestPathButton = new JButton("RACE");
        viewLogButton = new JButton("View Logs");

        speedLabel = new JRadioButton("X Steps/sec");
        speedLabel.setSelected(true);
        percentLabel = new JRadioButton("X% Exploration Target");
        timeLabel = new JRadioButton("X min: YY sec Limit");
        ButtonGroup bg = new ButtonGroup();
        bg.add(speedLabel);bg.add(percentLabel);bg.add(timeLabel);

        connectionStatus = new JLabel("Not connected");
        connectionStatus.setBackground(Color.LIGHT_GRAY);

        logViewerFrame = new JFrame("Logs");
        logViewerFrame.setLayout(new GridLayout(1, 2, 0, 20));
        logViewerFrame.setSize(500, 700);

        sentToArduinoPanel = new JTextArea();
        sentToArduinoPanel.setSize(120, 570);
        sentToArduinoPanel.setEditable(false);
        JScrollPane ardPnl = new JScrollPane(sentToArduinoPanel);
        ardPnl.setBorder(BorderFactory.createTitledBorder( BorderFactory.createLineBorder(Color.black), "Arduino"));

        sentToAndroidPanel = new JTextArea();
        sentToAndroidPanel.setSize(120, 570);
        sentToAndroidPanel.setEditable(false);
        JScrollPane andPnl = new JScrollPane(sentToAndroidPanel);
        andPnl.setBorder(BorderFactory.createTitledBorder( BorderFactory.createLineBorder(Color.black), "Android"));


        logViewerFrame.add(ardPnl);
        logViewerFrame.add(andPnl);
        logViewerFrame.setResizable(false);


        speedField = new JTextField();
        speedField.setText("20");
        speedField.setHorizontalAlignment(SwingConstants.CENTER);
        speedField.setEditable(false);
        percentField = new JTextField();
        percentField.setText("5");
        percentField.setHorizontalAlignment(SwingConstants.CENTER);
        percentField.setEditable(false);

        minModel = new SpinnerNumberModel(0,0,10,1);
        secModel = new SpinnerNumberModel(0,0,60,1);
        minSpinner = new JSpinner(minModel);
        ((JSpinner.DefaultEditor) minSpinner.getEditor()).getTextField().setEditable(false);
        secSpinner = new JSpinner(secModel);
        ((JSpinner.DefaultEditor) secSpinner.getEditor()).getTextField().setEditable(false);

        gridPanel = new JPanel();
        gridPanel.setSize(408,400);
        gridPanel.setLayout(new GridLayout(20,15));
        fillGrid();
        mainWindow.add(gridPanel);
        mainWindow.setSize(408, 700);

        speedLabel.setBounds(10,405,170,20);
        plusButton1.setBounds(10,435,50,20);
        speedField.setBounds(65,435,60,20);
        minusButton1.setBounds(130,435,50,20);
        percentLabel.setBounds(10,465,170,20);
        plusButton2.setBounds(10,495,50,20);
        percentField.setBounds(65,495,60,20);
        minusButton2.setBounds(130,495,50,20);
        timeLabel.setBounds(10,525,170,20);
        minSpinner.setBounds(10,555,50,30);
        secSpinner.setBounds(80,555,50,30);

        clearButton.setBounds(210,410,180,20);
        terminateButton.setBounds(210,440,180,20);
        exploreButton.setBounds(210,470,180,50);
        fastestPathButton.setBounds(210,530,180,20);
        viewLogButton.setBounds(210,560,180,20);

        //connectionStatus.setBounds();

        timerLabel = new JLabel();
        timerLabel.setText("S:MMM seconds");
        timerLabel.setBounds(280,585,178,20);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setBounds(10,640,378,20);

        statusBar = new JProgressBar(0, 100);
        statusBar.setValue(0);
        statusBar.setString("Idle... ");
        statusBar.setStringPainted(true);
        statusBar.setBounds(10,610,378,20);

        mainWindow.add(clearButton);
        mainWindow.add(speedLabel);
        mainWindow.add(plusButton1);
        mainWindow.add(speedField);
        mainWindow.add(minusButton1);
        mainWindow.add(percentLabel);
        mainWindow.add(plusButton2);
        mainWindow.add(percentField);
        mainWindow.add(minusButton2);
        mainWindow.add(timeLabel);
        mainWindow.add(minSpinner);
        mainWindow.add(secSpinner);
        mainWindow.add(terminateButton);
        mainWindow.add(exploreButton);
        mainWindow.add(fastestPathButton);
        mainWindow.add(viewLogButton);
        mainWindow.add(timerLabel);
        mainWindow.add(progressBar);
        mainWindow.add(statusBar);
    }

    public static void setWayPoint(int x,int y){
        gridArray[x][y].setBackground(Color.MAGENTA);
        wayPointX = x;
        wayPointY = y;
        waypoint = new Point(x, y);
    }

    public static Point getWayPoint(){
        return waypoint;
    }

    private void removeWayPoint(int x, int y){
        gridArray[x][y].setBackground(Color.GREEN);
    }

    public static void showSecondaryExploration(int x, int y){
        gridArray[x][y].setBackground(Color.CYAN);
    }

    public static void highlightGrid(Grid g, Color c){
        gridArray[g.getPosition().x][g.getPosition().y].setBackground(c);
    }

    public static void setGridText(Grid g, String s){
        gridArray[g.getPosition().x][g.getPosition().y].setToolTipText(s);
    }

    public boolean isWayPointValid(int i, int j){
        if(Variables.COMPLETED_EXPLORATION){
            if(gridArray[i][j].getBackground() == Color.GREEN){
                if((Map.isValid(i-1,j-1) && ((gridArray[i-1][j-1].getBackground() == Color.GREEN ) || (gridArray[i-1][j-1].getBackground() == Color.MAGENTA ))) &&
                        (Map.isValid(i-1,j) && ((gridArray[i-1][j].getBackground() == Color.GREEN ) || (gridArray[i-1][j].getBackground() == Color.MAGENTA ))) &&
                        (Map.isValid(i-1,j+1) && ((gridArray[i-1][j+1].getBackground() == Color.GREEN ) || (gridArray[i-1][j+1].getBackground() == Color.MAGENTA ))) &&
                        (Map.isValid(i,j+1) && ((gridArray[i][j+1].getBackground() == Color.GREEN ) || (gridArray[i][j+1].getBackground() == Color.MAGENTA ))) &&
                        (Map.isValid(i+1,j+1) && ((gridArray[i+1][j+1].getBackground() == Color.GREEN ) || (gridArray[i+1][j+1].getBackground() == Color.MAGENTA ))) &&
                        (Map.isValid(i+1,j) && ((gridArray[i+1][j].getBackground() == Color.GREEN ) || (gridArray[i+1][j].getBackground() == Color.MAGENTA ))) &&
                        (Map.isValid(i+1,j-1) && ((gridArray[i+1][j-1].getBackground() == Color.GREEN ) || (gridArray[i+1][j-1].getBackground() == Color.MAGENTA ))) &&
                        (Map.isValid(i,j-1) && ((gridArray[i][j-1].getBackground() == Color.GREEN ) || (gridArray[i][j-1].getBackground() == Color.MAGENTA )))){
                    return true;
                }
            }
        }
        return false;
    }
    // todo Load map & Store map

}
