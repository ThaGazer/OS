package RightTriangle;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class triangles {
    private static final String errCmdUse = "usage: <filename>";
    private static final String errFOF = "file not found";
    private static final String errNoTriangle = "could not form a triangle with points provided";
    private static final String errPoints = "number of points in file do not match";

    private static List<Integer> pointsX;
    private static List<Integer> pointsY;
    private static Integer totalPnts;

    public static void main(String[] args) throws Exception {
        pointsX = new ArrayList<>();
        pointsY = new ArrayList<>();

        if(args.length != 1) {
            throw new IllegalArgumentException(errCmdUse);
        }

        File file = new File(args[0]);

        if(!file.isFile()) {
            throw new FileNotFoundException(errFOF);
        }

        Scanner scn = new Scanner(file);

        totalPnts = Integer.parseInt(scn.next());

        if(totalPnts >= 3) {
            readPoints(scn);
            System.out.println(findTriangles());
        } else {
            throw new Exception(errNoTriangle);
        }
    }

    private static void readPoints(Scanner scn) throws Exception {
        for(int i = 0; i < (totalPnts*2); i++) {
            if((i%2) == 0) {
                pointsX.add(Integer.parseInt(scn.next()));
            } else {
                pointsY.add(Integer.parseInt(scn.next()));
            }
        }

        if(pointsX.size() != pointsY.size() && pointsX.size() != totalPnts) {
            throw new Exception(errPoints);
        }
    }

    private static int findTriangles() {
        int numOfRightTri = 0;

        for(int i = 0; i < totalPnts-2; i++) {
            for(int j = i+1; j < totalPnts-1; j++) {
                for(int k = j+1; k < totalPnts; k++) {
                    int x1 = pointsX.get(i),x2 = pointsX.get(j),x3 = pointsX.get(k),
                            y1 = pointsY.get(i),y2 = pointsY.get(j),y3 = pointsY.get(k);

                    if(x1 != x2 || x1 != x3 && y1 != y2 || y1 != y3) {
                        int a,b,c,temp;
                        c = distanceFormula(x1,y1,x2,y2);

                        temp = distanceFormula(x1,y1,x3,y3);
                        if(c < temp) {
                            b = c;
                            c = temp;
                        } else {
                            b = temp;
                        }

                        temp = distanceFormula(x2,y2,x3,y3);
                        if(c < temp) {
                            a = c;
                            c = temp;
                        } else {
                            a = temp;
                        }

                        if(a + b == c) {
                            numOfRightTri++;
                        }
                    }
                }
            }
        }
        return numOfRightTri;
    }

    /**
     * returns the distance between two points squared
     * @param x1 x coordinate of p1
     * @param y1 y coordinate of p1
     * @param x2 x coordinate of p2
     * @param y2 y coordinate of p2
     * @return distance between the two coordinates
     */
    private static int distanceFormula(int x1, int y1, int x2, int y2) {
        return ((x1 - x2)*(x1 - x2)) + ((y1 - y2)*(y1 - y2));
    }
}
