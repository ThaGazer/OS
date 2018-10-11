/*
 * Author: Justin Ritter
 * File: null.java
 * Date: 10/10/2018
 */
package FortuneTeller.part2.dance;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class DanceFloorBarrier implements DanceFloor {

    private Semaphore mjLock = new Semaphore(1);
    private Semaphore zomLock = new Semaphore(2);
    private Semaphore zomStop = new Semaphore(0);
    private Semaphore finishLock = new Semaphore(1);
    private CyclicBarrier wall = new CyclicBarrier(3);

    private boolean mjStart = false;
    private int zomStart = 0;

    private void finish() {
        if(finishLock.tryAcquire()) {
            if(mjStart && zomStart == 2) {
                mjStart = false;
                zomStart = 0;
                mjLock.release();
                zomLock.release();
                zomLock.release();
            }
            finishLock.release();
        }
    }

    private void error(String errMessage) {
        System.err.println(errMessage);
        System.exit(1);
    }

    /**
     * Called by Michael Jackson impersonator when ready to dance. This method will
     * block until the preconditions are met (i.e., two zombies and one MJ ready to
     * dance and nobody is dancing). Only one MJ and two zombies should return at a
     * time.
     */
    @Override
    public void michaelStart() {
        try {
            mjLock.acquire();
            mjStart = true;
            wall.await();
        } catch(InterruptedException | BrokenBarrierException e) {
            error(e.getMessage());
        }
    }

    /**
     * Called by Michael Jackson impersonator when done dancing.  This should only
     * be called by an MJ who has called michaelStart().  This does not block.
     */
    @Override
    public void michaelStop() {
        if(mjStart) {
            zomStop.release();
            zomStop.release();
        }
    }

    /**
     * Called by a zombie when ready to dance. This method will block until the
     * preconditions are met (i.e., two zombies and one MJ ready to dance and nobody
     * is dancing).
     */
    @Override
    public void zombieStart() {
        try {
            zomLock.acquire();
            zomStart++;
            wall.await();
        } catch(InterruptedException | BrokenBarrierException e) {
            error(e.getMessage());
        }
    }

    /**
     * Called by a zombie immediately after it starts dancing.  This should only
     * be called by a zombie who has called zombieStart().  This blocks
     * until the corresponding michaelStop() is called, in which case it
     * immediately returns.
     */
    @Override
    public void zombieStop() {
        if(zomStart != 0) {
            try {
                zomStop.acquire();
                finish();
            } catch(InterruptedException e) {
                error(e.getMessage());
            }
        }
    }
}
