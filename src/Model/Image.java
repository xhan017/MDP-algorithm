package Model;

import java.awt.*;

public class Image {
    Point p;
    Direction d;
    private String obstacleFace;

    Image(Point p, Direction d, String obstacleFace){
        this.p = p;
        this.d = d;
        this.obstacleFace = obstacleFace;
    }

    public Point getP() {
        return p;
    }

    public Direction getD() {
        return d;
    }

    public String getObstacleFace() {
        return obstacleFace;
    }
}
