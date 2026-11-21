package com.bonefracture.xray;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

// Facade pattern - subsystem class
// Prepares an image for the detection model. Every step saves a new PNG next to the original
// ("wrist.png" -> "wrist-resized.png" -> "wrist-resized-normalized.png") and returns it as a new XRayImage,
// so the uploaded X-ray is never changed.
public class ImagePreprocessor {

    // 1st and 99th percentile: a few very dark or very bright pixels (labels, burned edges) do not decide the contrast
    private static final double LOW_PERCENTILE = 0.01;
    private static final double HIGH_PERCENTILE = 0.99;

    private int targetSize = 640;

    // Grey scale, longest side = targetSize (the input size of the YOLO model), aspect ratio kept
    public XRayImage resize(XRayImage image) {
        BufferedImage source = read(image);
        double scale = (double) targetSize / Math.max(source.getWidth(), source.getHeight());
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));

        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();

        return save(resized, image, "-resized");
    }

    // Contrast stretching: the darkest pixels become 0 and the brightest 255
    public XRayImage normalize(XRayImage image) {
        BufferedImage grey = toGrey(read(image));
        WritableRaster raster = grey.getRaster();
        int[] histogram = new int[256];
        for (int y = 0; y < grey.getHeight(); y++) {
            for (int x = 0; x < grey.getWidth(); x++) {
                histogram[raster.getSample(x, y, 0)]++;
            }
        }
        int pixels = grey.getWidth() * grey.getHeight();
        int low = percentile(histogram, pixels, LOW_PERCENTILE);
        int high = percentile(histogram, pixels, HIGH_PERCENTILE);

        if (high > low) {
            float range = high - low;
            for (int y = 0; y < grey.getHeight(); y++) {
                for (int x = 0; x < grey.getWidth(); x++) {
                    int value = raster.getSample(x, y, 0);
                    int stretched = Math.round((value - low) * 255f / range);
                    raster.setSample(x, y, 0, Math.max(0, Math.min(255, stretched)));
                }
            }
        }
        return save(grey, image, "-normalized");
    }

    public int getTargetSize() { return targetSize; }

    private static int percentile(int[] histogram, int pixels, double fraction) {
        long limit = Math.max(1, Math.round(pixels * fraction));
        long seen = 0;
        for (int value = 0; value < histogram.length; value++) {
            seen += histogram[value];
            if (seen >= limit) {
                return value;
            }
        }
        return 255;
    }

    private static BufferedImage toGrey(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return image;
        }
        BufferedImage grey = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = grey.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return grey;
    }

    private static BufferedImage read(XRayImage image) {
        try {
            BufferedImage pixels = ImageIO.read(new File(image.getPath()));
            if (pixels == null) {
                throw new IllegalArgumentException("Not an image that can be read: " + image.getPath());
            }
            return pixels;
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the X-ray: " + image.getPath(), e);
        }
    }

    private static XRayImage save(BufferedImage pixels, XRayImage original, String suffix) {
        File source = new File(original.getPath());
        String baseName = source.getName().replaceFirst("\\.[^.]+$", "");
        File png = new File(source.getParentFile(), baseName + suffix + ".png");
        try {
            ImageIO.write(pixels, "png", png);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the preprocessed X-ray: " + png.getPath(), e);
        }
        return new XRayImage(png.getPath(), FileFormat.PNG, original.getBodyPart(), original.isTakenWithCamera());
    }
}
