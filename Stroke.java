import java.util.ArrayList;

// Class for a free-form ink stroke object
public class Stroke extends Ink {

    ArrayList<Point> pointList = new ArrayList<Point>();

    String color = "";

    public Stroke(Point p) {
        pointList.add(p);
        pointList.add(p);
        this.type = "stroke";
        this.color = "black";
    }

    public ArrayList<Point> getPointList() {
        return this.pointList;
    }

    public void addToPointList(Point p) {
        pointList.add(p);
    }

    public void translate(int dx, int dy) {
        for (int i = 0; i < pointList.size(); i++) {
            int currX = pointList.get(i).getX();
            int currY = pointList.get(i).getY();
            pointList.set(i, new Point(currX + dx, currY + dy));
        }
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return this.color;
    }

}