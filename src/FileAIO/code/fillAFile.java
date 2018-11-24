import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class fillAFile {
  public static void main(String[] args) {
    if(args.length < 2) {
      throw new IllegalArgumentException("Usage: <amount> <filename>...");
    }

    Random rnd = new Random(System.nanoTime());
    int amount = Integer.parseInt(args[0]);

    for(int i = 1; i < args.length; i++) {
      File f = new File(args[i]);

      if(f.isFile()) {
        try {
          DataOutputStream out = new DataOutputStream(new FileOutputStream(f));

          System.out.println("Writing to " + f.getName() + ":");
          for(int j = 0; j < amount; j++) {
            byte[] buff = new byte[8];

            long longIn = rnd.nextInt(10);
            longIn &= 0x7fffffffffffffffL;

            for(int k = 0; k < 8; k++) {
              buff[k] = (byte)((longIn >> (8*k)));
            }
            System.out.println(longIn + ":" + Arrays.toString(buff));
            out.write(buff);

          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        System.err.println("could not find file: " + f.getName());
      }
    }
  }
}
