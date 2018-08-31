package RightTriangle;

public class Point {
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

    public boolean isZeroSlope(Point p2, Point p3) {
        return getX() == p2.getX() && getX() == p3.getX() || getY() == p2.getY() && getY() == p3.getY();
    }

    public int distance(Point p1) {
        return ((getX() - p1.getX())*(getX() - p1.getX())) + ((getY() - p1.getY())*(getY() - p1.getY()));
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getX()) + Integer.hashCode(getY());
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

