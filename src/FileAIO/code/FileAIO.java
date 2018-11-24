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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileAIO {

  private static final String Logfile = "aio.log";

  private static final String errUsage = FileAIO.class.getName() + " Usage: <filename> <filename>";
  private static final String errSize = "could not read file size";
  private static final String errByteOffset = "bad byte offset";

  private static final int MAXTHREADS = 10;

  private Logger logger = Logger.getLogger(Logfile);
  private AsynchronousFileChannel left, right;
  private Map<Integer, Integer> matches = new HashMap<>();
  private ExecutorService pool = Executors.newFixedThreadPool(MAXTHREADS);

  private FileAIO(String[] args) {
    if(args.length < 2 || args.length > 2) {
      throw new IllegalArgumentException(errUsage);
    }

    setFiles(args[0], args[1]);
  }

  public static void main(String[] args) {
    FileAIO t = new FileAIO(args);

    t.findMatching();
    t.printMatching();

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

    for(int i = 0; i < (fileSize/8); i++) {
      ByteBuffer buff = ByteBuffer.allocate(8);

      left.read(buff, i*8, buff, new LeftRead());
    }

    //pool.shutdown();
  }

  private void printMatching() {
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
      logger.log(Level.INFO, "Reading: " + result + " bytes");
      long leftVar = b2i(attachment.array());
      System.out.println(leftVar);
      /*try {
        for(int i = 0; i < (right.size() / 8); i++) {
          right.read(attachment, 0, leftVar, new RightRead());
        }
      } catch(IOException ioe) {
        logger.log(Level.SEVERE, errSize, ioe);
        System.exit(1);
      }*/
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
      logger.log(Level.WARNING, "failed left read on: " +
          attachment.toString(), exc);
    }
  }

  private class RightRead implements CompletionHandler<Integer, Long> {

    @Override
    public void completed(Integer result, Long attachment) {
      //System.out.println("Right");
    }

    @Override
    public void failed(Throwable exc, Long attachment) {
      logger.log(Level.WARNING, "failed right read on: " + attachment, exc);
    }
  }
}
