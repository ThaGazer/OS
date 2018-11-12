package triangle;

public class Triangle {

  private static final String errBigInt = "oops to big: ";

  private long a, b, c;

  public Triangle() {
    setA(-1);
    setB(-1);
    setC(-1);
  }

  public Triangle(int a, int b, int c) {
    setA(a);
    setB(b);
    setC(c);
  }

  public Triangle(Triangle t) {
    this(t.getA(), t.getB(), t.getC());
  }

  public void setA(int p) {
    if((long)p > Long.MAX_VALUE) {
      throw new IndexOutOfBoundsException(errBigInt);
    }
    a = p;
  }

  public void setB(int p) {
    if((long)p > Long.MAX_VALUE) {
      throw new IndexOutOfBoundsException(errBigInt);
    }
    b = p;
  }

  public void setC(int p) {
    if((long)p > Long.MAX_VALUE) {
      throw new IndexOutOfBoundsException(errBigInt);
    }
    c = p;
  }

  public int getA() {
    return (int)a;
  }

  public int getB() {
    return (int)b;
  }

  public int getC() {
    return (int)c;
  }

/*  public boolean isRight() {
    return right;
  }

  public boolean isTriangle() {
    if(getA() == getB() || getA() == getC() || getB() == getC()) {
      return false;
    }
    return true;
    *//*return !getA().isZeroSlope(getB(), getC());*//*
  }

  private boolean rightCheck() {
*//*    if(isTriangle()) {
      int a, b, c, temp;
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

      return a + b == c;
    } else {
      return false;
    }*//*
  }*/

  private boolean has(int p) {
    if(getA() == p) {
      return true;
    } else if(getB() == p) {
      return true;
    } else {
      return getC() == p;
    }
  }

  @Override
  public int hashCode() {
    return Long.hashCode(a) + Long.hashCode(b) + Long.hashCode(c);
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
    return "(" + getA() + ") (" + getB() + ") (" + getC() + ")";
  }
}
