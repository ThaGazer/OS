/*
 * AIO:AsynchIO
 *
 * Date Created: Apr/24/2018
 * Author:
 *   -Justin Ritter
 */
package AIO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.logging.*;

public class AsynchIO {

    private static final String errUsage = "Usage: <filename>";
    private static final String errFNF = "could not find file path";
    private static final String errReadFailed = "failed to read from channel";
    private static final String errParsing = "could not parse input file";

    private static final String regx_Num1 = "[\\d]+";
    private static final String regx_Path = "[a-zA-Z0-9_\\\\/.]+";

    private static final String LOGNAME = AsynchIO.class.getName();
    private static final String LOGFILENAME = "aio.log";
    private static Logger logger;

    public static void main(String args[]) throws IOException {
        if(args.length != 1) {
            throw new IllegalArgumentException(errUsage);
        }

        setup_logger();

        ArrayList<String> commands = new ArrayList<>(readAllLines(args[0]));
        for(String line : commands) {
             if(!commandChecker(line.split(" "))) {
                 logger.severe(errParsing);
                 System.exit(1);
             }
        }

        for(String line : commands) {
            StringBuilder out = new StringBuilder();
            String[] lineSplit = line.split(" ");

            out.append(lineSplit[0]).append(" ");
            try(AsynchronousFileChannel fileChannel =
                        AsynchronousFileChannel.open(Paths.get(lineSplit[1]),
                                StandardOpenOption.READ)) {
                ByteBuffer contents =
                        ByteBuffer.allocate(Integer.parseInt(lineSplit[3]));
                fileChannel.read(contents, Integer.parseInt(lineSplit[2]),
                        contents, new CompletionHandler<Integer, ByteBuffer>() {
                            @Override
                            public void completed
                                    (Integer result, ByteBuffer buff) {
                                out.append(new String(buff.array()));
                                System.out.println(out);
                                logger.info("Printing: " + out);
                            }

                            @Override
                            public void failed(Throwable exc, ByteBuffer buff) {
                                logger.warning(errReadFailed);
                            }
                        });
            } catch (IOException e) {
                logger.log(Level.SEVERE, errReadFailed, e);
                System.exit(2);
            }
        }
    }

    private static ArrayList<String> readAllLines(String fileName) {
        ArrayList<String> lines = new ArrayList<>();
        try {
            lines = new ArrayList<>
                    (Files.readAllLines
                            (Paths.get(fileName), StandardCharsets.US_ASCII));
        } catch (IOException e) {
            logger.log(Level.SEVERE, errFNF, e);
            System.exit(2);
        }
        return lines;
    }

    private static boolean commandChecker(String[] arr) {
        if(arr.length != 4) {
            return false;
        }
        if(!arr[0].matches(regx_Num1)) {
            return false;
        }
        if(!arr[1].matches(regx_Path)) {
            return false;
        }
        if(!arr[2].matches(regx_Num1)) {
            return false;
        }
        if(!arr[3].matches(regx_Num1)) {
            return false;
        }
        return true;
    }

    private static void setup_logger() throws IOException {
        logger = Logger.getLogger(LOGNAME);

        Handler fileHand = new FileHandler(LOGFILENAME);
        Handler consoleHand = new ConsoleHandler();

        fileHand.setLevel(Level.ALL);
        consoleHand.setLevel(Level.SEVERE);

        logger.addHandler(fileHand);
        logger.addHandler(consoleHand);
    }
}
