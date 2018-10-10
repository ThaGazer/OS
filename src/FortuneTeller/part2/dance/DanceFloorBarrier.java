/*
 * Author: Justin Ritter
 * File: null.java
 * Date: 10/10/2018
 */
package FortuneTeller.part2.dance;

public class DanceFloorBarrier implements DanceFloor {
    /**
     * Called by Michael Jackson impersonator when ready to dance. This method will
     * block until the preconditions are met (i.e., two zombies and one MJ ready to
     * dance and nobody is dancing). Only one MJ and two zombies should return at a
     * time.
     */
    @Override
    public void michaelStart() {

    }

    /**
     * Called by Michael Jackson impersonator when done dancing.  This should only
     * be called by an MJ who has called michaelStart().  This does not block.
     */
    @Override
    public void michaelStop() {

    }

    /**
     * Called by a zombie when ready to dance. This method will block until the
     * preconditions are met (i.e., two zombies and one MJ ready to dance and nobody
     * is dancing).
     */
    @Override
    public void zombieStart() {

    }

    /**
     * Called by a zombie immediately after it starts dancing.  This should only
     * be called by a zombie who has called zombieStart().  This blocks
     * until the corresponding michaelStop() is called, in which case it
     * immediately returns.
     */
    @Override
    public void zombieStop() {

    }
}
