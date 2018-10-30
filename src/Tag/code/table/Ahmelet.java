/*
 * Ahmelet
 * Version 1.0 created 9/27/2017
 *
 * Description: a rather weird competition of cooking where
 *      you only get one utensils and have to fight for the other two
 * Author:
 *  -Justin Ritter
 */
package table;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Ahmelet extends Table {

  BlockingQueue<Item> itemQueue = new ArrayBlockingQueue<>(3);

  @Override
  protected void preStartAddItems(Item addItem1, Item addItem2) {

  }

  @Override
  protected void postStartAddItems(Item addItem1, Item addItem2) {

  }

  @Override
  protected void preCook(Item item) {

  }

  @Override
  protected void postCook(Item item) {

  }
}
