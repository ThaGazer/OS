/*
 * Author: Justin Ritter
 * File: IOAgent.java
 * Date: 10/24/2018
 */
package fileio;

import java.io.IOException;

/**
 * A transaction for reading/writing from/to a byte source.  Indicies start
 * at 0.
 *
 * @version 1.0
 */
public interface Transaction {

  /**
   * Reads specified range of bytes (inclusive) from source
   *
   * @param startByte start index for bytes to read
   * @param len       number of bytes to read
   * @return array of bytes read
   * @throws IOException               if specified bytes cannot be read for some reason
   * @throws DeadlockDetectedException if attempting this read would create a deadlock
   * @throws IndexOutOfBoundsException if startByte or length is negative, or
   *                                   startByte+len is beyond the end of the byte source
   */
  byte[] read(final long startByte, final int len) throws IOException;

  /**
   * Writes buffer starting at specified offset to source.  Writing is permitted past the
   * current end of the byte source.  The values of data between the current end of the byte
   * source and the data buffer are undefined.
   *
   * @param buffer    data to write
   * @param startByte start index for writing
   * @throws IOException               if specified bytes cannot be written for some reason
   * @throws DeadlockDetectedException if attempting this read would create a deadlock
   * @throws IndexOutOfBoundsException if startByte is negative
   * @throws NullPointerException      if buffer is null
   */
  void write(final byte[] buffer, final long startByte) throws IOException;

  /**
   * Close this transaction, releasing all transaction resources.  This does not close
   * the underlying DataSource.
   */
  void close();
}
