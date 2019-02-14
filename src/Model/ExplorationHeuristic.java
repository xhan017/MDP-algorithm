package Model;

import java.awt.*;

public class ExplorationHeuristic implements Comparable<ExplorationHeuristic>{
    Grid g;
    int hVal;

    public ExplorationHeuristic(Grid g, Grid startGrid, Direction d){
        this.hVal = Math.abs(g.getPosition().x - startGrid.getPosition().x) + Math.abs(g.getPosition().y - startGrid.getPosition().y);
        this.g = g;
    }

    public int gethVal(){
        return hVal;
    }

    public Point getPosition(){
        return g.getPosition();
    }

    public Grid getGrid(){
        return g;
    }

    public void updateHeuristics(Grid g, Grid startGrid, Direction d){
        this.hVal = Math.abs(g.getPosition().x - startGrid.getPosition().x) + Math.abs(g.getPosition().y - startGrid.getPosition().y);

        this.g = g;
    }

    @Override
    public int compareTo(ExplorationHeuristic o) {
        int chVal = ((ExplorationHeuristic) o).gethVal();
        if(this.hVal < chVal){
            return -1;
        }else if(this.hVal == chVal){
            return 0;
        }else{
            return 1;
        }
    }
}
