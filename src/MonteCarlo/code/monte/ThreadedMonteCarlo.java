package monte;

import java.io.*;
import java.util.concurrent.*;

public class ThreadedMonteCarlo {

  private static String[] lines;
  private static int centX = 0, centY = 0, rad = 1;

  public static void main(String[] args) {
    if(args.length != 2) {
      System.err.println("incorrect number of parameter");
      throw new IllegalArgumentException();
    }

    String fname = args[0];
    int nProc = Integer.parseInt(args[1]);
    int totPnts = 1;

    try {
      BufferedReader buffedFile =
          new BufferedReader(new FileReader(fname));
      totPnts = Integer.parseInt(buffedFile.readLine());
      lines = new String[totPnts];

      for(int i = 0; i < totPnts; i++) {
        lines[i] = buffedFile.readLine();
        if(lines[i] == null) {
          System.err.println("wrong number of timed_points.txt");
          throw new IllegalArgumentException();
        }
      }
    } catch(FileNotFoundException e) {
      System.err.println("could not find file");
      e.printStackTrace();
    } catch(IOException e) {
      e.printStackTrace();
    }

    //calculates how much each thread should handle
    int start = 0;
    int amnount = totPnts / nProc;
    int[][] params = new int[nProc][2];

    for(int i = 0; i < nProc; i++) {
      params[i][1] = amnount;
      params[i][0] = start;
      start += amnount;
    }
    //adds extra timed_points.txt to last thread
    if(start < totPnts) {
      params[nProc - 1][0] += (totPnts - start);
    }

    ExecutorService exec = Executors.newCachedThreadPool();
    Future[] res = new Future[nProc];
    for(int i = 0; i < nProc; i++) {
      System.out.println("Starting thread: " + i);
      res[i] = exec.submit(new thread(params[i]));
    }

    int totPntIn = 0;
    for(Future r : res) {
      try {
        totPntIn += (int)r.get();
      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    double pi = 4 * ((double)totPntIn / totPnts);
    System.out.println("pi is equal to: " + pi);
  }

  static class thread implements Callable<Integer> {

    private int where, howMuch;

    private thread(int[] a) {
      where = a[0];
      howMuch = a[1];
    }

    @Override
    public Integer call() {

      int count = 0;
      for(int i = where; i < (where + howMuch); i++) {
        String[] splitLine = lines[i].split(", ");
        double x = Double.parseDouble(splitLine[0]);
        double y = Double.parseDouble(splitLine[1]);

        if((Math.sqrt(x - centX) + Math.sqrt(y - centY))
            <= Math.sqrt(rad)) {
          count++;
        }
      }
      return count;
    }
  }
}
