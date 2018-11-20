package triangle;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class TrianglesBasic {
  private static final String errParams = "usage: <filename>";
  private static final String errFNF = "file not found ";
  private static final String errPoints = "number of timed_points.txt in file do not match";

  private static List<Point> points;
  private static Set<Triangle> dups = new HashSet<>();
  private static Integer totalPnts;

  public static void main(String[] args) throws Exception {
    points = new ArrayList<>();
    TrianglesBasic t = new TrianglesBasic();

    if(args.length < 1) {
      throw new IllegalArgumentException(errParams);
    }

    t.readPoints(args[0]);
    System.out.println("Right triangles: " + t.findTriangles());
  }

  protected void readPoints(String fileName) throws Exception {
    try(Scanner scn = new Scanner(new File(fileName))) {
      totalPnts = scn.nextInt();

      for(int i = 0; i < (totalPnts * 2); i++) {
        if((i % 2) == 0) {
          points.add(new Point(Integer.parseInt(scn.next()), Integer.parseInt(scn.next())));
        }
      }
    } catch(FileNotFoundException fnfe) {
      System.err.println(errFNF);
    }
  }

  protected int findTriangles() {
    List<Triangle> foundTri = new ArrayList<>();

    int numOfRightTri = 0;
    int numOfTri = 0;

    points.sort(Point::compareTo);
    System.out.println(points);

    for(int i = 0; i < totalPnts-2; i++) {
      for(int j = i+1; j < totalPnts-1; j++) {
        for(int k = j+1; k < totalPnts; k++) {
          numOfTri++;

          String out = (i + " " + j + " " + k);

          if(Triangle.isRight(points.get(i), points.get(j), points.get(k))) {
            out += "<-right";
            //foundTri.add(new Triangle(points.get(i), points.get(j), points.get(k)));

            numOfRightTri++;
          }
          System.out.println(out);
        }
      }
    }
    System.out.println(numOfTri);
    return numOfRightTri;
  }
}
