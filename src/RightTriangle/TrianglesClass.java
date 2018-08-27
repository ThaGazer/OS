package RightTriangle;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TrianglesClass extends Triangles {

    private static final String errParams = "Usage: <filename> <thread count>";
    private static final String errPoints = "we got problems";

    private List<Point> points = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        TrianglesClass t = new TrianglesClass();

        if(args.length < 1) {
            throw new IllegalArgumentException(errParams);
        }

        //reads all points into a collection
        t.readPoints(args[0]);
        System.out.println(t.findTriangles());

    }

    @Override
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

    @Override
    protected int findTriangles() {
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
}
