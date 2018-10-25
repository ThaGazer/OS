/*
 * Author: Justin Ritter
 * File: null.java
 * Date: 10/24/2018
 */
package fileio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileDataSource implements DataSource {

  private static final String RW = "rw";

  //error messages
  private static final String errNullBuffer = "Buffer cannot be null";
  private static final String errNegativeStart = "Start index cannot be negative: ";
  private static final String errNegativeLen = "Length cannot be negative: ";
  private static final String errOverflow = "Bounds exceed the buffer length: ";

  //console messages
  private static final String msgFileCreation = "Created file: ";
  private static final String msgCloseFileDataSource = "Closing FileDataSource";
  private static final String msgCloseTransaction = "Closing Transaction";
  //private static final String msgRead = "read bytes: ";
  //private static final String msgWrite = "writing bytes: ";

  private static Logger logger;

  private RandomAccessFile file;

  /**
   * Construct byte source
   *
   * @param fileIn file containing bytes
   * @param log    logger to track operations.
   * @throws NullPointerException if given file or log is null
   */
  public FileDataSource(File fileIn, Logger log) throws IOException {
    setLogger(log); /*always set the logger before anything*/
    setFile(fileIn);
  }

  /**
   * Construct byte source
   *
   * @param fileName filename containing bytes
   * @param log      logger to track operations
   * @throws NullPointerException if given filename or log is null
   */
  public FileDataSource(String fileName, Logger log) throws IOException {
    this(new File(fileName), log);
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

  /**
   * file setter
   *
   * @param fileIn file containing bytes
   * @throws NullPointerException if given file is null
   */
  private void setFile(File fileIn) throws IOException {
    if(fileIn == null) {
      throw new NullPointerException();
    }
    if(fileIn.createNewFile()) {
      logger.log(Level.INFO, msgFileCreation + fileIn.getName());
    }

    file = new RandomAccessFile(fileIn, RW);
  }

  @Override
  public Transaction newTransaction() {

    ArrayList<FileLock> fileLocks = new ArrayList<>();

    return new Transaction() {
      @Override
      public byte[] read(long startByte, int length) throws IOException {
        if(startByte + length > file.getChannel().size()) {
          throw new IndexOutOfBoundsException(errOverflow +
              "Position=" + startByte + " Length=" + length + " FileSize=" + getLength());
        }
        if(startByte < 0) {
          throw new IndexOutOfBoundsException(errNegativeStart + startByte);
        } else if(length < 0) {
          throw new IndexOutOfBoundsException(errNegativeLen + length);
        }

        //TODO detect deadlock somehow

        return completeRead(startByte, length);
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


        completeWrite(buffer, startByte);
      }

      @Override
      public void close() {
        logger.info(msgCloseTransaction);
        try {
          FileChannel fc = file.getChannel();
          fc.force(true);

          for(FileLock fl : fileLocks) {
            fl.release();
          }
        } catch(IOException e) {
          e.printStackTrace();
        }
      }

      /**
       * Guaranteed to read length number of bytes from channel
       * @param startPosition starting position of read
       * @param length number of bytes to read
       * @return a byte[] representation of the bytes read from the channel
       * @throws IOException if IO error
       */
      private byte[] completeRead(long startPosition, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        FileChannel fc = file.getChannel();

        //TODO is partial locking even needed?
        //partialLock(fc, startPosition, length);

        FileLock fl = fc.lock(startPosition, length, true);
        fc.read(buffer, fl.size());
        fl.release();
        return buffer.array();
      }

      /**
       * Guaranteed to write buffer.length() to channel
       * @param buffer buffer to write to channel
       * @param startPosition starting position in the channel
       */
      private void completeWrite(byte[] buffer, long startPosition) throws IOException {
        FileChannel fc = file.getChannel();

        //lock channel from startPosition to startPosition+buffer.length with an exclusive lock
        FileLock fl = fc.lock(startPosition, buffer.length, false);

        //partialLock(fc, startPosition, buffer.position());
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        while(byteBuffer.hasRemaining()) {
          fc.write(byteBuffer, startPosition);
        }
        fl.release();
      }

      /**
       * Produces a lock on the channel from startPosition to startPosition + length. If there already is a lock
       * it will try to produce a partial lock on the remaining bytes out of the original lock
       * @param fc the FileChannel to lock on
       * @param startPosition the starting position of the lock
       * @param length how long the lock is
       * @throws IOException if IO error
       */
      private void partialLock(FileChannel fc, long startPosition, int length) throws IOException {
        boolean foundOverlap = false;

        //search for a previous lock that overlaps
        for(FileLock fl : fileLocks) {
          if(fl.overlaps(startPosition, length)) {
            foundOverlap = true;

            //if this position is less than fl.position
            if(startPosition < fl.position()) {
              //lock over the extra beginning
              fileLocks.add(fc.lock(startPosition, (fl.position() - startPosition), true));
            }
            //if this range is greater than fl's range
            if((startPosition + length) > (fl.position() + fl.size())) {
              //lock over the extra ending
              fileLocks.add(fc.lock((fl.position() + fl.size()),
                  (startPosition + length) - (fl.position() + fl.size()), true));
            }
          }
        }

        if(!foundOverlap) {
          fileLocks.add(fc.lock(startPosition, length, true));
        }
      }
    };
  }

  @Override
  public long getLength() throws IOException {
    return file.length();
  }

  @Override
  public void close() throws IOException {
    logger.info(msgCloseFileDataSource);
    file.close();
  }
}
