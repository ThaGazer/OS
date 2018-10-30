/*
 * Author: Justin Ritter
 * File: DanceDriver.java
 * Date: 10/10/2018
 */
package dance;

import java.util.Random;

public class DanceDriver {

  private static final String msgMJDancing = " mj impersonator started dancing";
  private static final String msgZombieDancing = " zombie impersonator started dancing";

  DanceFloorBarrier danceFloor;

  public static void main(String[] args) {
    new DanceDriver().go();
  }

  public void go() {
    danceFloor = new DanceFloorBarrier();

    //spawns MJs
    new Thread(() -> {
      try {
        for(int i = 0; i < Integer.MAX_VALUE; i++) {
          new Thread(new MJ(String.valueOf(i))).start();

          Thread.sleep(new Random().nextInt(500));
        }
      } catch(InterruptedException e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
    }).start();

    //spawns Zombies
    new Thread(() -> {
      try {
        for(int i = 0; i < Integer.MAX_VALUE; i++) {
          new Thread(new Zombie(String.valueOf(i))).start();

          Thread.sleep(new Random().nextInt(250));
        }
      } catch(InterruptedException e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
    }).start();
  }

  private class MJ implements Runnable {

    private String name;

    public MJ(String name) {
      this.name = name;
    }

    @Override
    public void run() {
      danceFloor.michaelStart();

      System.out.println(name + msgMJDancing);

      //dance for a random amount of time
      try {
        Thread.sleep(new Random().nextInt(500));
      } catch(InterruptedException e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }

      danceFloor.michaelStop();
    }
  }

  private class Zombie implements Runnable {

    private String name;

    public Zombie(String name) {
      this.name = name;
    }

    @Override
    public void run() {
      danceFloor.zombieStart();
      System.out.println(name + msgZombieDancing);
      danceFloor.zombieStop();
    }
  }
}


