/*
  Author: Justin Ritter
  Date:
  File: TrianglesRecursive.java
  Description: testing file mapping with checking right triangles from a list of points
 */

package triangle;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class Triangles {

  private static final String errUsage = "Usage: <filename> <thread count>";
  private static final String errFNF = "Could not find file: ";
  private static final String errThreadJoin = "Could not join thread: ";
  private static final String errDupPoint = "found matching points";
  private static final String errFileFormat = "There are problems with the " +
          "way the file is formatted: ";
  private static final String errFileOverflow = "too many points found";
  private static final String errFileUnderflow = "too few points found";

  private String filename;
  private int threadCount;
  private ArrayList<Point> pointList = new ArrayList<>();
  private MappedTextBuffer pointsTextBuffer;
  private AtomicInteger totalRightTriangles = new AtomicInteger();
  private AtomicInteger totalTriangles = new AtomicInteger();

  private Triangles(String[] args) {
    if(args.length < 2 || args.length > 2) {
      throw new IllegalArgumentException(errUsage);
    }

    filename = args[0];
    threadCount = Integer.valueOf(args[1]);
  }

  public static void main(String[] args) {
    Triangles t = new Triangles(args);

    //reads all points into a collection
    t.readPoints();

    //attempts to find all possible right triangles from points read in
    t.findTriangles();

    System.out.println(t.totalTriangles.get());
    System.out.println("Right triangles: " + t.totalRightTriangles.get());
  }

  private void readPoints() {
    File pointsFile = new File(filename);
    try {
      pointsTextBuffer = new MappedTextBuffer
              (new RandomAccessFile(pointsFile, "r").
                      getChannel().
                      map(FileChannel.MapMode.READ_ONLY, 0, pointsFile.length()));
    } catch(IOException e) {
      System.err.println(errFNF + pointsFile);
      System.exit(1);
    }

    fileFormatChecker();
  }

  private void findTriangles() {
    ArrayList<Thread> threadList = new ArrayList<>();
    int workLoad = pointList.size() / threadCount;
    int remainder = pointList.size() % threadCount;
    int startLoc = 0;

    pointList.sort(Comparator.naturalOrder());
    System.out.println(pointList);

    if(pointList.size() == 3) {
      rightCheck(0, 1, 2);
    } else if(pointList.size() > 3) {
      for(int i = 0; i < threadCount; i++) {
        int ajustedWorkLoad = workLoad;
        if(remainder > 0) {
          ajustedWorkLoad++;
          remainder--;
        }

        int passedWorkLoad = ajustedWorkLoad;
        int passedStartLoc = startLoc;
        Thread thread = new Thread(() -> {

          //iterate through point in this threads
          // workload or till end of points list
          for(int j = passedStartLoc; j < (passedStartLoc + passedWorkLoad) &&
                  j < pointList.size(); j++) {
            //all points before j
            for(int k = 0; k + 1 < j; k++) {
              System.out.println(k + " " + (k+1) + " " + j);
              rightCheck(j, k, k+1);
            }

            //all points after j
            for(int k = j + 1; k < pointList.size() - 1; k++) {
              System.out.println(j + " " + k + " " + (k+1));
              rightCheck(j, k, k+1);
            }

            //bounds check
            if(j == 0) {
              rightCheck(j, j+1, pointList.size() - 1);
            } else if(j == pointList.size() - 1) {
              rightCheck(j, 0, j - 1);
            } else {
              rightCheck(j, 0, pointList.size() - 1);
            }
          }
        });
        thread.start();
        threadList.add(thread);

        startLoc += ajustedWorkLoad;
      }
    }

    //wait on all threads to finish
    for(Thread thread : threadList) {
      try {
        thread.join();
      } catch(InterruptedException e) {
        System.err.println(errThreadJoin);
        System.exit(1);
      }
    }
  }

  private void fileFormatChecker() {
    try {
      //how many points there should be
      int pointCount = pointsTextBuffer.nextInt();

      while(pointsTextBuffer.hasRemaining()) {
        //if there were more points then there should have been
        if(pointCount <= 0) {
          throw new Exception(errFileOverflow);
        }

        if(!pointList.add(new Point(pointsTextBuffer.nextInt(),
                pointsTextBuffer.nextInt()))) {
          throw new IllegalArgumentException(errDupPoint);
        }
        pointCount--;
      }

      if(pointCount != 0) {
        throw new Exception(errFileUnderflow);
      }
    } catch(Exception e) {
      System.err.println(errFileFormat + ": " + e.getMessage());
      System.exit(1);
    }
  }

  private void rightCheck(int i, int j, int k) {
    Point p1 = pointList.get(i), p2 = pointList.get(j), p3 = pointList.get(k);

    System.out.print(i + " " + j + " " + k);

    if(Triangle.isTriangle(p1, p2, p3)) {
      totalTriangles.incrementAndGet();
      if(Triangle.isRight(p1,p2,p3)) {
        System.out.println("<-right");
        totalRightTriangles.incrementAndGet();
      } else {
        System.out.println();
      }
    }
  }
}