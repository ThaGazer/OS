package RightTriangle;

import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

        for(int i = 0; i < loopCount; i++) {
            try {
                System.out.println("------------Program output------------");
                long start = System.nanoTime();
                triangles.main(new String[]{fileName});
                long stop = System.nanoTime();
                System.out.println("--------------------------------------");

                System.out.println("Program runtime " + i + ": " + (stop-start) + "ns\n\n");
                times.add((stop-start));
            } catch(Exception e) {
                System.err.println(errTriangleRun + e.getMessage());
            }
        }
        System.out.println("Program average runtime: " + calculateAvg(times) + "s");
    }

    private static void handleCmdLine(String[] args) {
        if(args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException(errParams);
        }

        loopCount = Integer.parseInt(args[0]);
        fileName = args[1];
    }

    private static double calculateAvg(List<Long> times) {
        long sum = 0;
        //times.forEach((k) -> sum[0] =+ k);
        for(Long time : times) {
            sum += time;
        }
        sum = TimeUnit.NANOSECONDS.toSeconds(sum);
        System.out.println("Program total runtime: " + sum + "s");
        return (double)sum/times.size();
    }

    private static void fillAFile(String fileName) throws IOException {
        int pointCount = 1000;
        FileWriter fileWriter = new FileWriter(new File(fileName));
        Random rnd = new Random(System.currentTimeMillis());
        Set<Pair<Integer, Integer>> pointSet = new HashSet<>();

        fileWriter.write(pointCount + "\n");
        while(pointSet.size() != pointCount) {
            pointSet.add(new Pair<>(rnd.nextInt(100), rnd.nextInt(100)));
        }

        for(Pair p : pointSet) {
            fileWriter.write(p.getKey() + " " + p.getValue() + "\n");
        }
        fileWriter.flush();
    }
}
