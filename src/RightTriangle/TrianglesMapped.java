package RightTriangle;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ProtocolException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class TrianglesMapped {

  private static final String errUsage = "Usage: <filename> <thread count>";
  private static final String errFNF = "Could not find file: ";
  private static final String errThreadJoin = "Could not join thread: ";
  private static final String errFileFormat = "There are problems with the way the file is formatted";
  private static final String errFileOverflow = "too many point found";
  private static final String errFileUnderflow = "not enough points in file";

  private String filename;
  private int threadCount;
  private ArrayList<Point> pointList = new ArrayList<>();
  private ArrayList<Triangle> rightTriangles = new ArrayList<>();
  private MappedTextBuffer pointsTextBuffer;
  private Semaphore sem = new Semaphore(1);

  public TrianglesMapped(String[] args) {
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

  protected void readPoints(String filename) {
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

  protected int findTriangles() {
    //TODO remove debug commenting

    ArrayList<Thread> threadList = new ArrayList<>();
    int workLoad = pointList.size() / threadCount;
    int remainder = pointList.size() % threadCount;
    int startLoc = 0;
    AtomicInteger totalRightTriangles = new AtomicInteger();

    for(int i = 0; i < threadCount; i++) {
      int ajustedWorkLoad = workLoad;
      if(remainder > 0) {
        ajustedWorkLoad++;
        remainder--;
      }

      int finalAjustedWorkLoad = ajustedWorkLoad;
      int finalStartLoc = startLoc;
      Thread thread = new Thread(() -> {
        int totalTri = 0;
        for(int j = finalStartLoc; j < finalStartLoc + finalAjustedWorkLoad; j++) {
          for(int k = 0; k < pointList.size(); k++) {
            for(int l = 0; l < pointList.size(); l++) {
              if(j != k && j != l && k != l) {
                String out = "";
                Triangle t = new Triangle(pointList.get(j), pointList.get(k), pointList.get(l));
                out += t.toString();
                totalTri++;
                if(!rightTriangles.contains(t)) {
                  if(t.isRight()) {
                    totalRightTriangles.getAndIncrement();
                    rightTriangles.add(t);
                    out += "<- right";
                  }
                } else {
                  out += "<-dup";
                }
                System.out.println(out);
              }
            }
          }
        }
        //System.out.println(Thread.currentThread().getName() + " " + totalTri);
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
        pointList.add(new Point(pointsTextBuffer.nextInt(), pointsTextBuffer.nextInt()));
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