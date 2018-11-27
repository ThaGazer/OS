/*
 * Author: Justin Ritter
 * File: Triangle3Loop.java
 * Date: 11/20/2018
 *
 * Description: Reads x,y points from a file and calculates how many
 * right triangles can be made from the points
 */
package triangle;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Triangles {

  private static final String errUsage = "Usage: <filename> <thread count>";
  private static final String errFNF = "Could not find file: ";
  private static final String errThreadJoin = "Could not join thread: ";
  private static final String errDupPoint = "found matching points";
  private static final String errFileFormat =
          "There are problems with the way the file is formatted";
  private static final String errFileOverflow = "too many points found";
  private static final String errFileUnderflow = "too few points found";

  private String filename; /*name of file to read from*/
  private int threadCount; /*number of threads user wants*/
  private ArrayList<Point> pointList = new ArrayList<>();
  private MappedTextBuffer pointsTextBuffer; /*mapped file buffer*/
  private AtomicInteger totalRightTriangles = new AtomicInteger();

  private Triangles(String args[]) {
    if(args.length < 2 || args.length > 2) {
      throw new IllegalArgumentException(errUsage);
    }

    filename = args[0];
    threadCount = Integer.valueOf(args[1]);
  }

  public static void main(String args[]) {
    Triangles t = new Triangles(args);

    //reads all points into a collection
    t.readPoints();

    //attempts to find all possible right triangles from points read in
    t.findTriangles();

    System.out.println("Right triangles: " + t.totalRightTriangles.get());
  }

  /**
   * Spawns threadCount number of threads to calculate the number of right
   * triangles from a list of x,y points.
   *
   * if threadCount > pointList.size()-2 shrink threadCount as to
   * not spawn unnecessary threads
   */
  private void findTriangles() {
    ArrayList<Thread> threadList = new ArrayList<>();

    //threadCount resize
    if(threadCount > pointList.size()-2) {
      threadCount = pointList.size()-2;
    }

    //has to be enough points to form a triangle
    if(pointList.size() >= 3) {
      //workLoad for each thread to compute
      int workLoad = pointList.size() / threadCount;

      //spawns threadCount number of threads
      for(int i = 0; i < threadCount; i++) {
        int offset = i; /*offset to pass into thread*/
        threadList.add(new Thread(() -> {
          //find all combinations of points starting with j
          for(int j = (workLoad * offset);
              j < (workLoad * offset) + workLoad; j++) {
            for(int k = j + 1; k < pointList.size() - 1; k++) {
              for(int l = k + 1; l < pointList.size(); l++) {
                //check to see if j,k,l refer to a right triangle
                System.out.println(j + " " + k + " "+ l);
                rightCheck(j, k, l);
              }
            }
          }
        }));
        threadList.get(i).start();
      }
    }

    for(Thread t : threadList) {
      try {
        t.join();
      } catch(InterruptedException e) {
        System.err.println(errThreadJoin);
        System.exit(1);
      }
    }
  }

  /**
   * Checks if three indices relate to points in the pointList that form a
   * right triangle. Increment totalRightTriangles if so.
   * @param a point index
   * @param b point index
   * @param c point index
   */
  private void rightCheck(int a, int b, int c) {
    Point p1 = pointList.get(a);
    Point p2 = pointList.get(b);
    Point p3 = pointList.get(c);

    if(Triangle.isRight(p1,p2,p3)) {
      totalRightTriangles.incrementAndGet();
    }
  }

  /**
   * Read all points from a file. Where the first number is the number of
   * points in the file and the points are formatted [(int) (int)[\n|EOF]]
   */
  private void readPoints() {
    File pointsFile = new File(filename);
    try {
      pointsTextBuffer = new MappedTextBuffer(
              new RandomAccessFile(pointsFile, "r").getChannel().
                      map(FileChannel.MapMode.READ_ONLY, 0, pointsFile.length()));
    } catch(IOException e) {
      System.err.println(errFNF + pointsFile);
      System.exit(1);
    }

    fileFormatChecker();
  }

  /**
   * Ensures the formatting of the file.
   * Reads all resulting points into the pointList
   */
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
    }
  }
}
