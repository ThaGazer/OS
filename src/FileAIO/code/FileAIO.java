/*
 * Author: Justin Ritter
 * File: FileAIO.java
 * Date: 11/23/2018
 *
 * Description: Read integers from one file and
 * find how many exist in another file
 */

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

public class FileAIO {

  private static final String LOGFILE = "aio.log";

  private static final String msgPoolShutDown = "pool has been shutdown";

  private static final String errLogFileCreation =
          "Could not create log file: ";
  private static final String errUsage = FileAIO.class.getName() +
          " Usage: <filename> <filename>";
  private static final String errFileSize = "could not read file size";
  private static final String errThreadInterrupt =
          "thread has been interrupted";
  private static final String errLeft = "Error reading left: ";
  private static final String errRight = "Error reading right: ";
  private static final String errByteOffset = "bad byte offset";
  private static final String errDuplicate_int = "Duplicate value found";
  private static final String errClose = "Could not close file";

  private static final int MAXTHREADS = 10;

  private Logger logger;
  private AsynchronousFileChannel left, right;
  private ConcurrentSkipListMap<Long, Long> matches =
          new ConcurrentSkipListMap<>();
  private List<Long> potentials =
          Collections.synchronizedList(new ArrayList<>());
  private ExecutorService pool = Executors.newFixedThreadPool(MAXTHREADS);

  /**
   * Creates a new FileAIO object
   *
   * @param args arguments passed to program
   */
  private FileAIO(String[] args) {
    if(args.length < 2 || args.length > 2) {
      throw new IllegalArgumentException(errUsage);
    }

    setup_logger();
    setFiles(args[0], args[1]);
  }

  public static void main(String[] args) {
    FileAIO t = new FileAIO(args);

    t.findMatching();

    t.shutDown();

    t.printMatching();
  }

  /**
   * setup for the logging service
   *
   * Creates a filehandler that will write all logs to LOGFILE
   * Creates a consolehandler that will only output server logs
   */
  private void setup_logger() {
    LogManager.getLogManager().reset();
    logger = Logger.getLogger(FileAIO.class.getName());

    try {
      Handler fileHand = new FileHandler(LOGFILE);
      fileHand.setLevel(Level.ALL);
      logger.addHandler(fileHand);
    } catch(IOException ioe) {
      System.err.println(errLogFileCreation + LOGFILE);
      System.exit(1);
    }

    Handler consoleHand = new ConsoleHandler();
    consoleHand.setLevel(Level.SEVERE);
    logger.addHandler(consoleHand);
  }

  /**
   * Calls Donahoo's AsynchronousFileChannelFactory to create file channels
   * for left and right files
   *
   * @param leftFile  left file
   * @param rightFile right file
   */
  private void setFiles(String leftFile, String rightFile) {
    try {
      left = AsynchronousFileChannelFactory.open(Paths.get(leftFile), pool);
      right = AsynchronousFileChannelFactory.open(Paths.get(rightFile), pool);
    } catch(IOException ioe) {
      error_severe(ioe.getMessage(), ioe);
    }
  }

  /**
   * submit reads on the left file then submits reads on the right file.
   * When complete, terminates the pool and closes the file channels
   */
  private void findMatching() {
    long fileSize = getFileSize(left);

    for(long i = 0; i < (fileSize / 8); i++) {
      ByteBuffer buff = ByteBuffer.allocate(8);
      left.read(buff, i * 8, buff, new LeftRead());
    }

    fileSize = getFileSize(right);

    for(long i = 0; i < (fileSize / 8); i++) {
      ByteBuffer buff = ByteBuffer.allocate(8);
      right.read(buff, i * 8, buff, new RightRead());
    }
  }

  /**
   * Gets the file size in bytes
   *
   * @param file file to get size of
   * @return the file size
   */
  private long getFileSize(AsynchronousFileChannel file) {
    long fileSize = -1;
    try {
      fileSize = file.size();
    } catch(IOException ioe) {
      error_severe(errFileSize, ioe);
    }
    return fileSize;
  }

  /**
   * prints out the contents of the matches map
   */
  private void printMatching() {
    //add back in any right values if the left values weren't adding in time
    for(Long l : potentials) {
      if(matches.containsKey(l)) {
        matches.put(l, matches.get(l)+1);
      }
    }

    for(Map.Entry<Long, Long> e : matches.entrySet()) {
      System.out.println(e);
    }
  }

  /**
   * Terminates the ExecutorService for the asynchronous reads
   * and closes both left and right file channels
   */
  private void shutDown() {
    try {
      pool.awaitTermination(1, TimeUnit.SECONDS);
      pool.shutdown(); // Disable new tasks from being submitted

      // Wait a while for existing tasks to terminate
      if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
        pool.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!pool.awaitTermination(60, TimeUnit.SECONDS))
          System.err.println("Pool did not terminate");
      }
      logger.log(Level.INFO, msgPoolShutDown);

      try {
        left.close();
      } catch(IOException ioe) {
        logger.log(Level.SEVERE, errLeft + errClose, ioe);
        System.exit(1);
      }

      try {
        right.close();
      } catch(IOException ioe) {
        logger.log(Level.SEVERE, errRight + errClose, ioe);
        System.exit(1);
      }
    } catch (InterruptedException ie) {
      // (Re-)Cancel if current thread also interrupted
      pool.shutdownNow();
      // Preserve interrupt status
      logger.log(Level.SEVERE, errThreadInterrupt, ie.fillInStackTrace());
      System.exit(1);
    }
  }

  /**
   * An error handler function just cause I called this set of code alot
   * @param msg message to log
   * @param cause the cause of the error if any
   */
  private void error_severe(String msg, Throwable cause) {
    logger.log(Level.SEVERE, msg, cause);
    shutDown();
    System.exit(1);
  }

  /**
   * Converts a byte array to an integer
   *
   * @param b byte array to convert
   * @return convert integer as a long
   */
  private long b2i(byte[] b) {
    long integer = 0;

    for(int i = 7; i >= 0; i--) {
      integer |= ((b[i] & 0xff) << (8 * i));
    }
    return integer;
  }

  /**
   * Completion handler for reads on the left file
   *
   * Stores integers read in into a map
   *
   * If a duplicate number is inserted program will terminate
   */
  private class LeftRead implements CompletionHandler<Integer, ByteBuffer> {
    @Override
    public void completed(Integer result, ByteBuffer attachment) {
      if(result >= 0) {
        Long leftVar = b2i(attachment.array());
        logger.log(Level.INFO, "Reading: " + result + " bytes:" + leftVar);

        //null check even though it should already be initialized
        //add value to a mapping. If the value already exist thats bad
        System.out.println(leftVar);
        if(matches.put(leftVar, 0L) != null) {
          error_severe(errDuplicate_int, null);
        }
      } else {
        logger.log(Level.WARNING, errLeft + errByteOffset);
      }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
      error_severe(errLeft + attachment.toString(), exc);
    }
  }

  /**
   * Completion handler for reads on the right file
   *
   * Increments the key value of the number read in
   */
  private class RightRead implements CompletionHandler<Integer, ByteBuffer> {

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
      if(result >= 0) {
        long rightVar = b2i(attachment.array());
        logger.log(Level.INFO, "Reading: " + result + " bytes:" + rightVar);

        //only increment value if key exists
        if(matches.containsKey(rightVar)) {
          matches.put(rightVar, matches.get(rightVar) + 1);
        } else {
          potentials.add(rightVar);
        }
      } else {
        logger.log(Level.WARNING, errRight + errByteOffset);
      }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
      error_severe(errRight + attachment.toString(), exc);
    }
  }
}
