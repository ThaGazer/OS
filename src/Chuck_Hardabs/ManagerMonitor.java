/*
 * Chuck_Hardabs:ManagerMonitor
 * Created on 2/20/2018
 *
 * Author(s):
 * -Justin Ritter
 */
package Chuck_Hardabs;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class ManagerMonitor {
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

    public static void main(String[] args) {
        new ManagerMonitor().go();

    }

    private void go() {
        // Create the celebrity thread
        Celebrity c = new Celebrity();
        new Thread(c, "Celebrity").start();

        // Continually generate new fans
        int i = 0;
        while (true) {
            new Thread(new Fan(), "Fan " + i++).start();
            try {
                Thread.sleep(rndGen.nextInt(MAX_TIME_IN_BETWEEN_ARRIVALS));
            } catch (InterruptedException e) {
                System.err.println(e.toString());
                System.exit(1);
            }
        }
    }

    private synchronized void enque() throws InterruptedException {
        while(isFull()) {
            wait();
        }
        notify();
    }

    private synchronized void deque() throws InterruptedException {
        while(isEmpty()) {
            wait();
        }
        notify();
    }

    private boolean isFull() {
        synchronized(this) {
            return numFansInLine >= MAX_ALLOWED_IN_QUEUE-1;
        }
    }

    private boolean isEmpty() {
        synchronized(this) {
            return numFansInLine <= MIN_FANS;
        }
    }

    class Celebrity implements Runnable
    {
        @Override
        public void run() {
            while (true)
            {
                try {
                    deque();
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                // Check to see if celebrity flips out
                synchronized(this) {
                    checkCelebrityOK();
                }

                // Take picture with fans
                System.out.println("Celebrity takes a picture with fans");

                // Remove the fans from the line
                for(int i = 0; i < MIN_FANS; i++) {
                    System.out.println(line.remove(0).getName() + ": OMG! Thank you!");
                }

                // Adjust the numFans variable
                synchronized(this) {
                    numFansInLine -= MIN_FANS;
                }

                // Take a break
                try {
                    Thread.sleep(rndGen
                            .nextInt(MAX_BREAK_TIME));
                } catch (InterruptedException e) {
                    System.err.println(e.toString());
                    System.exit(1);
                }
            }
        }

    }

    public void checkCelebrityOK()
    {
        if (numFansInLine > MAX_ALLOWED_IN_QUEUE)
        {
            System.err.println("Celebrity becomes claustrophobic and flips out");
            System.exit(1);
        }

        if (numFansInLine < MIN_FANS)
        {
            System.err.println("Celebrity becomes enraged that he was woken from nap for too few fans");
            System.exit(1);
        }
    }

    class Fan implements Runnable
    {
        String name;

        public String getName()
        { return name;}

        @Override
        public void run() {

            // Set the thread name
            name = Thread.currentThread().toString();
            System.out.println(Thread.currentThread() + ": arrives");

            // Look in the exhibit for a little while
            try {
                Thread.sleep(rndGen.nextInt(MAX_EXHIBIT_TIME));
            } catch (InterruptedException e) {
                System.err.println(e.toString());
                System.exit(1);
            }

            //waits for a spot in line
            try {
                enque();
            } catch(InterruptedException e) {
                System.err.println();
                System.exit(1);
            }

            // Get in line
            System.out.println(Thread.currentThread() + ": gets in line");
            line.add(0, this);
            synchronized(this) {
                numFansInLine++;
            }
        }
    }
}
