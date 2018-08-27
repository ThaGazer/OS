package RightTriangle;

public class TrianglesThreaded extends TrianglesClass {
    private static final String errParams = "Usage: <filename> <nPros>";

    public static void main(String[] args) throws Exception {
        TrianglesThreaded t = new TrianglesThreaded();

        if(args.length < 2) {
            throw new IllegalArgumentException(errParams);
        }

        t.readPoints(args[0]);
    }

    @Override
    protected int findTriangles() {
        return 0;
    }
}
