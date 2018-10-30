package triangle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Triangles {
  //Error messages
  private static final String errParams = "Usage: <filename> <nPros>";
  private static final String errFNF = "file not found ";
  private static final String errSemAcquire = "could not acquire the lock: ";
  private static final String errFileFormat = "file not formatted properly: ";

  private int nprocs = 0; /*number of process expressed by the user*/
  private int numPoints; /*number of timed_points.txt readin from the file*/

  private int totalRightTriangles = 0;
  private Semaphore sem = new Semaphore(1);

  private Set<Triangle> checkTriangles = new HashSet<>();
  private List<Point> points = new ArrayList<>();


  public static void main(String[] args) {
    Triangles t = new Triangles();

    //parameters checking
    if(args.length < 2) {
      throw new IllegalArgumentException(errParams);
    }

    //reading from file and storing timed_points.txt into the set
    t.readPoints(args[0]);
    t.nprocs = Integer.parseInt(args[1]);

    //calculate number of right triangles
    System.out.println(t.findTriangles());
  }

  /**
   * reads timed_points.txt from a file
   *
   * @param fileName file to read from
   */
  private void readPoints(String fileName) {
    try {
      //a read buffer
      BufferedReader reader = new BufferedReader(new FileReader(fileName));

      try {
        //number of timed_points.txt in the file
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
    } catch(FileNotFoundException e) {
      System.err.println(errFNF + e.getMessage());
      System.exit(1);
    } catch(IOException e) {
      System.err.println(errFileFormat + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * find the total number of right triangles in the set
   *
   * @return total number of right triangles
   */
  private int findTriangles() {
    findThreadTriangles();
    return totalRightTriangles;
  }

  /**
   * uses threads to speed up the process of finding right triangles in the set
   */
  private void findThreadTriangles() {
    //prevents excessive thread creation
    ExecutorService pool = Executors.newFixedThreadPool(nprocs);

    long totalLoad = ((((long)points.size() * points.size() * points.size())) -
        ((3L * points.size() * points.size())) + ((2L * points.size())));
    totalLoad = totalLoad / 6L + (totalLoad % 6L);

    long workLoad = totalLoad / nprocs;
    long remainder = totalLoad % nprocs;

    long workCount = 0, threadWork = workLoad;
    for(int i = 0; i < points.size() && nprocs > 0; i++) {
      for(int j = i + 1; j < points.size() && nprocs > 0; j++) {
        for(int k = j + 1; k < points.size() && nprocs > 0; k++) {
          if((workCount % threadWork) == 0) {
            nprocs--;

            if(nprocs == 0) {
              threadWork += remainder;
            }

            pool.execute(new RightTriangleFinder(i, j, k, threadWork));
          }
          workCount++;
        }
      }
    }

    pool.shutdown();

    //waiting for pool to completely shutdown before reporting result
    while(!pool.isTerminated()) {
    }
  }

  /**
   * runnable class to find right triangles from a list
   */
  private class RightTriangleFinder implements Runnable {

    private int i, j, k;
    private long amount;

    RightTriangleFinder(int i, int j, int k, long workLoad) {
      this.i = i;
      this.j = j;
      this.k = k;
      amount = workLoad;
    }

    @Override
    public void run() {
      boolean start = true;
      for(; i < points.size() && amount > 0; i++) {
        if(!start) {
          j = i + 1;
        }
        for(; j < points.size() && amount > 0; j++) {
          if(!start) {
            k = j + 1;
          }
          for(; k < points.size() && amount > 0; k++) {
            if(start) {
              start = false;
            }
            amount--;

            //new triangle out timed_points.txt i,j,k
            Triangle t = new Triangle(points.get(i), points.get(j), points.get(k));

            try {
              sem.acquire();
              if(!checkTriangles.contains(t)) {
                sem.release();
                if(t.isRight()) {
                  sem.acquire();

                  totalRightTriangles++;
                  checkTriangles.add(t);

                  sem.release();
                }
              }
            } catch(InterruptedException e) {
              System.err.println(errSemAcquire + e.getMessage());
            }
          }
        }
      }
    }
  }
}
