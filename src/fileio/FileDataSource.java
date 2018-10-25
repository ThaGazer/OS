/*
 * Author: Justin Ritter
 * File: null.java
 * Date: 10/24/2018
 */
package fileio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileDataSource implements DataSource {

  private static final String RW = "rw";

  //error messages
  private static final String errNullBuffer = "Buffer cannot be null";
  private static final String errNegativeStart = "Start index cannot be negative: ";
  private static final String errNegativeLen = "Length cannot be negative: ";
  private static final String errOverflow = "Bounds exceed the buffer length: ";
  private static final String errDeadlock = "Potential for deadlock";

  //console messages
  private static final String msgFileCreation = "Created file: ";
  private static final String msgCloseFileDataSource = "Closing FileDataSource";
  private static final String msgCloseTransaction = "Closing Transaction";

  private static final String DeadlockFileName = "src/fileio/lockTransfer.txt";

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
    return new Transaction() {
      ArrayList<FileLock> fileLocks = new ArrayList<>();
      private boolean isClosed = false;

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

        //deadlock check
/*        if(deadlockDetection()) {
          throw new DeadlockDetectedException(errDeadlock);
        }*/

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

        //deadlock check
/*        if(deadlockDetection()) {
          throw new DeadlockDetectedException(errDeadlock);
        }*/

        completeWrite(buffer, startByte);
      }

      @Override
      public void close() {
        logger.info(msgCloseTransaction);
        isClosed = true;

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

        //lock channel from startPosition to startPosition+buffer.length with a shared lock
        partialLock(fc, startPosition, length, true);

        fc.read(buffer, length);
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
        partialLock(fc, startPosition, buffer.length, false);

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        while(byteBuffer.hasRemaining()) {
          fc.write(byteBuffer, startPosition);
        }
      }

      /**
       * Produces a lock on the channel from startPosition to startPosition + length. If there already is a lock
       * it will try to produce a partial lock on the remaining bytes out of the original lock
       * @param fc the FileChannel to lock on
       * @param startPosition the starting position of the lock
       * @param length how long the lock is
       * @throws IOException if IO error
       */
      private void partialLock(FileChannel fc, long startPosition, int length, boolean shared) throws IOException {
        boolean foundOverlap = false;

        try {
          //search for a previous lock that overlaps
          fileLocks.sort((o1, o2) -> (int)(o1.position() - o2.position()));

          ArrayList<FileLock> tempList = new ArrayList<>();
          for(int i = 0; i < fileLocks.size(); i++) {
            FileLock fl = fileLocks.get(i);
            if(fl.overlaps(startPosition, length)) {
              foundOverlap = true;

              //if this position is less than fl.position else if this range is greater than fl's range
              if(startPosition < fl.position()) {
                //lock over the extra beginning
                tempList.add(fc.lock(startPosition, (fl.position() - startPosition)-2, shared));
              } else if((startPosition + length) > (fl.position() + fl.size())) {
                long end = (startPosition + length) - ((fl.position() + fl.size()) + 1);

                if(i < fileLocks.size()-1) {
                  if(end >= fileLocks.get(i + 1).position()) {
                    end = fileLocks.get(i + 1).position() - 1;
                  }
                }
                //lock over the extra ending
                tempList.add(fc.lock((fl.position() + fl.size()) + 1, end, shared));
              }
            }
          }
          fileLocks.addAll(tempList);

          if(!foundOverlap) {
            fileLocks.add(fc.lock(startPosition, length, shared));
          }
        } catch(OverlappingFileLockException ofle) {
          throw new IOException("Start: " + startPosition +
              " Length: " + length + Arrays.toString(fileLocks.toArray()));
        }
      }

      /**
       * An attempt to discover deadlock between processes
       * @return if deadlock is possible
       * @throws IOException if IO error
       */
      private boolean deadlockDetection() throws IOException {
        File transferFile = new File(DeadlockFileName);

        //create a new transfer lock file if it doesn't already exist
        if(transferFile.createNewFile()) {
          logger.info(msgFileCreation + DeadlockFileName);
        }
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(transferFile));
        ArrayList<Long> lockPos = new ArrayList<>();
        for(FileLock fl : fileLocks) {
          lockPos.add(fl.position());
        }
        out.writeObject(lockPos);


        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(transferFile))) {
          ArrayList<Long> locksIn = new ArrayList<>((ArrayList<Long>)in.readObject());

            //if locksIn contains any lock from fileLocks and we haven't close yet possible deadlock
            for(FileLock fl : fileLocks) {
              if(locksIn.contains(fl.position()) && !isClosed) {
                return true;
              }
            }
        } catch(Exception e) {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
        return false;
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
