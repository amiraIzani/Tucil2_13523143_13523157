import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageCompressor {
    public static void run(String inputPath, int method, double threshold, int minBlockSize,
                           double targetCompression, String outputPath, String gifPath) {
        try {
            long startTime = System.nanoTime();
            BufferedImage image = ImageIO.read(new File(inputPath));

            QuadTree tree;
            GIFExporter gif = (gifPath != null && !gifPath.trim().isEmpty()) ? new GIFExporter(gifPath, 500) : null;

            if (targetCompression > 0.0 && targetCompression <= 1.0) { //Jika target kompresi di set
                double low, high, bestThresh;


                switch (method) {
                    case 1 -> { //Variance
                        low = 0.0;
                        high = 16384.0;
                    }
                    case 2 -> { //MAD
                        low = 0.0;
                        high = 255.0;
                    }
                    case 3 -> { //Max Pixel Difference
                        low = 0.0;
                        high = 255.0;
                    }
                    case 4 -> { //Entropy
                        low = 0.0;
                        high = 8.0;
                    }
                    case 5 -> { //SSIM
                        low = -1.0; //avoid error
                        high = 1.0;
                    }
                    default -> {
                        low = 0.0;
                        high = 10000.0;
                    }
                }

                bestThresh = threshold;

                double epsilon = 0.1;
                int maxIterations = 30;

                File inputFile = new File(inputPath);
                long originalSize = inputFile.length();



                for (int i = 0; i < maxIterations; i++) {
                    double mid = (low + high) / 2.0;
                    QuadTree temp = new QuadTree(image, method, mid, minBlockSize, targetCompression);
                    temp.build();


                    BufferedImage output = temp.reconstructImage();


                    File tempFile = File.createTempFile("compressed_", ".png");
                    ImageIO.write(output, "png", tempFile);

                    long compressedSize = tempFile.length();
                    double compressionRatio = 1.0 - ((double) compressedSize / originalSize);

                    tempFile.delete();

                    if (method == 5) {
                        if (compressionRatio > targetCompression) {
                            low = mid;
                            bestThresh = mid;
                        } else {
                            high = mid;
                        }
                    }
                    else {
                        if (compressionRatio < targetCompression) {
                            low = mid;
                        } else {
                            high = mid;
                            bestThresh = mid;
                        }
                    }

                    if (Math.abs(high - low) < epsilon) {
                        break;
                    }
                }

                System.out.printf("Threshold terbaik ditemukan: %.4f untuk target kompresi %.2f%%\n",
                        bestThresh, targetCompression * 100.0);

                tree = new QuadTree(image, method, bestThresh, minBlockSize, targetCompression);
            } else {
                // Mode manual
                tree = new QuadTree(image, method, threshold, minBlockSize, targetCompression);
            }



            tree.build();

            if (gif != null) {
                for (BufferedImage frame : tree.generateGIFFrames()) {
                    gif.addFrame(frame);
                }
                gif.close();
            }

            BufferedImage output = tree.reconstructImage();
            ImageIO.write(output, "png", new File(outputPath));

            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1e6; //ms

            File inputFile = new File(inputPath);
            File outputFile = new File(outputPath);

            long originalSize = inputFile.length();
            long compressedSize = outputFile.length();

            double compressionPercent = 100.0 * (1 - (double) compressedSize / originalSize);

            System.out.println("HASIL: \n");
            System.out.printf("Waktu eksekusi: %.2f ms\n", duration);
            System.out.println("Ukuran gambar sebelum: " + originalSize);
            System.out.println("Ukuran gambar sesudah: " + compressedSize);
            System.out.printf("Persentase kompresi: %.2f%%\n", compressionPercent);
            System.out.println("Kedalaman pohon: " + tree.getMaxDepth());
            System.out.println("Jumlah simpul: " + tree.countTotalNodes());


            System.out.printf("\nSuccess! Output disimpan di: %s\n", outputPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}