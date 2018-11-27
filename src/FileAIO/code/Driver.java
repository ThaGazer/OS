import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Driver {

    private long loopCount;

    private Driver(String[] args) {
        handleArgs(args);
    }

    public static void main(String[] args) {
        Driver driver = new Driver(args);

        driver.runMain(FileAIO.class, new String[]{args[1], args[2]});
    }

    private String[] runMain(Class<?> type, String[] args) {
        List<Long> times = new ArrayList<>();
        for(int i = 0; i < loopCount; i++) {
            try {
        String topOutput = "------------" + type.getSimpleName() + " output------------";
        System.out.println(topOutput);

                long start = System.nanoTime();
                type.getDeclaredMethod("main", String[].class).invoke(null, (Object)args);
                long stop = System.nanoTime();
        for(int j = 0; j < topOutput.length(); j++) {
          System.out.print("-");
        }
        System.out.println();

        System.out.println("Program runtime " + i + ": " + (stop - start) + "ns");
                times.add((stop - start));
            } catch(Exception e) {
                System.err.println(e.getMessage());
                System.exit(1);
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

        return new String[]{String.valueOf(sum), String.valueOf((double)sum / times.size())};
    }

    private void handleArgs(String[] args) {
        if(args.length != 3) {
            throw new IllegalArgumentException("Usage: <loop count> <filename> <filename>");
        }

        loopCount = Long.parseLong(args[0]);
    }
}
