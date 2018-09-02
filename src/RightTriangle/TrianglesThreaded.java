package RightTriangle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class TrianglesThreaded extends TrianglesClass {
    private static final String errParams = "Usage: <filename> <nPros>";
    private static final String errSemAcquire = "could not acquire the lock: ";

    private int nprocs = 0;
    private int numPoints;

    private int totalRightTriangles = 0;
    private Semaphore sem = new Semaphore(1);

    private Set<Triangle> checkTriangles = new HashSet<>();



    public static void main(String[] args) {
        TrianglesThreaded t = new TrianglesThreaded();

        if(args.length < 2) {
            throw new IllegalArgumentException(errParams);
        }

        t.readPoints(args[0]);
        t.nprocs = Integer.parseInt(args[1]);

        System.out.println(t.findTriangles());
    }

    @Override
    protected void readPoints(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            numPoints = Integer.parseInt(reader.readLine());

            String line;
            while((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(" ");
                points.add(new Point(Integer.parseInt(lineSplit[0]), Integer.parseInt(lineSplit[1])));
            }
        } catch (FileNotFoundException e) {
            System.err.println(errFNF + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected int findTriangles() {
        findThreadTriangles();
        return totalRightTriangles;
    }

    private void findThreadTriangles() {
        ExecutorService pool = Executors.newFixedThreadPool(nprocs);

        //need to fix this logic for odd divisions
        int amountPer = numPoints / nprocs;
        int remainder = numPoints % nprocs;

        int beg = 0, end = amountPer;
        for(int i = 0; i < nprocs; i++) {
            pool.execute(new RightTriangleFinder(points.subList(beg, end)));
            beg += amountPer;
            end += amountPer;

            if(remainder > 0 && nprocs <= numPoints) {
                end++;
                remainder--;
            }
            if(remainder > 0 && nprocs > numPoints){
                beg = end;
                end++;
                remainder--;
            }
        }

        pool.shutdown();

        while(!pool.isTerminated()) {}
    }

    private class RightTriangleFinder implements Runnable {

        List<Point> threadPoints;

        public RightTriangleFinder(List<Point> pIn) {
            threadPoints = pIn;
        }

        @Override
        public void run() {
            String out = "";
            for(Point p : threadPoints) {
                for(int i = 0; i < points.size()-1; i++) {
                    Triangle t = new Triangle(p, points.get(i), points.get(i+1));
                    out += (Thread.currentThread().getName() + " found triangle: " + t + "\n");
                    try {
                        sem.acquire();
                        if(!checkTriangles.contains(t) && t.isRight()) {
                                totalRightTriangles++;
                                out += "^ right triangle\n";
                                checkTriangles.add(t);
                                sem.release();
                        }
                        sem.release();
                    } catch (InterruptedException e) {
                        System.err.println(errSemAcquire + e.getMessage());
                    }
                }
            }
            System.out.println(out);
            System.out.flush();
        }
    }
}
