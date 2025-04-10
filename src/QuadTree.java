import java.awt.*;
import java.util.List;
import java.awt.image.BufferedImage;
import java.util.*;

public class QuadTree {

    private BufferedImage image;
    private int method;
    private double threshold;
    private int minSize;
    private double targetCompression;
    private Node root;
    private int maxDepth = 0;
    private Map<Integer, List<Node>> nodesByDepth = new HashMap<>();



    public QuadTree(BufferedImage image, int method, double threshold, int minSize, double targetCompression) {
        this.image = image;
        this.method = method;
        this.threshold = threshold;
        this.minSize = minSize;
        this.targetCompression = targetCompression;
    }

    public void build() {
        nodesByDepth.clear();
        Color color = averageColor(0, 0, image.getWidth(), image.getHeight());
        root = buildRecursive(0, 0, image.getWidth(), image.getHeight(), 1);
    }

    private Node buildRecursive(int x, int y, int w, int h, int depth) {
        maxDepth = Math.max(maxDepth, depth);
        Node node = new Node(x, y, w, h);
        nodesByDepth.computeIfAbsent(depth, k -> new ArrayList<>()).add(node);


        if (!shouldSplit(x, y, w, h, minSize)) {
            node.color = averageColor(x, y, w, h);
            return node;
        }else{

        int hw = w / 2;
        int hh = h / 2;

        node.children = new Node[4];
        node.children[0] = buildRecursive(x, y, hw, hh, depth + 1);
        node.children[1] = buildRecursive(x + hw, y, w - hw, hh, depth + 1);
        node.children[2] = buildRecursive(x, y + hh, hw, h - hh, depth + 1);
        node.children[3] = buildRecursive(x + hw, y + hh, w - hw, h - hh, depth + 1);

        return node;}
    }

    private boolean shouldSplit(int x, int y, int w, int h, int minSize) {
        if ((w*h/4) < minSize) return false;
        if (method == 5){
        return ErrorCalculator.compute(image, x, y, w, h, method) < threshold;
        }
        else{
            return ErrorCalculator.compute(image, x, y, w, h, method) > threshold;
        }
    }


    public Color averageColor(int x, int y, int w, int h) {
        long r = 0, g = 0, b = 0;
        int total = w * h;
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                Color c = new Color(image.getRGB(i, j));
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
            }
        }
        return new Color((int)(r / total), (int)(g / total), (int)(b / total));
    }



    public BufferedImage reconstructImage() {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        drawNode(output, root);
        return output;
    }

    private void drawNode(BufferedImage img, Node node) {
        if (node.isLeaf()) {
            Graphics2D g = img.createGraphics();
            g.setColor(node.color);
            g.fillRect(node.x, node.y, node.width, node.height);
            g.dispose();
        } else {
            for (Node child : node.children) {
                drawNode(img, child);
            }
        }
    }


    public List<BufferedImage> generateGIFFrames() {
        List<BufferedImage> frames = new ArrayList<>();
        for (int d = 1; d <= maxDepth; d++) {
            BufferedImage frame = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            reconstructQuadTreeForGIF(frame, 0, d);
            frames.add(frame);
        }
        return frames;
    }

    public void reconstructQuadTreeForGIF(BufferedImage output, int currentDepth, int depthLimit) {
        reconstructNodeForGIF(root, output, currentDepth, depthLimit);
    }

    private void reconstructNodeForGIF(Node node, BufferedImage output, int currentDepth, int depthLimit) {
        if (node.isLeaf() || currentDepth >= depthLimit) {
            if (node.color == null) {
                node.color = averageColor(node.x, node.y, node.width, node.height);
            }
            for (int y = node.y; y < node.y + node.height; y++) {
                for (int x = node.x; x < node.x + node.width; x++) {
                    output.setRGB(x, y, node.color.getRGB());
                }
            }
            return;
        }

        for (Node child : node.children) {
            reconstructNodeForGIF(child, output, currentDepth + 1, depthLimit);
        }
    }


    public int countTotalNodes() {
        return countNodes(root);
    }

    private int countNodes(Node node) {
        if (node == null) return 0;
        int count = 1;
        if (!node.isLeaf()) {
            for (Node child : node.children) count += countNodes(child);
        }
        return count;
    }

    public int getMaxDepth() {
        return maxDepth;
    }
}