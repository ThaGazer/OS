package RightTriangle.test;

import RightTriangle.*;
import org.junit.jupiter.api.*;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TriangleTest {

    private Triangle tri;

    @BeforeEach
    void before() {
        tri = new Triangle(new Point(0,0), new Point(0,1), new Point(1,0));
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
            Triangle test = new Triangle(pnt[rnd.nextInt(3)], pnt[rnd.nextInt(3)], pnt[rnd.nextInt(3)]);

            assertEquals(tri, test);
        }
    }
}