package parlor;

/**
 * Class to manage a fortune teller parlor.
 * <p>
 * For all methods, you should handle interrupts internally. Do not let an
 * interrupt terminate a method or cause it to behave incorrectly.
 *
 * @version 1.0
 */
public interface Parlor {
  /**
   * Called by fortune teller to request next patron in order by arrival.
   * This method will block until a patron is available.  A patron
   * getting her fortune told gives up her parlor chair.  If the parlor
   * is closed, this method will return null.
   *
   * @return name of patron whose fortune is told
   */
  String tellFortune();

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
  boolean newPatron(String name);

  /**
   * This function marks the parlor as closed, causing threads blocked in
   * tellFortune() and newPatron() to return with false/null.  Threads
   * subsequently calling these function also immediately return with false/null.
   */
  void close();
}
