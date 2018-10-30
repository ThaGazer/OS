/*
 * Author: Justin Ritter
 * File: null.java
 * Date: 10/20/2018
 */
package fileio;

import java.io.IOException;

/**
 * Thrown to indicate an operation (if allowed) may have caused deadlock
 */
public class DeadlockDetectedException extends IOException {

  private static final long serialVersionUID = 1L;

  private String cycle;

  /**
   * Constructs exception with given cycle and null message
   *
   * @param cycle string representation of cycle causing deadlock
   */
  public DeadlockDetectedException(final String cycle) {
    this(null, cycle);
  }

  /**
   * Constructs exception with given message and cycle and n
   *
   * @param message detail message
   * @param cycle   string representation of cycle causing deadlock
   */
  public DeadlockDetectedException(final String message, final String cycle) {
    super(message);
    this.cycle = cycle;
  }

  /**
   * Get a string representation of cycle
   *
   * @return string representative of cycle
   */
  public String getCycle() {
    return cycle;
  }
}
