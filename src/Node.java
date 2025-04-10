
import java.awt.Color;

public class Node {
    public int x, y, width, height;
    public Color color;
    public Node[] children;

    public Node(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isLeaf() {
        return children == null;
    }
}
