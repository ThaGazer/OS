package RightTriangle;

import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SpeedTest {

    private static final String errParams = "usage: <number of test> <fileName> <nProcs>";
    private static final String errTriangleRun = "something happened when running the program: ";
    private static final String errFile = "could not locate file: ";

    private static int loopCount;
    private static String fileName;

    public static void main(String[] args) throws IOException {
        handleCmdLine(args);
        fillAFile(fileName);

        StringBuilder out = new StringBuilder();
//        out.append(handleOutput(TrianglesBasic.class.getSimpleName(), runMain(TrianglesBasic.class, new String[]{args[1]})));
//        out.append(handleOutput(TrianglesClass.class.getSimpleName(), runMain(TrianglesClass.class, new String[]{args[1]})));
        out.append(handleOutput(TrianglesThreaded.class.getSimpleName(), runMain(TrianglesThreaded.class, new String[]{args[1], args[2]})));

        System.out.println("\n" + out);
    }

    private static String handleOutput(String name, String[] progOut) {
        return name + " total runtime: " + progOut[0] + "ms\n" + name + " average runtime: " + progOut[1] + "ms\n\n";
    }

    private static void handleCmdLine(String[] args) {
        if(args.length < 2 || args.length > 3) {
            throw new IllegalArgumentException(errParams);
        }

        loopCount = Integer.parseInt(args[0]);
        fileName = args[1];
    }

    private static String[] runMain(Class<?> type, String[] args) {
        List<Long> times = new ArrayList<>();
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
        sum = TimeUnit.NANOSECONDS.toMillis(sum);

        return new String[]{String.valueOf(sum), String.valueOf((double)sum/times.size())};
    }

    private static void fillAFile(String fileName) throws IOException {
        int pointCount = 5000;
        FileWriter fileWriter = new FileWriter(new File(fileName));
        Random rnd = new Random(System.nanoTime());
        Set<Pair<Integer, Integer>> pointSet = new HashSet<>();

        fileWriter.write(pointCount + "\n");
        while(pointSet.size() != pointCount) {
            pointSet.add(new Pair<>(rnd.nextInt(1000), rnd.nextInt(1000)));
        }

        for(Pair p : pointSet) {
            fileWriter.write(p.getKey() + " " + p.getValue() + "\n");
        }
        fileWriter.flush();
    }
}
