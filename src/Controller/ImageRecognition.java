package Controller;

import Model.Variables;
import View.DisplayMapGUI;

import java.awt.*;
import java.util.ArrayList;

public class ImageRecognition {

    private static ArrayList<Point> imageLocationArrayList = new ArrayList<>();
    public static ArrayList<Point> imageFoundArrayList = new ArrayList<>();
    private static char[][] mapArray = new char[20][15];
    private static ArrayList<String> obstacleLocations = new ArrayList<>();
    private static int imageCount = 0;

    public ImageRecognition(){
        String s = MapStorage.readMap(2, Variables.INPUT_FILE_NAME);
        char[] temp = s.toCharArray();

        // Remove buffer bits 0000 from Map
        int bufferCount = 4;
        StringBuffer temp1 = new StringBuffer();
        for(char c : temp){
            if(bufferCount > 0){
                bufferCount--;
                continue;
            }
            temp1.append(c);
        }

        // Format Map into char[][] array
        temp = temp1.toString().toCharArray();
        int counter = 0;
        for(int i = 0; i < 20; i++){
            for(int j = 0; j < 15; j++){
                mapArray[i][j] = temp[counter];
                counter++;
            }
        }

        // Flip Map for proper obstacle Coordinates
        mapArray = MapStorage.mapParser(mapArray);
        for(int i = 0; i < 20; i++){
            for(int j = 0; j < 15; j++){
                if(mapArray[i][j] == '1') {
                    // j = take 19 - that shit
                    obstacleLocations.add(i + "-" + j);
                }
            }
        }

        // Set the number of Images in the Map
        imageCount = (int) (Math.random() * obstacleLocations.size());

        // Set Image(s) on a random Obstacle
        int tempCount = imageCount;
        while(tempCount > 0){
            int randomInt = (int) (Math.random() * obstacleLocations.size());
            String[] randomString = obstacleLocations.get(randomInt).split("-");
            imageLocationArrayList.add(new Point(Integer.parseInt(randomString[1]),Integer.parseInt(randomString[0])));
            obstacleLocations.remove(randomInt);
            tempCount--;
        }
        System.out.println("Total images : " + imageLocationArrayList.size());
    }

    public static ArrayList<Point> getImageLocationArrayList() {
        return imageLocationArrayList;
    }

    public static void setImageLocationArrayList(ArrayList<Point> imageLocationArrayList) {
        ImageRecognition.imageLocationArrayList = imageLocationArrayList;
    }

    public static int getImageCount(){
        return imageCount;
    }

    public static void resetImages(){
        DisplayMapGUI.isImageFound = false;
        obstacleLocations = new ArrayList<>();
        imageLocationArrayList = new ArrayList<>();
        imageFoundArrayList = new ArrayList<>();
        imageCount = 0;
    }
}
