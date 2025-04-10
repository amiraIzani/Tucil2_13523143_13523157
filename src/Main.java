import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        String inputPath;
        while (true) {
            System.out.print("Masukkan path gambar input (.png/.jpg/.jpeg): ");
            inputPath = sc.nextLine().trim();
            File inputFile = new File(inputPath);
            if (inputFile.exists() && inputFile.isFile()
                    && (inputPath.endsWith(".png") || inputPath.endsWith(".jpg") || inputPath.endsWith(".jpeg"))) {
                break;
            }
            System.out.println("Error: File tidak ditemukan atau format tidak sesuai!");
        }

        int errorMethod;
        while (true) {
            System.out.print("Pilih metode error (1: Variance, 2: MAD, 3: MaxDiff, 4: Entropy, 5: SSIM): ");
            if (sc.hasNextInt()) {
                errorMethod = sc.nextInt();
                if (errorMethod >= 1 && errorMethod <= 5) {
                    break;
                }
            }
            System.out.println("Error: Pilihan tidak valid!");
            sc.nextLine();
        }

        System.out.print("Masukkan nilai threshold ");
        switch (errorMethod) {
            case 1 -> System.out.print("Variance (ideal range [0..16384)): ");
            case 2 -> System.out.print("Mean Absolute Deviation (ideal range [0..127.5]): ");
            case 3 -> System.out.print("Max Pixel Difference (ideal range [0..255]): ");
            case 4 -> System.out.print("Entropy (ideal range [0..8]): ");
            case 5 -> System.out.print("Structural Similarity Index (ideal range [0..1]): ");
        }
        double threshold = sc.nextDouble();

        int minBlockSize;
        while (true) {
            System.out.print("Masukkan ukuran blok minimum (>=1): ");
            if (sc.hasNextInt()) {
                minBlockSize = sc.nextInt();
                if (minBlockSize > 0) break;
            }
            System.out.println("Error: Ukuran blok minimum harus >= 1!");
            sc.nextLine();
        }

        double targetCompression;
        while (true) {
            System.out.print("Masukkan target persentase kompresi (0 untuk nonaktifkan, 0.0 hingga 1.0): ");
            if (sc.hasNextDouble()) {
                targetCompression = sc.nextDouble();
                if (targetCompression >= 0 && targetCompression <= 1) break;
            }
            System.out.println("Error: Masukkan angka antara 0 dan 1!");
            sc.nextLine();
        }
        sc.nextLine(); // clear newline

        String outputPath;
        while (true) {
            System.out.print("Masukkan path untuk gambar hasil (.png/.jpg/.jpeg): ");
            outputPath = sc.nextLine().trim();
            if (outputPath.endsWith(".png") || outputPath.endsWith(".jpg") || outputPath.endsWith(".jpeg")) {
                break;
            }
            System.out.println("Error: Format output tidak valid!");
        }

        String gifPath;
        while (true) {
            System.out.print("Masukkan path GIF (kosongkan jika tidak perlu): ");
            gifPath = sc.nextLine().trim();
            if (gifPath.isEmpty() || gifPath.endsWith(".gif")) {
                break;
            }
            System.out.println("Error: Path harus berakhiran .gif atau kosong!");
        }

        // Mulai proses kompresi
        sc.close();

        ImageCompressor.run(inputPath, errorMethod, threshold, minBlockSize, targetCompression, outputPath, gifPath);
    }
}
