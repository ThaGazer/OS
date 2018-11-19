/*
 * Author: Justin Ritter
 * Date:
 * File: Triangle.java
 * Description: method to assist in triangle checking
 */
package triangle;

public class Triangle {

  public static boolean isRight(Point p1, Point p2, Point p3) {
    return rightCheck(p1, p2, p3);
  }

  public static String toString(Point p1, Point p2, Point p3) {
    return p1.toString() + p2.toString() + p3.toString();
  }

  private static boolean rightCheck(Point p1, Point p2, Point p3) {
    double a, b, c;
    a = p1.distance(p2);
    b = p1.distance(p3);
    c = p2.distance(p3);

    return a+b == c || a+c == b || b+c == c;
  }
}
