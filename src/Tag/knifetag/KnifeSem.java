/*
 * KnifeSem
 * Version 1.0 created 9/27/2017
 *
 * Description: implements knifeTag class using semaphores
 *
 * Note: I attempted the extra credit and believe I should be considered for it
 * Author:
 *  -Justin Ritter
 */
package Tag.knifetag;

import java.util.concurrent.Semaphore;

public class KnifeSem extends KnifeTag {
    private Semaphore sem = new Semaphore(MAXCOMPETITORS);
    private Semaphore numberCompetLock = new Semaphore(1);
    private Semaphore janitor = new Semaphore(1);

    /**
     * acquires the janitor lock to block the competitors from entering
     * if there are no other competitors in the arena
     */
    @Override
    protected void preClean() {
        try {
            if(sem.availablePermits() == MAXCOMPETITORS) {
                janitor.acquire();
                numCompetitors = 0;
            }
        } catch(InterruptedException e) {
            System.out.println(funcPreCle + e.getMessage());
        }
    }

    /**
     * opens the arena to start play again
     */
    @Override
    protected void postClean() {
        janitor.release();
    }

    /**
     * Checks to make sure the janitor is not cleaning
     * then tries to enter the arena.
     * If can enter increment number of competitors by 1
     */
    @Override
    protected void preCompete() {
        try {
            if(janitor.availablePermits() == 1) {
                sem.acquire();

                numberCompetLock.acquire();
                numCompetitors = numCompetitors + 1;
                numberCompetLock.release();
            }
        } catch(InterruptedException e) {
            System.out.println(funcPreCom + e.getMessage());
        }
    }

    /**
     * decreases the number of competitors and releases a
     * slot for another competitor to enter
     */
    @Override
    protected void postCompete() {
        try {
            numberCompetLock.acquire();
            numCompetitors = numCompetitors - 1;
            numberCompetLock.release();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        sem.release();
    }

}
