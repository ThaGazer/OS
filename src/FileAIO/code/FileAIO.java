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
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

public class FileAIO {

  private static final String Logfile = "aio.log";

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

  private static final int MAXTHREADS = 10;

  private Logger logger;
  private AsynchronousFileChannel left, right;
  private TreeMap<Long, Long> matches = new TreeMap<>();
  private ExecutorService pool = Executors.newFixedThreadPool(MAXTHREADS);

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

  private void setup_logger() {
    LogManager.getLogManager().reset();
    logger = Logger.getLogger(FileAIO.class.getName());

    try {
      Handler fileHand = new FileHandler(Logfile);
      fileHand.setLevel(Level.ALL);
      logger.addHandler(fileHand);
    } catch(IOException ioe) {
      System.err.println(errLogFileCreation + Logfile);
    }

    Handler consoleHand = new ConsoleHandler();
    consoleHand.setLevel(Level.SEVERE);
    logger.addHandler(consoleHand);
  }

  private void setFiles(String leftFile, String rightFile) {
    try {
      left = AsynchronousFileChannelFactory.open(Path.of(leftFile), pool);
      right = AsynchronousFileChannelFactory.open(Path.of(rightFile), pool);
    } catch(IOException ioe) {
      logger.log(Level.SEVERE, ioe.getMessage(), ioe.fillInStackTrace());
      System.exit(1);
    }
  }

  private void findMatching() {
    long fileSize = 0;
    try {
      fileSize = left.size();
    } catch(IOException ioe) {
      logger.log(Level.SEVERE, errSize, ioe);
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
      System.exit(1);
    }

    for(long i = 0; i < (fileSize/8); i++) {
      ByteBuffer buff = ByteBuffer.allocate(8);
      right.read(buff, i*8, buff, new RightRead());
    }
  }

  private void printMatching() {
    for(Map.Entry<Long, Long> e : matches.entrySet()) {
      System.out.println(e);
    }
  }

  private void shutDown() {
    try {
      pool.shutdown();
      pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
      logger.info(msgPoolShutDown);
    } catch(InterruptedException ioe) {
      logger.log(Level.SEVERE, errThreadInterrupt, ioe);
    }
  }

  private long b2i(byte[] b) {
    long integer = 0;

    for(int i = 7; i >= 0; i--) {
      integer |= ((b[i] & 0xff) << (8 * i));
    }
    return integer;
  }

  private class LeftRead implements CompletionHandler<Integer, ByteBuffer> {
    @Override
    public void completed(Integer result, ByteBuffer attachment) {
      long leftVar = b2i(attachment.array());
      logger.log(Level.INFO, "Reading: " + result + " bytes:" + leftVar);

      if(matches.put(leftVar, 0L) != null) {
        logger.severe(errDuplicate_int);
        System.exit(1);
      }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
      logger.log(Level.WARNING, errLeft + errByteOffset, exc);
    }
  }

  private class RightRead implements CompletionHandler<Integer, ByteBuffer> {

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
      long rightVar = b2i(attachment.array());
      logger.log(Level.INFO, "Reading: " + result + " bytes:" + rightVar);

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
