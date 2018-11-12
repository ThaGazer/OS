package triangle;

public class Point implements Comparable<Point> {
  private int x;
  private int y;

  public Point() {
    x = 0;
    y = 0;
  }

  public Point(int x, int y) {
    setX(x);
    setY(y);
  }

  public Point(Point p) {
    this(p.getX(), p.getY());
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int distance(Point p1) {
    return Math.abs(((getX() - p1.getX()) * (getX() - p1.getX())) + ((getY() - p1.getY()) * (getY() - p1.getY())));
  }

  @Override
  public int compareTo(Point obj) {
    if(getX() == obj.getX()) {
      return getY() - obj.getY();
    }
    return getX() - obj.getX();
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int res = 1;
    res += prime * res + Integer.hashCode(getX());
    res += prime * res + Integer.hashCode(getY());
    return res;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }

    if(obj == null) {
      return false;
    }

    if(getClass() != obj.getClass()) {
      return false;
    }

    Point p = (Point)obj;
    return getX() == p.getX() && getY() == p.getY();
  }

  @Override
  public String toString() {
    return "(" + getX() + "," + getY() + ")";
  }

}

