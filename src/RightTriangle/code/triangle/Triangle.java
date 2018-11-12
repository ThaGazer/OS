package triangle;

public class Triangle {

  private static final String errBigInt = "oops to big: ";

  public static boolean isRight(Point p1, Point p2, Point p3) {
    return rightCheck(p1, p2, p3);
  }

  public static boolean isTriangle(Point p1, Point p2, Point p3) {
    if(p1.equals(p2) || p1.equals(p3) || p2.equals(p3)) {
      return false;
    }
    return !isZeroSlope(p1,p2,p3);
  }

  private static boolean rightCheck(Point p1, Point p2, Point p3) {
    if (!isTriangle(p1, p2, p3)) {
      return false;
    }

    int a, b, c, temp;
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

    return (a*a + b*b) == c*c;
  }

  private static boolean isZeroSlope(Point p1, Point p2, Point p3) {
    return p1.getX() == p2.getX() && p1.getX() == p3.getX() || p1.getY() == p2.getY() && p1.getY() == p3.getY();
  }
}
