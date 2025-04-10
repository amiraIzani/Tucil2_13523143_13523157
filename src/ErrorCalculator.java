
import java.awt.image.BufferedImage;
import java.awt.Color;

public class ErrorCalculator {
    public static double compute(BufferedImage img, int x, int y, int w, int h, int method) {
        return switch (method) {
            case 1 -> variance(img, x, y, w, h);
            case 2 -> mad(img, x, y, w, h);
            case 3 -> maxDiff(img, x, y, w, h);
            case 4 -> entropy(img, x, y, w, h);
            case 5 -> structuralSimilarity(img, x, y, w, h);
            default -> 0;
        };
    }

    public static double variance(BufferedImage img, int x, int y, int width, int height) {
        double[] sum = {0, 0, 0};
        double[] sumSq = {0, 0, 0};
        int n = width * height;

        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                Color c = new Color(img.getRGB(i, j));
                sum[0] += c.getRed();
                sum[1] += c.getGreen();
                sum[2] += c.getBlue();
                sumSq[0] += c.getRed() * c.getRed();
                sumSq[1] += c.getGreen() * c.getGreen();
                sumSq[2] += c.getBlue() * c.getBlue();
            }
        }

        double varR = sumSq[0] / n - Math.pow(sum[0] / n, 2);
        double varG = sumSq[1] / n - Math.pow(sum[1] / n, 2);
        double varB = sumSq[2] / n - Math.pow(sum[2] / n, 2);

        return (varR + varG + varB) / 3.0;
    }

    public static double mad(BufferedImage img, int x, int y, int width, int height) {
        double[] mean = {0, 0, 0};
        int n = width * height;

        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                Color c = new Color(img.getRGB(i, j));
                mean[0] += c.getRed();
                mean[1] += c.getGreen();
                mean[2] += c.getBlue();
            }
        }

        mean[0] /= n;
        mean[1] /= n;
        mean[2] /= n;

        double[] total = {0, 0, 0};
        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                Color c = new Color(img.getRGB(i, j));
                total[0] += Math.abs(c.getRed() - mean[0]);
                total[1] += Math.abs(c.getGreen() - mean[1]);
                total[2] += Math.abs(c.getBlue() - mean[2]);
            }
        }

        return (total[0] + total[1] + total[2]) / (3.0 * n);
    }

    public static double maxDiff(BufferedImage img, int x, int y, int width, int height) {
        int minR = 255, minG = 255, minB = 255;
        int maxR = 0, maxG = 0, maxB = 0;

        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                Color c = new Color(img.getRGB(i, j));
                int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
                if (r < minR) minR = r;
                if (r > maxR) maxR = r;
                if (g < minG) minG = g;
                if (g > maxG) maxG = g;
                if (b < minB) minB = b;
                if (b > maxB) maxB = b;
            }
        }

        return ((maxR - minR) + (maxG - minG) + (maxB - minB)) / 3.0;
    }

    public static double entropy(BufferedImage img, int x, int y, int width, int height) {
        int[] histogram = new int[256];
        int n = width * height;

        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                Color c = new Color(img.getRGB(i, j));
                int avg = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                histogram[avg]++;
            }
        }

        double entropy = 0.0;
        for (int count : histogram) {
            if (count > 0) {
                double p = (double) count / n;
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }

        return entropy;
    }



    public static double structuralSimilarity(BufferedImage image, int x, int y, int width, int height) {
        long r = 0, g = 0, b = 0;
        int total = width * height;
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                Color c = new Color(image.getRGB(i, j));
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
            }
        }

        int avgR = (int)(r / total);
        int avgG = (int)(g / total);
        int avgB = (int)(b / total);

        double ssimR = computeSSIM(image, avgR, x, y, width, height, 'r');
        double ssimG = computeSSIM(image, avgG, x, y, width, height, 'g');
        double ssimB = computeSSIM(image, avgB, x, y, width, height, 'b');

        return 0.2989 * ssimR + 0.5870 * ssimG + 0.1140 * ssimB;
    }


    private static double computeSSIM(BufferedImage image, int meanY, int x, int y, int width, int height, char channel) {
        final double C1 = 6.5025;
        final double C2 = 58.5225;

        double sumX = 0, sumX2 = 0, sumXY = 0;
        int N = width * height;

        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                Color c = new Color(image.getRGB(i, j));
                int valX = switch (channel) {
                    case 'r' -> c.getRed();
                    case 'g' -> c.getGreen();
                    case 'b' -> c.getBlue();
                    default -> -1;
                };

                sumX += valX;
                sumX2 += valX * valX;
                sumXY += valX * meanY;
            }
        }

        double meanX = sumX / N;
        double varianceX = sumX2 / N - meanX * meanX;
        double varianceY = 0;
        double covariance = sumXY / N - meanX * meanY;

        double numerator = (2 * meanX * meanY + C1) * (2 * covariance + C2);
        double denominator = (meanX * meanX + meanY * meanY + C1) * (varianceX + varianceY + C2);

        return numerator / denominator;
    }

}


