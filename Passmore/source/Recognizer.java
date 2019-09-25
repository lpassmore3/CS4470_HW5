// This class is for the Recognizer class
// to recognize command gestures on the right
// side of the application.
//
// Author: Luke Austin Passmore, lpassmore3

import java.util.ArrayList;
import java.util.regex.*;

public class Recognizer {

    //======= The Constants for Directions: =======//
    // N = North        NORTHISH = [N, A, D]
    // A = Northeast    NORTHEASTISH = [A, N, E]
    // E = East         EASTISH = [E, A, B]
    // B = Southeast    SOUTHEASTISH = [B, E, S]
    // S = South        SOUTHISH = [S, B, C]
    // C = Southwest    SOUTHWESTISH = [C, S, W]
    // W = West         WESTISH = [W, C, D]
    // D = Northwest    NORTHWESTISH = [D, N, W]

    // Templates for gestures
    String[] RIGHT_ANGLE = {"SOUTHEASTISH", "WESTISH"};
    String[] LEFT_ANGLE = {"SOUTHWESTISH", "SOUTHEASTISH"};
    String[] PIGTAIL = {"SOUTHISH", "EASTISH", "NORTHISH", "WESTISH", "SOUTHISH"};
    String[] LOOP_1 = {"SOUTHWESTISH", "SOUTHEASTISH", "NORTHEASTISH", "NORTHWESTISH"};
    String[] LOOP_2 = {"SOUTHEASTISH", "SOUTHWESTISH", "NORTHWESTISH", "NORTHEASTISH"};

    Pattern rightAnglePattern;
    Pattern leftAnglePattern;
    Pattern pigtailPattern;
    Pattern loopPattern1;
    Pattern loopPattern2;

    public Recognizer() {
        this.rightAnglePattern = createPattern(RIGHT_ANGLE);
        this.leftAnglePattern = createPattern(LEFT_ANGLE);
        this.pigtailPattern = createPattern(PIGTAIL);
        this.loopPattern1 = createPattern(LOOP_1);
        this.loopPattern2 = createPattern(LOOP_2);
    }

    // Builds a direction vector from a passed-in gesture stroke by
    // comparing it to stored gesture patterns.
    public String recognize(Stroke s) {

        String dirVect = buildDirectionVector(s);
        System.out.println(dirVect);

        Boolean matchedRightAngle = rightAnglePattern.matcher(dirVect).find();
        Boolean matchedLeftAngle = leftAnglePattern.matcher(dirVect).find();
        Boolean matchedPigtail = pigtailPattern.matcher(dirVect).find();
        Boolean matchedLoop1 = loopPattern1.matcher(dirVect).find();
        Boolean matchedLoop2 = loopPattern2.matcher(dirVect).find();

        // System.out.println(matchedRightAngle);
        // System.out.println(matchedLeftAngle);
        // System.out.println(matchedPigtail);
        // System.out.println(matchedLoop);

        if (matchedRightAngle) {
            return "RIGHT_ANGLE";
        } else if (matchedLeftAngle) {
            return "LEFT_ANGLE";
        } else if (matchedPigtail) {
            return "PIGTAIL";
        } else if (matchedLoop1 || matchedLoop2) {
            return "LOOP";
        } else {
            return "NULL_GESTURE";
        }

    }

    // Creates a direction vector for the gesture stroke by
    // iterating through points and setting a direction for
    // each point segment.
    private String buildDirectionVector(Stroke s) {

        StringBuilder directionVector = new StringBuilder();
        
        ArrayList<Point> points = s.getPointList();
        int pointSize = points.size();

        for (int i = 0; i < pointSize - 2; i++) {
            
            Point currPoint = points.get(i);
            Point nextPoint = points.get(i + 1);

            int currX = currPoint.getX();
            int currY = currPoint.getY();
            int nextX = nextPoint.getX();
            int nextY = nextPoint.getY();

            if (currX == nextX && currY > nextY) {          // North
                directionVector.append("N");
            } else if (currX < nextX && currY > nextY) {    // Northeast
                directionVector.append("A");
            } else if (currX < nextX && currY == nextY) {   // East
                directionVector.append("E");
            } else if (currX < nextX && currY < nextY) {    // Southeast
                directionVector.append("B");
            } else if (currX == nextX && currY < nextY) {   // South
                directionVector.append("S");
            } else if (currX > nextX && currY < nextY) {    // Southwest
                directionVector.append("C");
            } else if (currX > nextX && currY == nextY) {   // West
                directionVector.append("W");
            } else if (currX > nextX && currY > nextY) {    // Northwest
                directionVector.append("D");
            }
        }
        return directionVector.toString();
    }

    // Creates a pattern for a gesture from a gesture template
    private Pattern createPattern(String[] template) {
        StringBuilder s = new StringBuilder();
        s.append("^");          // match the start of input
        s.append(".{0,2}+");    // consume any character 0-2 times (this gets rid of the noise at the beginning)
        for (int i = 0; i < template.length; i++) {
            switch (template[i]) {
                case "NORTH": s.append("N+"); break;
                case "EAST": s.append("E+"); break;
                case "SOUTH": s.append("S+"); break;
                case "WEST": s.append("W+"); break;
                case "NORTHEAST": s.append("A+"); break;
                case "SOUTHEAST": s.append("B+"); break;
                case "SOUTHWEST": s.append("C+"); break;
                case "NORTHWEST": s.append("D+"); break;

                case "NORTHISH": s.append("[NAD]+"); break;
                case "EASTISH": s.append("[EAB]+"); break;
                case "SOUTHISH": s.append("[SBC]+"); break;
                case "WESTISH": s.append("[WCD]+"); break;
                case "NORTHEASTISH": s.append("[ANE]+"); break;
                case "SOUTHEASTISH": s.append("[BES]+"); break;
                case "SOUTHWESTISH": s.append("[CSW]+"); break;
                case "NORTHWESTISH": s.append("[DWN]+"); break;
            }
        }
        s.append(".{0,2}+"); 
        s.append("$");
        return Pattern.compile(s.toString());
    }

}