
import org.junit.jupiter.api.*;
import triangle.*;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TriangleTest {

  private Triangle tri;

  @BeforeEach
  void before() {
    tri = new Triangle(new Point(0, 0), new Point(0, 1), new Point(1, 0));
  }

  @Test
  void test_isTriangle() {
    assertTrue(tri.isTriangle());
  }

  @Test
  void test_isRight() {
    assertTrue(tri.isRight());
  }

  @Test
  void test_Equals() {
    Random rnd = new Random(System.nanoTime());

    for(int i = 0; i < 10000; i++) {
      Point[] pnt = new Point[]{new Point(rnd.nextInt(), rnd.nextInt()), new Point(rnd.nextInt(), rnd.nextInt()), new Point(rnd.nextInt(), rnd.nextInt())};
      tri = new Triangle(pnt[0], pnt[1], pnt[2]);
      Triangle test = new Triangle(pnt[2], pnt[1], pnt[0]);

      assertEquals(tri, test);
    }
  }

  @Test
  void test_EqualtPoints() {
    Triangle tri2 = new Triangle(new Point(0, 0), new Point(0, 1), new Point(0, 1));
    Triangle tri3 = new Triangle(new Point(0, 1), new Point(0, 1), new Point(0, 1));

    assertNotEquals(tri, tri2);
    assertNotEquals(tri2, tri);
    assertNotEquals(tri2, tri3);
  }

  @Test
  void test_Point() {
    Point p1 = new Point(0, 1);
    Point p2 = new Point(1, 0);
    assertNotEquals(p1, p2);
  }

  @Test
  void timeReads() {

  }
}