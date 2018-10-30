package dance;

/**
 * Class to manage a dance floor.
 * <p>
 * For all methods, you should handle interrupts internally. Do not let an
 * interrupt terminate a method or cause it to behave incorrectly.
 *
 * @version 1.0
 */
public interface DanceFloor {
  /**
   * Called by Michael Jackson impersonator when ready to dance. This method will
   * block until the preconditions are met (i.e., two zombies and one MJ ready to
   * dance and nobody is dancing). Only one MJ and two zombies should return at a
   * time.
   */
  void michaelStart();

  /**
   * Called by Michael Jackson impersonator when done dancing.  This should only
   * be called by an MJ who has called michaelStart().  This does not block.
   */
  void michaelStop();

  /**
   * Called by a zombie when ready to dance. This method will block until the
   * preconditions are met (i.e., two zombies and one MJ ready to dance and nobody
   * is dancing).
   */
  void zombieStart();


  /**
   * Called by a zombie immediately after it starts dancing.  This should only
   * be called by a zombie who has called zombieStart().  This blocks
   * until the corresponding michaelStop() is called, in which case it
   * immediately returns.
   */
  void zombieStop();
}
