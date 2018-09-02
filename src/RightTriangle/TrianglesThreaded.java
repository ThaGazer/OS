package RightTriangle;

import java.util.*;
import java.util.concurrent.*;

public class TrianglesThreaded extends TrianglesClass {
    private static final String errParams = "Usage: <filename> <nPros>";
    private static final String errSemAcquire = "could not acquire the lock: ";

    protected int nprocs = 0;

    private int totalRightTriangles = 0;
    private Semaphore sem = new Semaphore(1);
    private Set<Triangle> checkTriangles = new HashSet<>();


    public static void main(String[] args) throws Exception {
        TrianglesThreaded t = new TrianglesThreaded();

        if(args.length < 2) {
            throw new IllegalArgumentException(errParams);
        }

        t.readPoints(args[0]);
        t.nprocs = Integer.parseInt(args[1]);

        System.out.println(t.findTriangles());

    }

    @Override
    protected void readPoints(String fileName) throws Exception {
        super.readPoints(fileName);
    }

    protected int findTriangles() {
        findThreadTriangles();
        return totalRightTriangles;
    }

    private void findThreadTriangles() {
        ExecutorService pool = Executors.newFixedThreadPool(nprocs);

        //need to fix this logic for odd divisions
        int threadAmount = points.size() / nprocs;

        for(int i = 0; i < nprocs; i++) {
            List<Point> tri = points.subList(i*threadAmount, (i+1)*threadAmount);
            pool.execute(new RightTriangleFinder(tri));
        }

        pool.shutdown();

        while(!pool.isTerminated()) {}
    }

    private class RightTriangleFinder implements Runnable {

        private List<Point> pointList;

        public RightTriangleFinder(List<Point> listIn) {
            pointList = listIn;
        }

        @Override
        public void run() {
//            String out = "";
            for(Point p : pointList) {
                for(int i = 0; i < points.size()-1; i++) {
                    Triangle t = new Triangle(p, points.get(i), points.get(i+1));
//                    out += (Thread.currentThread().getName() + " found triangle: " + t + "\n");
                    try {
                        sem.acquire();
                        if(!checkTriangles.contains(t) && t.isRight()) {
                                totalRightTriangles++;
//                                out += "^ right triangle\n";
                                checkTriangles.add(t);
                                sem.release();
                        }
                        sem.release();
                    } catch (InterruptedException e) {
                        System.err.println(errSemAcquire + e.getMessage());
                    }
                }
            }
            /*System.out.println(out);
            System.out.flush();*/
        }
    }
}
