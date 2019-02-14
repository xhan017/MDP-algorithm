package Model;

import java.awt.*;

public class Grid implements Comparable<Grid>{
    Point p;
    Grid parent;
    int noOfTurns;
    Direction d;
    boolean traversedOver;
    boolean hasObstacle;

    public Grid(int x, int y){
        this.p = new Point(x, y);
        noOfTurns = 0;
    }

    // ==Start== For phantom block detection
    public void setTraversedOver(boolean s){
        this.traversedOver = s;
    }

    public boolean getTraversedOver(){
        return traversedOver;
    }

    public void setHasObstacle(boolean s){
        this.hasObstacle = s;
    }

    public boolean getHasObstacle(){
        return hasObstacle;
    }
    // ==End==

    public Point getPosition(){
        return p;
    }

    public void setParent(Grid parentGrid){
        parent = parentGrid;
    }

    public int getNoOfTurns(){
        return noOfTurns;
    }

    public void setNoOfTurns(int i){
        this.noOfTurns = i;
    }

    public void addNoOfTurns(int i){
        noOfTurns+=i;
    }

    public void setDirection(Direction d){
        this.d = d;
    }

    public Direction getDirection(){
        return d;
    }

    public Grid getParent(){
        return parent;
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject)
            return true;
        if (anotherObject == null)
            return false;
        if (getClass() != anotherObject.getClass())
            return false;
        Grid other = (Grid) anotherObject;
        if (p.x != other.getPosition().x)
            return false;
        if (p.y != other.getPosition().y)
            return false;
        if (d != null){
            if(d != other.getDirection())
                return false;
        }
        return true;
    }

    @Override
    public int compareTo(Grid g) {
        int moves = ((Grid) g).getNoOfTurns();
        if(this.noOfTurns < moves){
            return -1;
        }else if(this.noOfTurns == moves){
            return 0;
        }else{
            return 1;
        }

    }
}