/*
 * Author: Justin Ritter
 * File: ParlorSemaphore.java
 * Date: 10/10/2018
 */
package FortuneTeller.part2.parlor;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class ParlorSemaphore implements Parlor {

    private static final String errParams = "improper capacity size";
    private static final String errNullName = "everyone must have a name";

    private Semaphore closeLock = new Semaphore(1);
    private Semaphore parlorLock = new Semaphore(1);
    private Semaphore tellerLock = new Semaphore(1);

    private boolean closed = false;
    private int capacity;
    private ArrayList<String> names;

    public ParlorSemaphore(int capacity) {
        if(capacity < 0) {
            throw new IllegalArgumentException(errParams);
        }

        this.capacity = capacity;
        names = new ArrayList<>(capacity);
    }

    /**
     * Called by fortune teller to request next patron in order by arrival.
     * This method will block until a patron is available.  A patron
     * getting her fortune told gives up her parlor chair.  If the parlor
     * is closed, this method will return null.
     *
     * @return name of patron whose fortune is told
     */
    @Override
    public String tellFortune() {
        //if shop is closed
        if(!isClosed()) {
            try {
                //
                while(names.size() <= 0) {
                    //needed if no one is in the shop and the parlor recently closed
                    if(isClosed()) {
                        return null;
                    }
                    tellerLock.acquire();
                }

                //tells fortune
                parlorLock.acquire();
                String name = names.remove(0);
                parlorLock.release();
                return name;
            } catch(InterruptedException e) {
                System.err.println(e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Adds a new patron with given name to the parlor.  The parlor has a finite
     * number of chairs (given to the constructor).  If there is no available chair
     * in the parlor, this method immediately returns false; otherwise, this method
     * immediately returns true. This method immediately returns false when the shop
     * is closed.
     *
     * @param name name of patron
     * @return true if patron was added or false if the parlor was full or closed
     * @throws NullPointerException if name is null
     */
    @Override
    public boolean newPatron(String name) {
        //if shop closed nothing to do
        if(!isClosed()) {
            //if name is null throw something at them
            if(name == null) {
                throw new NullPointerException(errNullName);
            }

            try {
                //attempts to enter parlor
                parlorLock.acquire();

                boolean ret = false;

                //adds name to queue if parlor is below capacity
                if(names.size() < capacity) {
                    names.add(name);
                    ret = true;
                    //tell the teller you are here
                    tellerLock.release();
                }
                parlorLock.release();
                return ret;
            } catch(InterruptedException e) {
                System.err.println(e.getMessage());
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * This function marks the parlor as closed, causing threads blocked in
     * tellFortune() and newPatron() to return with false/null.  Threads
     * subsequently calling these function also immediately return with false/null.
     */
    @Override
    public void close() {
        try {
            closeLock.acquire();
            setClosed();
            tellerLock.release();
            closeLock.release();
        } catch(InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * returns a flag if the parlor is closed or not
     * @return closed flag
     */
    private synchronized boolean isClosed() {
        return closed;
    }

    /**
     * Sets the closed flag
     */
    private void setClosed() {
        closed = true;
    }
}
