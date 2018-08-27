package RightTriangle;

public class Point {
    private int x;
    private int y;

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean zeroSlope(Point p2, Point p3) {
        return getX() != p2.getX() || getX() != p3.getX() && getY() != p2.getY() || getY() != p3.getY();
    }

    public int distance(Point p1) {
        return ((getX() - p1.getX())*(getX() - p1.getX())) + ((getY() - p1.getY())*(getY() - p1.getY()));
    }

    public boolean rightCheck(Point p2, Point p3) {
        if(zeroSlope(p2, p3)) {
            int a,b,c, temp;
            c = distance(p2);

            a = distance(p3);
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
        } else {
            return false;
        }
    }

    public String toString() {
        return "(" + getX() + "," + getY() + ")";
    }
}

