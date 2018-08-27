package RightTriangle;

import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SpeedTest {

    private static final String errParams = "usage: <number of test> <fileName>";
    private static final String errTriangleRun = "something happened when running the program: ";
//    private static final String errFile = "could not locate file: ";

    private static int loopCount;
    private static String fileName;
    private static List<Long> times = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        handleCmdLine(args);
        fillAFile(fileName);

        handleOutput(runMain(triangles.class, new String[]{args[1]}));

        handleOutput(runMain(threadedTriangles.class, new String[]{args[1]}));
    }

    private static void handleOutput(String[] progOut) {
        System.out.println(triangles.class.getSimpleName() + " total runtime: " + progOut[0] + "s");
        System.out.println(triangles.class.getSimpleName() + " average runtime: " + progOut[1] + "s");
    }

    private static void handleCmdLine(String[] args) {
        if(args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException(errParams);
        }

        loopCount = Integer.parseInt(args[0]);
        fileName = args[1];
    }

    private static String[] runMain(Class<?> type, String[] args) {
        for(int i = 0; i < loopCount; i++) {
            try {
                String topOutput = "------------" + type.getSimpleName() + " output------------";
                System.out.println(topOutput);
                long start = System.nanoTime();
                type.getDeclaredMethod("main", String[].class).invoke(null,(Object)args);
                long stop = System.nanoTime();

                for(int j = 0; j < topOutput.length(); j++) {
                    System.out.print("-");
                }
                System.out.println();

                System.out.println("Program runtime " + i + ": " + (stop-start) + "ns");
                times.add((stop-start));
            } catch(Exception e) {
                System.err.println(errTriangleRun + e.getCause());
                return new String[]{};
            }
        }
        return calculateAvg(times);
    }

    private static String[] calculateAvg(List<Long> times) {
        long sum = 0;
        //times.forEach((k) -> sum[0] =+ k);
        for(Long time : times) {
            sum += time;
        }
        sum = TimeUnit.NANOSECONDS.toSeconds(sum);

        return new String[]{String.valueOf(sum), String.valueOf((double)sum/times.size())};
    }

    private static void fillAFile(String fileName) throws IOException {
        int pointCount = 100;
        FileWriter fileWriter = new FileWriter(new File(fileName));
        Random rnd = new Random(System.currentTimeMillis());
        Set<Pair<Integer, Integer>> pointSet = new HashSet<>();

        fileWriter.write(pointCount + "\n");
        while(pointSet.size() != pointCount) {
            pointSet.add(new Pair<>(rnd.nextInt(10), rnd.nextInt(10)));
        }

        for(Pair p : pointSet) {
            fileWriter.write(p.getKey() + " " + p.getValue() + "\n");
        }
        fileWriter.flush();
    }
}
