/*
 * Author: Justin Ritter
 * File: ParlorDriver.java
 * Date: 10/10/2018
 */
package parlor;

import java.util.ArrayList;
import java.util.Random;

public class ParlorDriver {

  private static final String errParams = "Usage: <[Semaphore:0|Monitor:1]> <capacity>";
  private static final String errParam1 = "improperly formatted class option";
  private static final String errParam2 = "improperly formatted capacity";

  private static final String msgClosed = "shop is close for the day";
  private static final String msgFortune = "Fortune teller telling ";
  private static final String msgPatronsHandled = "Teller helped: ";
  private static final String msgTookaSeat = " took a seat in the parlor";
  private static final String msgNextStore = " continued on to find a new store";

  private Parlor parlor;
  ArrayList<Thread> threadList = new ArrayList<>();

  public static void main(String[] args) {
    new ParlorDriver().go(args);
  }

  /**
   * method to run in main
   * @param args args passed to the program
   */
  public void go(String[] args) {
    if(args.length != 2) {
      throw new IllegalArgumentException(errParams);
    }

    //Setup for the driver. Determines which interface to use and how many seats to add
    setInterface(args);

    //create a new teller
    Thread thread = new Thread(new Teller());
    thread.start();
    threadList.add(thread);

    //patron spawner thread
    thread = new Thread(() -> {
      for(int i = 0; i < Integer.MAX_VALUE; i++) {
        //start a new patron with a name of i
        Thread pThread = new Thread(new Patron(String.valueOf(i)));
        pThread.start();
        threadList.add(pThread);
      }
    });
    thread.start();
    threadList.add(thread);

    //close the parlor at a random point
    try {
      Thread.sleep(new Random().nextInt(Integer.MAX_VALUE));
    } catch(InterruptedException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    parlor.close();


    //wait for threads to finish
    for(Thread t : threadList) {
      try {
        t.join();
      } catch(InterruptedException e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
    }
  }

  /**
   *
   * @param args args[0] hold which sub-interface version to use
   *             args[1] number of seats available
   */
  private void setInterface(String[] args) {
    try {
      switch(Integer.parseInt(args[0])) {
        case 0:
          parlor = new ParlorSemaphore(Integer.parseInt(args[1]));
          break;
        case 1:
          parlor = new ParlorMonitor(Integer.parseInt(args[1]));
          break;
        default:
          throw new IllegalArgumentException(errParam1);
      }
    } catch(NumberFormatException e) {
      System.err.println(errParam2);
      System.exit(1);
    }
  }

  /**
   * Runs a teller for the parlor
   */
  private class Teller implements Runnable {

    private int patronsHandled = 0;

    @Override
    public void run() {
      while(true) {
        //attempts to tell someones fortune
        String patron = parlor.tellFortune();

        /*if tellFortune return a null reference then the shop is closed
        else print the patrons name and try again*/
        if(patron == null) {
          System.out.println(msgClosed);
          break;
        } else {
          System.out.println(msgFortune + patron);
          patronsHandled++;
        }
      }

      //if shop closes print the number of patrons helped
      System.out.println(msgPatronsHandled + patronsHandled);
      System.exit(0);
    }
  }

  /**
   * Acts as a patron entering the parlor
   */
  private class Patron implements Runnable {

    private String name; /*name of the patron*/

    /**
     * Constructs a new Patron with name
     * @param name name of the new Patron
     */
    public Patron(String name) {
      this.name = name;
    }

    @Override
    public void run() {
      //tries to take a seat in the parlor
      if(parlor.newPatron(name)) {
        System.out.println(name + msgTookaSeat);
      } else {
        System.out.println(name + msgNextStore);
      }
    }
  }
}
