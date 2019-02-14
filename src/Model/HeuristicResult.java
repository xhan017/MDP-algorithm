package Model;

public class HeuristicResult {

    private final int noOfTurns;
    private final Direction endDirection;

    public HeuristicResult(int noOfTurns, Direction endDirection){
        this.noOfTurns = noOfTurns;
        this.endDirection = endDirection;
    }

    public int getNoOfTurns(){
        return noOfTurns;
    }

    public Direction getEndDirection() {
        return endDirection;
    }
}
