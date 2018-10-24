/*
 * Author: Justin Ritter
 * File: null.java
 * Date: 10/24/2018
 */
package fileio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.logging.Logger;

public class FileDataSource implements DataSource {

  private static final String RW = "rw";

  private static final String errNullBuffer = "Buffer cannot be null";
  private static final String errNegativeStart = "Start index cannot be negative";
  private static final String errNegativeLen = "Length cannot be negative";
  private static final String errOverflow = "Bounds exceed the buffer length";

  private static Logger logger;

  private RandomAccessFile file;

  /**
   * Construct byte source
   *
   * @param fileIn file containing bytes
   * @param log    logger to track operations.
   * @throws FileNotFoundException if given file is not found
   * @throws NullPointerException  if given file or log is null
   */
  public FileDataSource(File fileIn, Logger log) throws FileNotFoundException {
    setFile(fileIn);
    setLogger(log);
  }

  /**
   * Construct byte source
   *
   * @param fileName filename containing bytes
   * @param log      logger to track operations
   * @throws FileNotFoundException if given filename is not found
   * @throws NullPointerException  if given filename or log is null
   */
  public FileDataSource(String fileName, Logger log) throws FileNotFoundException {
    this(new File(fileName), log);
  }

  /**
   * file setter
   *
   * @param fileIn file containing bytes
   * @throws FileNotFoundException if given file is not found
   * @throws NullPointerException  if given file is null
   */
  private void setFile(File fileIn) throws FileNotFoundException {
    if(fileIn == null) {
      throw new NullPointerException();
    }
    file = new RandomAccessFile(fileIn, RW);
  }

  /**
   * logger setter
   *
   * @param log logger reference
   * @throws NullPointerException if given log is null
   */
  private void setLogger(Logger log) {
    if(log == null) {
      throw new NullPointerException();
    }
    logger = log;
  }

  @Override
  public Transaction newTransaction() {
    return new Transaction() {
      @Override
      public byte[] read(long startByte, int len) throws IOException {
        if(startByte + len > getLength()) {
          throw new IndexOutOfBoundsException(errOverflow);
        }
        if(startByte < 0) {
          throw new IndexOutOfBoundsException(errNegativeStart);
        } else if(len < 0) {
          throw new IndexOutOfBoundsException(errNegativeLen);
        }

        //TODO detect deadlock somehow

        return completeRead(startByte, len);
      }

      @Override
      public void write(byte[] buffer, long startByte) throws IOException {
        if(buffer == null) {
          throw new NullPointerException(errNullBuffer);
        }
        if(startByte < 0) {
          throw new IndexOutOfBoundsException(errNegativeStart);
        }
        //TODO detect deadlock somehow
        //TODO lock write

        file.write(buffer, (int)startByte, buffer.length);
      }

      @Override
      public void close() {
        //TODO completely release transaction resources?
        try(FileChannel fc = file.getChannel()) {
          fc.force(false);
        } catch(IOException e) {
          e.printStackTrace();
        }
      }

      /**
       * Guarantees that length number of bytes will be read from the channel
       * @param startByte starting position of read
       * @param length number of bytes to read
       * @return a byte[] representation of the bytes read from the channel
       * @throws IOException if IO error
       */
      private byte[] completeRead(long startByte, int length) throws IOException {
        ByteBuffer buff = ByteBuffer.allocate(length);
        FileChannel fc = file.getChannel();
        FileLock fl = fc.lock(startByte, length, false);

        //TODO finish completely reading
        int bytesRead = 0;
        while((bytesRead += fc.read(buff, startByte)) < length) {
          startByte += bytesRead;
        }
        fl.release();

        return buff.array();
      }
    };
  }

  @Override
  public long getLength() throws IOException {
    if(file.getFD().valid()) {
      return file.length();
    }
    return 0;
  }

  @Override
  public void close() throws IOException {
    file.close();
  }
}
