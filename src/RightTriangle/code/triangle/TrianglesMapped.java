package triangle;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ProtocolException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class TrianglesMapped {

  private static final String errUsage = "Usage: <filename> <thread count>";
  private static final String errFNF = "Could not find file: ";
  private static final String errThreadJoin = "Could not join thread: ";
  private static final String errDupPoint = "found matching points";
  private static final String errFileFormat = "There are problems with the way the file is formatted";
  private static final String errFileOverflow = "too many point found";
  private static final String errFileUnderflow = "not enough points in file";

  private String filename;
  private int threadCount;
  private ArrayList<Point> pointList = new ArrayList<>();
  private ArrayList<Triangle> rightTriangles = new ArrayList<>();
  private MappedTextBuffer pointsTextBuffer;

  private TrianglesMapped(String[] args) {
    if(args.length < 2 || args.length > 2) {
      throw new IllegalArgumentException(errUsage);
    }

    filename = args[0];
    threadCount = Integer.valueOf(args[1]);
  }

  public static void main(String[] args) {
    TrianglesMapped t = new TrianglesMapped(args);

    //reads all points into a collection
    t.readPoints(t.filename);
    int totRightTri = t.findTriangles();

    System.out.println("Right triangles: " + totRightTri);
  }

  private void readPoints(String filename) {
    File pointsFile = new File(filename);
    try {
      pointsTextBuffer = new MappedTextBuffer(new RandomAccessFile(pointsFile, "r").
          getChannel().map(FileChannel.MapMode.READ_ONLY, 0, pointsFile.length()));
    } catch(IOException e) {
      System.err.println(errFNF + pointsFile);
      System.exit(1);
    }

    fileFormatChecker();
  }

  private int findTriangles() {
    //TODO remove debug commenting

    ArrayList<Thread> threadList = new ArrayList<>();
    int workLoad = pointList.size() / threadCount;
    int remainder = pointList.size() % threadCount;
    int startLoc = 0;
    AtomicInteger totalRightTriangles = new AtomicInteger();

    pointList.sort(Comparator.naturalOrder());

    for(int i = 0; i < threadCount; i++) {
      int ajustedWorkLoad = workLoad;
      if(remainder > 0) {
        ajustedWorkLoad++;
        remainder--;
      }

      int passedWorkLoad = ajustedWorkLoad;
      int passedStartLoc = startLoc;
      Thread thread = new Thread(() -> {
        for(int j = passedStartLoc; j < (passedStartLoc+passedWorkLoad) && j < (pointList.size()-2); j++) {
          Triangle t = new Triangle(pointList.get(j), pointList.get(j+1), pointList.get(j+2));
        }
      });
      thread.start();
      threadList.add(thread);

      startLoc += ajustedWorkLoad;
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
    return totalRightTriangles.get();
  }

  private void fileFormatChecker() {
    try {
      //how many points there should be
      int pointCount = pointsTextBuffer.nextInt();
      while(pointsTextBuffer.hasRemaining()) {
        //if there were more points then there should have been
        if(pointCount <= 0) {
          throw new ProtocolException(errFileOverflow);
        }

        if(!pointList.add(new Point(pointsTextBuffer.nextInt(), pointsTextBuffer.nextInt()))) {
          throw new IllegalArgumentException(errDupPoint);
        }
        pointCount--;
      }

      if(pointCount > 0) {
        throw new ProtocolException(errFileUnderflow);
      }
    } catch(Exception e) {
      System.err.println(errFileFormat + ": " + e.getMessage());
      System.exit(1);
    }
  }
}