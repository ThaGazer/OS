package RightTriangle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Triangles extends TrianglesClass {
    //Error messages
    private static final String errParams = "Usage: <filename> <nPros>";
    private static final String errFNF = "file not found ";
    private static final String errSemAcquire = "could not acquire the lock: ";
    private static final String errFileFormat = "file not formatted properly: ";

    private int nprocs = 0; /*number of process expressed by the user*/
    private int numPoints; /*number of points readin from the file*/

    private int totalRightTriangles = 0;
    private Semaphore sem = new Semaphore(1);

    private Set<Triangle> checkTriangles = new HashSet<>();

    public static void main(String[] args) {
        Triangles t = new Triangles();

        //parameters checking
        if(args.length < 2) {
            throw new IllegalArgumentException(errParams);
        }

        //reading from file and storing points into the set
        t.readPoints(args[0]);
        t.nprocs = Integer.parseInt(args[1]);

        //calculate number of right triangles
        System.out.println(t.findTriangles());
    }

    /**
     * reads points from a file
     * @param fileName file to read from
     */
    @Override
    protected void readPoints(String fileName) {
        try {
            //a read buffer
            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            try {
                //number of points in the file
                numPoints = Integer.parseInt(reader.readLine());
            } catch(NumberFormatException nfe) {
                System.err.println(errFileFormat + nfe.getMessage());
                System.exit(1);
            }

            //will read entire line then split the line on spaces
            String line;
            while((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(" ");

                //if line formatting is wrong
                if(lineSplit.length != 2) {
                    System.err.println(errFileFormat + Arrays.toString(lineSplit));
                    System.exit(1);
                }

                //adds point to the set
                points.add(new Point(Integer.parseInt(lineSplit[0]), Integer.parseInt(lineSplit[1])));
            }

            if(points.size() != numPoints) {
                System.err.println(errFileFormat + "expected size = <" + numPoints +
                        "> actual size = <" + points.size() + ">");
                System.exit(1);
            }
        } catch(NumberFormatException nfe) {
            System.err.println(errFileFormat + nfe.getMessage());
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println(errFNF + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(errFileFormat + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * find the total number of right triangles in the set
     * @return total number of right triangles
     */
    protected int findTriangles() {
        findThreadTriangles();
        return totalRightTriangles;
    }

    /**
     * uses threads to speed up the process of finding right triangles in the set
     */
    private void findThreadTriangles() {
        //prevents excessive thread creation
        ExecutorService pool = Executors.newFixedThreadPool(nprocs);

        int amountPer = numPoints / nprocs;
        int remainder = numPoints % nprocs;
        int beg = 0, end = amountPer;

        for(int i = 0; i < nprocs; i++) {
            pool.execute(new RightTriangleFinder(points.subList(beg,end)));

            //increments starting position for next thread
            beg += amountPer;
            end += amountPer;

            //adds in any remainder of work to be done
            if(remainder > 0) {
                end++;
                remainder--;
            }
        }

        pool.shutdown();

        //waiting for pool to completely shutdown before reporting result
        while(!pool.isTerminated()) {}
    }

    /**
     * runnable class to find right triangles from a list
     */
    private class RightTriangleFinder implements Runnable {

        List<Point> pointList;

        RightTriangleFinder(List<Point> pIn) {
            pointList = pIn;
        }

        @Override
        public void run() {
            for(Point p : pointList) {
                for (int j = points.indexOf(p)+1; j < points.size(); j++) {
                    for(int k = j+1; k < points.size(); k++) {
                        Triangle t = new Triangle(p, points.get(j), points.get(k));
                        try {
                            sem.acquire();
                            if (!checkTriangles.contains(t) && t.isRight()) {
                                totalRightTriangles++;
                                checkTriangles.add(t);
                            }
                            sem.release();
                        } catch (InterruptedException e) {
                            System.err.println(errSemAcquire + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
