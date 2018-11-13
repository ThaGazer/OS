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
    double a, b, c, temp;
    c = p1.distance(p2);

    a = p1.distance(p3);
    if(a > c) {
      temp = c;
      c = a;
      a = temp;
    }

    b = p2.distance(p3);
    if(b > c) {
      temp = c;
      c = b;
      b = temp;
    }
    return a+b == c;
  }
}
