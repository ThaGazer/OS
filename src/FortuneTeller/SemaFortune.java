/*
 * Author: Justin Ritter
 * File: SemaFortune.java
 * Date: 9/26/2018
 */
package FortuneTeller;

// You may only add code where indicated

import java.util.Date;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class SemaFortune {
    // Maximum fortune telling time (ms)
    private static final int MAXFORTUNETELLINGTIME = 3000;
    // Maximum time between patron arrive (ms)
    private static final int MAXPATRONINTERARRIVALTIME = 100;
    // Maximum number of patrons in shop
    private static final int MAXPARLORCAPACITY = 4;

    // Total number of patrons
    private int patronCt = 0;
    // Number of patrons receiving their fortune (should be a max of 1)
    private AtomicInteger ct = new AtomicInteger(0);
    // Generator for random events
    private Random rndGen = new Random(new Date().getTime());

    /*
     * You may add variables here
     */
    private int balkRate = 0; /*number of patrons turned away*/
    private Semaphore crystalBallLock = new Semaphore(0); /*only allow one patron access to the crystal ball*/
    private Semaphore fortuneLock = new Semaphore(1); /*awaken teller*/
    private Semaphore lock = new Semaphore(0); /*data mutex lock*/
    private Semaphore patronLock = new Semaphore(MAXPARLORCAPACITY); /*only allow 4 patrons to sit in the parlor*/

    public void go() {
        Teller teller = new Teller();
        new Thread(teller, "Teller").start();

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            new Thread(new Patron("Patron " + i++)).start();
            try {
                Thread.sleep(rndGen.nextInt(MAXPATRONINTERARRIVALTIME));
            } catch (InterruptedException e) {
            }
        }
    }

    public static void main(String[] args) {
        new SemaFortune().go();
    }

    protected static void error(final String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    class Teller implements Runnable {

        @Override
        public void run() {
            /*
             * You may add code in this method; you may not delete code
             */
            try {
                //initializes lock to be locked on first run through


                while (true) {
                    //wait for patron to show up
                    crystalBallLock.acquire();

                    //telling fortune
                    tellFortune();

                    //tell tell the patron that their fortune is ready
                    lock.release();
                }
            } catch (InterruptedException e) {
                error(e.getMessage());
            }
        }

        public void tellFortune() {
            if (patronCt < 1) {
                error("Fortune teller arrested on tax fraud for over reporting sessions (Telling fortune to nobody)");
            }
            System.out.println("Fortune teller telling");
            int c = ct.incrementAndGet();
            if (c < -1 || c > 1) {
                error("Outta phase " + c);
            }
/*            try { // Telling
                Thread.sleep(rndGen.nextInt(MAXFORTUNETELLINGTIME));
            } catch (InterruptedException e) {
            }*/
        }
    }

    class Patron implements Runnable {

        private String name;

        public Patron(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            /*
             * You may add code in this method; you may not delete code
             */
            try {
                //check for open seat in parlor
                if(patronLock.tryAcquire()) {
                    patronCt++;
                    if (patronCt > MAXPARLORCAPACITY + 1) {
                        error("Town unprepared for flooding after distracted fortune teller gives bad advice to weatherman (Too many patrons in parlor)");
                    }

                    //patron wants their fortune
                    fortuneLock.acquire();
                    //tell fortune teller that you are here
                    crystalBallLock.release();
                    //allow another patron to enter parlor
                    patronLock.release();

                    //wait for fortune to finish
                    lock.acquire();
                    getFortune();
                    patronCt--;

                    //tell other patrons that the crystal ball is open
                    fortuneLock.release();
                } else {
                    //added bulk counter
                    handleShopFull();
                }
            } catch (InterruptedException e) {
                error(e.getMessage());
            }
        }

        public void getFortune() {
            System.out.println(name + " fortune told");
            ct.decrementAndGet();
        }

        public synchronized void handleShopFull() {
            // Could add counter here to compute balk rate
            balkRate++;
        }
    }
}
