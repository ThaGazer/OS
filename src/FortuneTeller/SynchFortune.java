/*
 * Author: Justin Ritter
 * File: SemaFortune.java
 * Date: 9/26/2018
 */
package FortuneTeller;

// You may only add code where indicated

import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class SynchFortune {
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
    private int bulkRate = 0; /*number of patrons turned away*/
    private int crystalBall = 1; /*only allow one patron access to the crystal ball*/
    private final Object fortuneLock = new Object(); /*tell patron when fortune is ready*/

    public void go() {
        Teller teller = new Teller();
        new Thread(teller, "Teller").start();

        for(int i = 0; i < Integer.MAX_VALUE; i++) {
            new Thread(new Patron("Patron " + i++)).start();
            try {
                Thread.sleep(rndGen.nextInt(MAXPATRONINTERARRIVALTIME));
            } catch(InterruptedException e) {
            }
        }
    }

    public static void main(String[] args) {
        new SynchFortune().go();
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
            while(true) {

                synchronized(Teller.class) {
                    try {
                        //wait for patron to come to crystal ball
                        Teller.class.wait();
                    } catch(InterruptedException e) {
                        error(e.getMessage());
                    }
                    //tell them their fortune
                    tellFortune();
                }

                synchronized(fortuneLock) {
                    //notify patron that their fortune is ready
                    fortuneLock.notify();
                    //allow someone else to the crystal ball
                    crystalBall++;
                }
            }
        }

        public void tellFortune() {
            if(patronCt < 1) {
                error("Fortune teller arrested on tax fraud for overreporting sessions (Telling fortune to nobody)");
            }
            System.out.println("Fortune teller telling");
            int c = ct.incrementAndGet();
            if(c < -1 || c > 1) {
                error("Outta phase " + c);
            }
            try { // Telling
                Thread.sleep(rndGen.nextInt(MAXFORTUNETELLINGTIME));
            } catch(InterruptedException e) {
            }
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
                if(patronCt < MAXPARLORCAPACITY) {
                    synchronized(Patron.class) {
                        patronCt++;
                        if(patronCt > MAXPARLORCAPACITY + 1) {
                            error("Town unprepared for flooding after distracted fortune teller gives bad advice to weatherman (Too many patrons in parlor)");
                        }
                    }

                    synchronized(Patron.class) {
                        //only one patron to crystal ball
                        while(crystalBall < 1) {
                            Patron.class.wait();
                        }
                        crystalBall -= 1;
                    }

                    synchronized(Teller.class) {
                        //awaken teller
                        Teller.class.notify();
                    }

                    synchronized(fortuneLock) {
                        //wait for fortune to be ready
                        fortuneLock.wait();
                        getFortune();

                        synchronized(Patron.class) {
                            //tell the next patron that the crystal ball is open
                            Patron.class.notify();
                            patronCt--;
                        }
                    }
                } else {
                    //System.out.println("turned away: " + name);
                    handleShopFull();
                }
            } catch(InterruptedException e) {
                error(e.getMessage());
            }
        }

        public void getFortune() {
            System.out.println(name + " fortune told");
            ct.decrementAndGet();
        }

        public void handleShopFull() {
            // Could add counter here to compute balk rate
            bulkRate++;
        }
    }
}