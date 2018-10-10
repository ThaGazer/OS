/*
 * Author: Justin Ritter
 * File: null.java
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
        if(capacity < 0 || capacity >= Integer.MAX_VALUE) {
            this.capacity = capacity;
        } else {
            throw new IllegalArgumentException(errParams);
        }

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
        if(!isClosed()) {
            try {
                while(names.size() <= 0) {
                    if(isClosed()) {
                        return null;
                    }
                    tellerLock.acquire();
                }

                return names.remove(0);
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
        if(!isClosed()) {
            if(name == null) {
                throw new NullPointerException(errNullName);
            }

            try {
                parlorLock.acquire();
                boolean ret = false;
                if(names.size() < capacity) {
                    names.add(name);
                    ret = true;
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
        setClosed();
        tellerLock.notifyAll();
    }

    private synchronized boolean isClosed() {
        return closed;
    }

    private void setClosed() {
        try {
            closeLock.acquire();
            closed = true;
            closeLock.release();
        } catch(InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }
}
