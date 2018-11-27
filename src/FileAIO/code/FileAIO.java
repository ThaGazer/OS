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
import java.util.Map;
import java.util.TreeMap;
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
  private static final String errSize = "could not read file size";
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
  private TreeMap<Long, Long> matches = new TreeMap<>();
  private ExecutorService pool = Executors.newFixedThreadPool(MAXTHREADS);

  /**
   * Creates a new FileAIO object
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
   * @param leftFile left file
   * @param rightFile right file
   */
  private void setFiles(String leftFile, String rightFile) {
    try {
      left = AsynchronousFileChannelFactory.open(Paths.get(leftFile), pool);
      right = AsynchronousFileChannelFactory.open(Paths.get(rightFile), pool);
    } catch(IOException ioe) {
      logger.log(Level.SEVERE, ioe.getMessage(), ioe.fillInStackTrace());
      shutDown();
      System.exit(1);
    }
  }

  /**
   * submit reads on the left file then submits reads on the right file.
   * When complete, terminates the pool and closes the file channels
   */
  private void findMatching() {
    long fileSize = 0;
    try {
      fileSize = left.size();
    } catch(IOException ioe) {
      logger.log(Level.SEVERE, errSize, ioe);
      shutDown();
      System.exit(1);
    }

    for(long i = 0; i < (fileSize/8); i++) {
      ByteBuffer buff = ByteBuffer.allocate(8);
      left.read(buff, i*8, buff, new LeftRead());
    }

    try {
      fileSize = right.size();
    } catch(IOException ioe) {
      logger.log(Level.SEVERE, errSize, ioe);
      shutDown();
      System.exit(1);
    }

    for(long i = 0; i < (fileSize/8); i++) {
      ByteBuffer buff = ByteBuffer.allocate(8);
      right.read(buff, i*8, buff, new RightRead());
    }
  }

  /**
   * prints out the contents of the map
   */
  private void printMatching() {
    for(Map.Entry<Long, Long> e : matches.entrySet()) {
      System.out.println(e);
    }
  }

  /**
   * Terminates the ExecutorService for the asynchronous reads and closes both left and right file channels
   */
  private void shutDown() {
    try {
      pool.shutdown();
      pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
      logger.info(msgPoolShutDown);

      left.close();
      right.close();
    } catch(InterruptedException ioe) {
      logger.log(Level.SEVERE, errThreadInterrupt, ioe);
    } catch (IOException e) {
      logger.log(Level.SEVERE, errClose);
    }

    //System.exit(1);
  }

  /**
   * Converts a byte array to an integer
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
   * if a duplicate number is inserted program will terminate
   */
  private class LeftRead implements CompletionHandler<Integer, ByteBuffer> {
    @Override
    public void completed(Integer result, ByteBuffer attachment) {
      Long leftVar = b2i(attachment.array());
      logger.log(Level.INFO, "Reading: " + result + " bytes:" + leftVar);

      //null check even though it should already be initialized
      if(matches != null) {
        //add value to a mapping. If the value already exist thats bad
        if (matches.put(leftVar, 0L) != null) {
          logger.severe(errDuplicate_int);
          shutDown();
          System.exit(1);
        }
      } else {
        logger.log(Level.SEVERE, "for some reason the map is empty");
      }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
      logger.log(Level.WARNING, errLeft + errByteOffset, exc);
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
      long rightVar = b2i(attachment.array());
      logger.log(Level.INFO, "Reading: " + result + " bytes:" + rightVar);

      //only increment value if key exists
      if(matches.containsKey(rightVar)) {
        matches.put(rightVar, matches.get(rightVar)+1);
      }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
      logger.log(Level.WARNING, errRight + errByteOffset + attachment, exc);
    }
  }
}
