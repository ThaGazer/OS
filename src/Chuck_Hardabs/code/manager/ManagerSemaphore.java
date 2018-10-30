/*
 * Chuck_Hardabs:manager.ManagerSemaphore
 * Created on 2/20/2018
 *
 * Author(s):
 * -Justin Ritter
 */
package manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class ManagerSemaphore {
  // Maximum time in between fan arrivals
  private static final int MAX_TIME_IN_BETWEEN_ARRIVALS = 3000;

  // Maximum amount of break time in between celebrity photos
  private static final int MAX_BREAK_TIME = 10000;

  // Maximum amount of time a fan spends in the exhibit
  private static final int MAX_EXHIBIT_TIME = 10000;

  // Minimum number of fans for a photo
  private static final int MIN_FANS = 3;

  // Maximum number of fans allowed in queue
  private static final int MAX_ALLOWED_IN_QUEUE = 10;

  // Holds the queue of fans
  private static ArrayList<Fan> line = new ArrayList<Fan>();

  // The current number of fans in line
  private static int numFansInLine = 0;

  // For generating random times
  private Random rndGen = new Random(new Date().getTime());

  private Semaphore fanSem = new Semaphore(MAX_ALLOWED_IN_QUEUE);
  private Semaphore celebSem = new Semaphore(1);
  private Semaphore lineSem = new Semaphore(1);

  public static void main(String[] args) {
    try {
      new ManagerSemaphore().go();
    } catch(InterruptedException e) {
      System.err.println(e.toString());
      System.exit(1);
    }
  }

  private void go() throws InterruptedException {
    //lock the celebrity
    celebSem.acquire();

    // Create the celebrity thread
    Celebrity c = new Celebrity();
    new Thread(c, "Celebrity").start();

    // Continually generate new fans
    int i = 0;
    while(true) {
      new Thread(new Fan(), "Fan " + i++).start();
      try {
        Thread.sleep(rndGen.nextInt(MAX_TIME_IN_BETWEEN_ARRIVALS));
      } catch(InterruptedException e) {
        System.err.println(e.toString());
        System.exit(1);
      }
    }
  }

  class Celebrity implements Runnable {
    @Override
    public void run() {
      while(true) {
        try {
          celebSem.acquire();

        } catch(InterruptedException e) {
          System.err.println(e.toString());
          System.exit(1);
        }

        // Check to see if celebrity flips out
        checkCelebrityOK();

        // Take picture with fans
        System.out.println("Celebrity takes a picture with fans");

        // Remove the fans from the line
        for(int i = 0; i < MIN_FANS; i++) {
          System.out.println(line.remove(0).getName() + ": OMG! Thank you!");
        }

        // Adjust the numFans variable
        numFansInLine -= MIN_FANS;

        lineSem.release();
        fanSem.release(MIN_FANS);

        // Take a break
        try {
          Thread.sleep(rndGen
              .nextInt(MAX_BREAK_TIME));
        } catch(InterruptedException e) {
          System.err.println(e.toString());
          System.exit(1);
        }
      }

    }

  }

  public void checkCelebrityOK() {
    if(numFansInLine > MAX_ALLOWED_IN_QUEUE) {
      System.err.println("Celebrity becomes claustrophobic and flips out");
      System.exit(1);
    }

    if(numFansInLine < MIN_FANS) {
      System.err.println("Celebrity becomes enraged that he was woken from nap for too few fans");
      System.exit(1);
    }
  }

  class Fan implements Runnable {
    String name;

    public String getName() {
      return name;
    }

    @Override
    public void run() {
      try {
        fanSem.acquire();
      } catch(InterruptedException e) {
        System.err.println(e.toString());
      }

      // Set the thread name
      name = Thread.currentThread().toString();

      System.out.println(Thread.currentThread() + ": arrives");

      // Look in the exhibit for a little while
      try {
        Thread.sleep(rndGen.nextInt(MAX_EXHIBIT_TIME));
      } catch(InterruptedException e) {
        System.err.println(e.toString());
        System.exit(1);
      }

      // Get in line
      System.out.println(Thread.currentThread() + ": gets in line");
      line.add(0, this);

      try {
        lineSem.acquire();
      } catch(InterruptedException e) {
        System.err.println(e.toString());
      }
      numFansInLine++;

      if(numFansInLine < MIN_FANS) {
        lineSem.release();
      } else {
        celebSem.release();
      }
    }
  }
}
