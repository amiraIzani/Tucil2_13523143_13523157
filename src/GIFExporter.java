
import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.ImageTypeSpecifier;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GIFExporter {
    private ImageWriter writer;
    private IIOMetadata metadata;
    private ImageOutputStream output;

    public GIFExporter(String path, int delayMs) throws IOException {
        writer = ImageIO.getImageWritersBySuffix("gif").next();
        output = ImageIO.createImageOutputStream(new File(path));
        writer.setOutput(output);
        writer.prepareWriteSequence(null);

        ImageTypeSpecifier type = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        metadata = writer.getDefaultImageMetadata(type, null);
        String format = metadata.getNativeMetadataFormatName();

        IIOMetadataNode root = new IIOMetadataNode(format);
        IIOMetadataNode gce = new IIOMetadataNode("GraphicControlExtension");
        gce.setAttribute("disposalMethod", "none");
        gce.setAttribute("userInputFlag", "FALSE");
        gce.setAttribute("transparentColorFlag", "FALSE");
        gce.setAttribute("delayTime", Integer.toString(delayMs / 10));
        gce.setAttribute("transparentColorIndex", "0");
        root.appendChild(gce);

        IIOMetadataNode appExtensions = new IIOMetadataNode("ApplicationExtensions");
        IIOMetadataNode appExtension = new IIOMetadataNode("ApplicationExtension");
        appExtension.setAttribute("applicationID", "NETSCAPE");
        appExtension.setAttribute("authenticationCode", "2.0");
        appExtension.setUserObject(new byte[]{0x1, 0x0, 0x0});
        appExtensions.appendChild(appExtension);
        root.appendChild(appExtensions);

        metadata.mergeTree(format, root);
    }

    public void addFrame(BufferedImage image) throws IOException {
        IIOImage frame = new IIOImage(image, null, metadata);
        writer.writeToSequence(frame, null);
    }

    public void close() throws IOException {
        writer.endWriteSequence();
        output.close();
    }



}
