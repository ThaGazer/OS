/*
 * KnifeLock
 * Version 1.0 created 9/27/2017
 *
 * Description: implements knifeTag class using java locks
 *
 * Note: I attempted the extra credit and believe I should be considered for it
 * Author:
 *  -Justin Ritter
 */
package Tag.knifetag;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class KnifeLock extends KnifeTag {

    private BlockingQueue<Integer> que =
            new ArrayBlockingQueue<>(MAXCOMPETITORS);
    private BlockingQueue<Integer> janitorQue =
            new ArrayBlockingQueue<>(1);

    /**
     * Execute before cleaning
     */
    @Override
    protected void preClean() {
        if(que.remainingCapacity() == MAXCOMPETITORS) {
            try {
                janitorQue.put(numCompetitors);
                numCompetitors = 0;
            } catch (InterruptedException e) {
                System.out.println(funcPreCle + e.getMessage());
            }
        }
    }

    /**
     * Execute after cleaning
     */
    @Override
    protected void postClean() {
        try {
            janitorQue.take();
        } catch (InterruptedException e) {
            System.out.println(funcPostCle + e.getMessage());
        }
    }

    /**
     * Execute before competing
     */
    @Override
    protected void preCompete() {
        try {
            if(!janitorQue.isEmpty()) {
                que.put(numCompetitors);
                numCompetitors = numCompetitors + 1;
            }
        } catch (InterruptedException e) {
            System.out.println(funcPreCom + e.getMessage());
        }
    }

    /**
     * Execute after competing
     */
    @Override
    protected void postCompete() {
        try {
            numCompetitors = numCompetitors - 1;
            que.take();
        } catch (InterruptedException e) {
            System.out.println(funcPostCom + e.getMessage());
        }
    }
}