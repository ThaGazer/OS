package triangle;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class TrianglesBasic {
  private static final String errParams = "usage: <filename>";
  private static final String errFNF = "file not found ";
  private static final String errPoints = "number of timed_points.txt in file do not match";

  private static List<Integer> pointsX;
  private static List<Integer> pointsY;
  private static Set<Triangle> dups = new HashSet<>();
  private static Integer totalPnts;

  public static void main(String[] args) throws Exception {
    pointsX = new ArrayList<>();
    pointsY = new ArrayList<>();
    TrianglesBasic t = new TrianglesBasic();

    if(args.length < 1) {
      throw new IllegalArgumentException(errParams);
    }

    t.readPoints(args[0]);
    System.out.println(t.findTriangles());
  }

  protected void readPoints(String fileName) throws Exception {
    try(Scanner scn = new Scanner(new File(fileName))) {
      totalPnts = scn.nextInt();

      for(int i = 0; i < (totalPnts * 2); i++) {
        if((i % 2) == 0) {
          pointsX.add(Integer.parseInt(scn.next()));
        } else {
          pointsY.add(Integer.parseInt(scn.next()));
        }
      }
    } catch(FileNotFoundException fnfe) {
      System.err.println(errFNF);
    }

    if(pointsX.size() != pointsY.size() && pointsX.size() != totalPnts) {
      throw new Exception(errPoints);
    }
  }

  protected int findTriangles() {
    List<Integer> foundX = new ArrayList<>();
    List<Integer> foundY = new ArrayList<>();

    int numOfRightTri = 0;
    int numOfTri = 0;

    for(int i = 0; i < totalPnts; i++) {
      for(int j = 0; j < totalPnts; j++) {
        for(int k = 0; k < totalPnts; k++) {
          int x1 = pointsX.get(i), x2 = pointsX.get(j), x3 = pointsX.get(k),
              y1 = pointsY.get(i), y2 = pointsY.get(j), y3 = pointsY.get(k);
          numOfTri++;

          System.out.println("(" + x1 + " ," + y1 + ")(" + x2 + " ," + y2 + ")(" + x3 + " ," + y3 + ")");

          if(!(x1 == x2 && x1 == x3 || y1 == y2 && y1 == y3)) { //slope check
            if(!(x1 == x2 && y1 == y2 || x1 == x3 && y1 == y3 || x2 == x3 && y2 == y3)) { //equal points check
              if(!containsTriangle(foundX, foundY, x1, y1, x2, y2, x3, y3)) {
                int a, b, c, temp;
                c = distanceFormula(x1, y1, x2, y2);

                temp = distanceFormula(x1, y1, x3, y3);
                if(c < temp) {
                  b = c;
                  c = temp;
                } else {
                  b = temp;
                }

                temp = distanceFormula(x2, y2, x3, y3);
                if(c < temp) {
                  a = c;
                  c = temp;
                } else {
                  a = temp;
                }

                if(a + b == c) {
                  numOfRightTri++;

                  System.out.println("^right");
                  foundX.add(x1);
                  foundX.add(x2);
                  foundX.add(x3);
                  foundY.add(y1);
                  foundY.add(y2);
                  foundY.add(y3);
                }
              }
            }
          }
        }
      }
    }
    System.out.println(numOfTri);
    return numOfRightTri;
  }

  /**
   * returns the distance between two timed_points.txt squared
   *
   * @param x1 x coordinate of p1
   * @param y1 y coordinate of p1
   * @param x2 x coordinate of p2
   * @param y2 y coordinate of p2
   * @return distance between the two coordinates
   */
  private static int distanceFormula(int x1, int y1, int x2, int y2) {
    return ((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2));
  }

  private static boolean containsTriangle(List<Integer> x, List<Integer> y,
                                          Integer x1, Integer y1,
                                          Integer x2, Integer y2,
                                          Integer x3, Integer y3) {
    for(int i = 0; i < x.size(); i += 3) {
      List<Integer> triX = new ArrayList<>(3);
      List<Integer> triY = new ArrayList<>(3);

      triX.add(x.get(i));
      triX.add(x.get(i + 1));
      triX.add(x.get(i + 2));
      triY.add(y.get(i));
      triY.add(y.get(i + 1));
      triY.add(y.get(i + 2));

      if(triX.contains(x1) && triY.contains(y1)) {
        if(triX.contains(x2) && triY.contains(y2)) {
          if(triX.contains(x3) && triY.contains(y3)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
