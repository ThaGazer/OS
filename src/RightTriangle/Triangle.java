package RightTriangle;

public class Triangle {
    private Point a,b,c;
    private boolean right;

    public Triangle() {
        setA(new Point());
        setB(new Point());
        setC(new Point());
        right = false;
    }

    public Triangle(Point a, Point b, Point c) {
        setA(a);
        setB(b);
        setC(c);
        right = rightCheck();
    }

    public Triangle(Triangle t) {
        this(t.getA(), t.getB(), t.getC());
    }

    public void setA(Point p) {
        a = new Point(p);
    }

    public void setB(Point p) {
        b = new Point(p);
    }

    public void setC(Point p) {
        c = new Point(p);
    }

    public Point getA() {
        return new Point(a);
    }

    public Point getB() {
        return new Point(b);
    }

    public Point getC() {
        return new Point(c);
    }

    public boolean isRight() {
        return right;
    }

    public boolean isTriangle() {
        if(getA().equals(getB()) || getA().equals(getC()) || getB().equals(getC())) {
            return false;
        }
        if(getA().isZeroSlope(getB(), getC())) {
            return false;
        }
        return true;
    }

    private boolean rightCheck() {
        if(isTriangle()) {
            int a,b,c, temp;
            c = getA().distance(getB());

            a = getA().distance(getC());
            if(a > c) {
                temp = c;
                c = a;
                a = temp;
            }

            b = getB().distance(getC());
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

    private boolean has(Point p) {
        if(getA().equals(p)) {
            return true;
        } else if(getB().equals(p)) {
            return true;
        } else {
            return getC().equals(p);
        }
    }

    @Override
    public int hashCode() {
        return a.hashCode() + b.hashCode() + c.hashCode();
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

        Triangle t = (Triangle)obj;
        return has(t.getA()) && has(t.getB()) && has(t.getC()) && t.has(getA()) && t.has(getB()) && t.has(getC());
    }

    public String toString() {
        return getA().toString() + getB().toString() + getC().toString();
    }
}
