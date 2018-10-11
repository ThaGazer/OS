/*
 * Author: Justin Ritter
 * File: ParlorMonitor.java
 * Date: 10/10/2018
 */
package FortuneTeller.part2.parlor;

import java.util.ArrayList;

public class ParlorMonitor implements Parlor {

    private static final String errParams = "improper capacity size";
    private static final String errNullName = "everyone must have a name";

    private ArrayList<String> names;
    private int capacity;
    private boolean closed = false;

    public ParlorMonitor(int capacity) {
        if(capacity < 0 || capacity >= Integer.MAX_VALUE) {
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
        if(!isClosed()) {
            synchronized(this) {
                while(names.size() <= 0) {
                    if(isClosed()) {
                        return null;
                    }
                    try {
                        wait();
                    } catch(InterruptedException e) {
                        System.err.println(e.getMessage());
                        return null;
                    }
                }

                return names.remove(0);
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

            synchronized(this) {
                if(names.size() < capacity) {
                    names.add(name);
                    return true;
                }
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
        synchronized(this) {
            setClosed();
            notifyAll();
        }
    }

    private synchronized boolean isClosed() {
        return closed;
    }

    private void setClosed() {
        closed = true;
    }
}
