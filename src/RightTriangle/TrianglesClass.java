package RightTriangle;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TrianglesClass {

    private static final String errParams = "Usage: <filename> <thread count>";
    private static final String errFNF = "file not found ";
    private static final String errPoints = "we got problems";

    protected List<Point> points = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        TrianglesClass t = new TrianglesClass();

        if(args.length < 1) {
            throw new IllegalArgumentException(errParams);
        }

        //reads all points into a collection
        t.readPoints(args[0]);
        System.out.println(t.findTriangles());

    }


    protected void readPoints(String filename) throws Exception {
        try {
            Scanner scn = new Scanner(new File(filename));

            int totalPoints = scn.nextInt();

            while(scn.hasNext()) {
                points.add(new Point(scn.nextInt(), scn.nextInt()));
            }

            if(points.size() != totalPoints) {
                throw new Exception(errPoints);
            }
        } catch (FileNotFoundException e) {
            System.err.println(errFNF + filename);
        }
    }

    protected int findTriangles() {
        int totalRight = 0;

        for(int i = 0; i < points.size()-2; i++) {
            for(int j = i+1; j < points.size(); j++) {
                for(int k = j; k < points.size(); k++) {
                    if(rightCheck(points.get(i), points.get(j), points.get(k))) {
                        totalRight++;
                    }
                }
            }
        }
        return totalRight;
    }

    private boolean rightCheck(Point p1, Point p2, Point p3) {
        if(!p1.isZeroSlope(p2, p3)) {
            int a,b,c, temp;
            c = p1.distance(p2);

            a = p1.distance(p3);
            if(a > c) {
                temp = c;
                c = a;
                a = temp;
            }

            b = p2.distance(p3);
            if(b > c) {
                temp = c;
                c = b;
                b = temp;
            }

            return a+b == c;
        } else {
            return false;
        }
    }
}
