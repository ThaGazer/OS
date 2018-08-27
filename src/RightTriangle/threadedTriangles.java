package RightTriangle;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class threadedTriangles {

    private static final String errParams = "Usage: <filename> <thread count>";
    private static final String errFile = "could not find file path";
    private static final String errPoints = "we got problems";

    private static List<point> points;

    public static void main(String[] args) throws Exception {
        points = new ArrayList<>();
        /*if(args.length != 2) {
            throw new IllegalArgumentException(errParams);
        }*/

        //reads all points into a collection
        readFile(args[0]);
        System.out.println(findTriangle());

    }

    private static void readFile(String filename) throws Exception {
        try {
            Scanner scn = new Scanner(new File(filename));

            int totalPoints = scn.nextInt();

            while(scn.hasNext()) {
                points.add(new point(scn.nextInt(), scn.nextInt()));
            }

            if(points.size() != totalPoints) {
                throw new Exception(errPoints);
            }
        } catch (FileNotFoundException e) {
            System.err.println(errFile + filename);
        }
    }

    private static int findTriangle() {
        int totalRight = 0;

        for(int i = 0; i < points.size()-2; i++) {
            for(int j = i+1; j < points.size()-1; j++) {
                for(int k = j+1; k < points.size(); k++) {
                    if(points.get(i).rightCheck(points.get(j), points.get(k))) {
                        totalRight++;
                    }
                }
            }
        }
        return totalRight;
    }

    private static class point {
        private int x;
        private int y;

        point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }

        boolean zeroSlope(point p2, point p3) {
            return getX() != p2.getX() || getX() != p3.getX() && getY() != p2.getY() || getY() != p3.getY();
        }

        int distance(point p1) {
            return ((getX() - p1.getX())*(getX() - p1.getX())) + ((getY() - p1.getY())*(getY() - p1.getY()));
        }

        boolean rightCheck(point p2, point p3) {
            if(zeroSlope(p2, p3)) {
                int a,b,c, temp;
                c = distance(p2);

                a = distance(p3);
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

        public String toString() {
            return "(" + getX() + "," + getY() + ")";
        }
    }
}
