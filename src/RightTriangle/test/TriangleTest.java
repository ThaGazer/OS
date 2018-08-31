package RightTriangle.test;

import RightTriangle.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class TriangleTest {

    private Triangle tri;

    @BeforeEach
    public void before() {
        tri = new Triangle(new Point(0,0), new Point(0,1), new Point(1,0));
    }

    @Test
    public void test_isTriangle() {
        assertTrue(tri.isTriangle());
    }

    @Test
    public void test_isRight() {
        assertTrue(tri.isRight());
    }
}