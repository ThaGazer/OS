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

public class TrianglesRecursive {

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

  private TrianglesRecursive(String[] args) {
    if(args.length < 2 || args.length > 2) {
      throw new IllegalArgumentException(errUsage);
    }

    filename = args[0];
    threadCount = Integer.valueOf(args[1]);
  }

  public static void main(String[] args) {
    TrianglesRecursive t = new TrianglesRecursive(args);

    //reads all points into a collection
    t.readPoints();

    //attempts to find all possible right triangles from points read in
    t.findTriangles();

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

  /**
   * https://www.geeksforgeeks.org/print-all-possible-combinations-of-r-elements-in-a-given-array-of-size-n/
   */
  private void findTriRecur(int start, int end, int index, int r) {

  }

  private void findTriangles() {
    ArrayList<Thread> threadList = new ArrayList<>();
    int workLoad = pointList.size() / threadCount;
    int remainder = pointList.size() % threadCount;
    int startLoc = 0;

    pointList.sort(Comparator.naturalOrder());


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

  private void rightCheck(Point p1, Point p2, Point p3) {
    if(Triangle.isRight(p1,p2,p3)) {
      totalRightTriangles.incrementAndGet();
    }
  }
}