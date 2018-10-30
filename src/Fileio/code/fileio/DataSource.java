/*
 * Author: Justin Ritter
 * File: null.java
 * Date: 10/20/2018
 */
package fileio;

import java.io.IOException;

/**
 * A source of bytes with transactions that can read/write bytes
 * <p>
 * version 1.0
 */
public interface DataSource extends AutoCloseable {

  /**
   * Get a new transaction for reading/writing source bytes
   *
   * @return new transaction
   */
  Transaction newTransaction();

  /**
   * Get the number of bytes in the data source currently
   *
   * @return number of bytes in source
   * @throws IOException if error determining the number of bytes
   */
  long getLength() throws IOException;

  /**
   * Close all open transactions and then this data source
   *
   * @throws IOException if an I/O occurs
   */
  void close() throws IOException;
}
