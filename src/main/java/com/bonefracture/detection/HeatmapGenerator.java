package com.bonefracture.detection;

import com.bonefracture.xray.XRayImage;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// Facade - subsystem
// box-based heatmap for now, swap for EigenCAM once we have the onnx model
public class HeatmapGenerator {

    private static final int MAX_ALPHA = 180;           // the X-ray stays visible under the heatmap
    private static final double SPREAD = 0.5;           // sigma = half of the box size

    private String method = "box-gaussian";

    public Heatmap generate(XRayImage image, Detection detection) {
        int[] size = imageSize(image);
        int width = size[0];
        int height = size[1];
        Rectangle box = detection.getBoundingBox();
        double centreX = box.getCenterX();
        double centreY = box.getCenterY();
        double sigmaX = Math.max(1.0, box.getWidth() * SPREAD);
        double sigmaY = Math.max(1.0, box.getHeight() * SPREAD);
        float confidence = Math.max(0f, Math.min(1f, detection.getConfidence()));

        BufferedImage heatmap = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double dx = (x - centreX) / sigmaX;
                double dy = (y - centreY) / sigmaY;
                double heat = Math.exp(-(dx * dx + dy * dy) / 2) * confidence;
                int alpha = (int) Math.round(heat * MAX_ALPHA);
                if (alpha > 0) {
                    heatmap.setRGB(x, y, (alpha << 24) | colour(heat));
                }
            }
        }
        return new Heatmap(heatmap);
    }

    public String getMethod() { return method; }

    // jet colormap: blue -> green -> red
    private static int colour(double heat) {
        int red = (int) Math.round(255 * clamp(1.5 - Math.abs(4 * heat - 3)));
        int green = (int) Math.round(255 * clamp(1.5 - Math.abs(4 * heat - 2)));
        int blue = (int) Math.round(255 * clamp(1.5 - Math.abs(4 * heat - 1)));
        return (red << 16) | (green << 8) | blue;
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private static int[] imageSize(XRayImage image) {
        try {
            BufferedImage pixels = ImageIO.read(new File(image.getPath()));
            if (pixels == null) {
                throw new IllegalArgumentException("Not an image that can be read: " + image.getPath());
            }
            return new int[]{pixels.getWidth(), pixels.getHeight()};
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the X-ray: " + image.getPath(), e);
        }
    }
}
