import java.util.ArrayList;

// Class for a rectangle ink object
public class RectangleInk extends Ink {

    Point mouseStart;
    Point mouseEnd;

    String color = "";

    public RectangleInk(Point mouseStart, Point mouseEnd) {
        this.mouseStart = mouseStart;
        this.mouseEnd = mouseEnd;
        this.type = "rectangle";
        this.color = "black";
    }

    public Point getMouseStart() {
        return this.mouseStart;
    }

    public Point getMouseEnd() {
        return this.mouseEnd;
    }

    public void translate(int dx, int dy) {
        int currStartX = mouseStart.getX();
        int currStartY = mouseStart.getY();
        int currEndX = mouseEnd.getX();
        int currEndY = mouseEnd.getY();
        this.mouseStart = new Point(currStartX + dx, currStartY + dy);
        this.mouseEnd = new Point(currEndX + dx, currEndY + dy);
    }

    public Point setMouseEnd() {
        return this.mouseEnd;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return this.color;
    }

}