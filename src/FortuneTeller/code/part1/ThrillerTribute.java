/*
 * Author: Justin Ritter
 * File: ThrillerTribute.java
 * Date: 9/26/2018
 */
package part1;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

/**
 * Simulation of Thriller Tribute problem
 */
public class ThrillerTribute {

  // Delay between new MJ impersonators
  private static int MJDELAY = 500;
  // Delay between new zombies
  private static int ZOMBIEDELAY = 250;
  // Dance floor used to enforce constraints
  private DanceFloor floor = new DanceFloor();
  // Generator for random events
  private Random rndGen = new Random(new Date().getTime());

  /*
   * You may add variables here
   */
  CyclicBarrier wall = new CyclicBarrier(3);
  Semaphore mjLock = new Semaphore(1);
  Semaphore zombieLock = new Semaphore(2);
  Semaphore finishLock = new Semaphore(1);

  /**
   * Start MJ and zombie-spawning threads
   */
  public void go() {
    // Thread to repeatedly spawn MJ threads
    new Thread() {
      @Override
      public void run() {
        int i = 0; // MJ impersonator ID
        while(true) {
          new Thread(new MJ(i++)).start();
          try {
            sleep(rndGen.nextInt(MJDELAY));
          } catch(InterruptedException e) {
          }
        }
      }
    }.start();

    // Thread to repeatedly spawn zombie threads
    new Thread() {
      @Override
      public void run() {
        int i = 0; // Zombie ID
        while(true) {
          new Thread(new Zombie(i++)).start();
          try {
            sleep(rndGen.nextInt(ZOMBIEDELAY));
          } catch(InterruptedException e) {
          }
        }
      }
    }.start();
  }

  public static void main(String[] args) {
    new ThrillerTribute().go();
  }

  /**
   * I created this function for error handling
   * Its exactly the same has the error function from Donahoo's Fortune.java
   *
   * @param msg error message
   */
  private static void error(final String msg) {
    System.err.println(msg);
    System.exit(1);
  }

  /**
   * MJ impersonator dancer
   */
  private class MJ implements Runnable {

    private int id; // ID of MJ impersonator

    /**
     * MJ impersonator constructor
     *
     * @param id id of MJ impersonator
     */
    public MJ(int id) {
      this.id = id;
    }

    @Override
    public String toString() {
      return "MJ" + id;
    }

    /*
     * MJ impersonator main behavior
     */
    @Override
    public void run() {
      /*
       * You may add code in this method; you may not delete code
       */
      floor.addDancer(this);
    }
  }

  /**
   * Zombie dancer
   */
  private class Zombie implements Runnable {

    private int id; // ID of zombie

    /**
     * Zombie constructor
     *
     * @param id id of zombie
     */
    public Zombie(int id) {
      this.id = id;
    }

    @Override
    public String toString() {
      return "Zombie" + id;
    }

    /*
     * Zombie main behavior
     */
    @Override
    public void run() {
      /*
       * You may add code in this method; you may not delete code
       */
      floor.addDancer(this);
    }
  }

  /**
   * Shared dance floor
   */
  private class DanceFloor {

    /*
     * You may make minor tweaks to variables/method in this class;
     * you may not delete
     */
    // Maximum number of MJs on dance floor
    private static final int MAXMJS = 1;
    // Maximum number of zombies on dance floor
    private static final int MAXZOMBIES = 2;

    // Number of MJ impersonators
    private int mjCt = 0;
    // Number of zombies
    private int zombieCt = 0;

    /**
     * Add MJ impersonator dancer to the floor
     *
     * @param mj MJ impersonator dancer
     */
    public void addDancer(MJ mj) {
      try {
        if(mjLock.tryAcquire()) {
          mjCt++;
          // Test if too many MJs
          if(mjCt > MAXMJS) {
            System.err.println("Too many Michaels on the floor");
            System.exit(1);
          }
          System.out.println(mj + " is ready to dance!");

          finish();
          wall.await();
        }
      } catch(InterruptedException | BrokenBarrierException e) {
        error(e.getMessage());
      }
    }

    /**
     * Add zombie dancer to the floor
     *
     * @param z zombie dancer
     */
    public void addDancer(Zombie z) {
      try {
        if(zombieLock.tryAcquire()) {
          zombieCt++;
          // Test if too many zombies
          if(zombieCt > MAXZOMBIES) {
            System.err.println("Too many Zombies on the floor");
            System.exit(1);
          }
          System.out.println(z + " is ready to dance!");

          finish();
          wall.await();
        }
      } catch(InterruptedException | BrokenBarrierException e) {
        error(e.getMessage());
      }
    }

    // Reset count if last dancer
    protected void finish() {
      if(finishLock.tryAcquire()) {
        if(mjCt == 1 && zombieCt == 2) {
          mjCt = zombieCt = 0;
          mjLock.release();
          zombieLock.release(2);
        }
        finishLock.release();
      }
    }
  }
}
