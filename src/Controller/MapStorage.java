package Controller;

import Model.Direction;
import Model.Image;
import View.DisplayMapGUI;

import java.awt.*;
import java.io.*;


public class MapStorage {

    public static String mdfString = null;
    public static Direction currentDirection;
    public static Point currentLocation;

    public static String readMap(int type, String name) {
        File file;
        if(type == 1){
            file = new File(name+".explore");
        }else{
            file = new File(name+".obstacles");
        }
        String st, ret = "";
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((st = br.readLine()) != null) {
                ret += st;
            }
        }catch(IOException e){
            System.out.println("Error! "+e);
        }
        return ret;
    }

    public static String storeMap(char[][] map){
        StringBuilder originalMap = new StringBuilder();
        StringBuilder robot = new StringBuilder();
        StringBuilder p1 = new StringBuilder();
        StringBuilder p2 = new StringBuilder();
        StringBuilder image = new StringBuilder();

        robot.append((int)currentLocation.getY());
        robot.append(",");
        robot.append((int)currentLocation.getX());
        robot.append(",");
        robot.append(currentDirection.toString());

        char[][] parsedMap = mapParser(map);

        originalMap.append('1');
        originalMap.append('1');
        for(char[] c1 : parsedMap){
            for(char c2 : c1){
                originalMap.append(c2);
            }
        }
        originalMap.append('1');
        originalMap.append('1');

        //padding front 11
        p1.append('1');
        p1.append('1');
        for(char[] c1 : parsedMap){
            for(char c2 : c1){
                if(c2 == 'x')
                    c2 = '1';
                p1.append(c2);
            }
        }
        //padding back 11
        p1.append('1');
        p1.append('1');

        //padding front 0000
        p2.append('0');
        p2.append('0');
        p2.append('0');
        p2.append('0');
        for(char[] c1 : parsedMap){
            for(char c2 : c1){
                if(c2 == '0')
                    continue;
                if(c2 == '1')
                    c2 = '0';
                if(c2 == 'x')
                    c2 = '1';
                p2.append(c2);
            }
        }

        //padding for the unexplored areas (if any)
        while(p2.length() < p1.length()){
            p2.append('0');
        }

        if(ImageRecce.imageArrayList.size() != 0) {
            for(Image im : ImageRecce.imageArrayList){
                if((int) im.getP().getY() < 0 || (int) (19 - im.getP().getX())<0){
                    continue;
                }
                image.append((int) im.getP().getY());
                image.append(",");
                image.append((int) (19 - im.getP().getX()));
                image.append(",");
                image.append(im.getObstacleFace());
                image.append(";");
            }
        }else{
            image.append("null");
        }

        /*
        System.out.println("Original: " + originalMap.toString());
        System.out.println("P1: " + p1.toString());
        System.out.println("P1HEX: " + binToHexConverter(p1.toString()));
        System.out.println("P2: " + p2.toString());
        System.out.println("P2HEX: " + binToHexConverter(p2.toString()));
        */

        String fullMDFString = robot.toString() + " " +
                binToHexConverter(p1.toString()) + " " +
                binToHexConverter(p2.toString()) + " " +
                image.toString() + "\n";

        // todo MAYBE NEED \n ???? for RPI

        return mdfString = fullMDFString;
    }

    public static char[][] mapParser(char[][] rawMap){
        char[][] parsedMap = new char[20][15];
        for(int i = 0; i <= 19; i++){
            for(int j = 0; j <= 14; j++){
                parsedMap[19-i][j] = rawMap[i][j];
            }
        }
        return parsedMap;
    }

    private static String binToHexConverter(String bin){
        StringBuilder hexString = new StringBuilder();
        StringBuilder tempBuilder = new StringBuilder();
        String binValue, hexValue;
        char[] pChars = bin.toCharArray();
        if(pChars[0] == '0'){ // p2
            int count = 4;
            for (char c : pChars) {
                if(count > 0) {
                    count--;
                    continue;
                }
                tempBuilder.append(c);
                if (tempBuilder.length() == 4) {
                    binValue = tempBuilder.toString();
                    hexValue = binToHex(binValue);
                    hexString.append(hexValue);

                    //Re-Initialize
                    tempBuilder = new StringBuilder();
                }
            }
            return hexString.toString();
        }else if(pChars[0] == 'x'){
            int count = 1;
            for(char c : pChars){
                if(count > 0){
                    count--;
                    continue;
                }
                tempBuilder.append(c);
                if (tempBuilder.length() == 4) {
                    binValue = tempBuilder.toString();
                    hexValue = binToHex(binValue);
                    hexString.append(hexValue);

                    //Re-Initialize
                    tempBuilder = new StringBuilder();
                }
            }
            return hexString.toString();
        } else { // p1
            for (char c : pChars) {
                tempBuilder.append(c);
                if (tempBuilder.length() == 4) {
                    binValue = tempBuilder.toString();
                    hexValue = binToHex(binValue);
                    hexString.append(hexValue);

                    //Re-Initialize
                    tempBuilder = new StringBuilder();
                }
            }
            return hexString.toString();
        }
    }

    private static String binToHex(String bin) {
        switch (bin) {
            case "0000":
                return "0";
            case "0001":
                return "1";
            case "0010":
                return "2";
            case "0011":
                return "3";
            case "0100":
                return "4";
            case "0101":
                return "5";
            case "0110":
                return "6";
            case "0111":
                return "7";
            case "1000":
                return "8";
            case "1001":
                return "9";
            case "1010":
                return "A";
            case "1011":
                return "B";
            case "1100":
                return "C";
            case "1101":
                return "D";
            case "1110":
                return "E";
            case "1111":
                return "F";
            default:
                return "0";
        }
    }
}
