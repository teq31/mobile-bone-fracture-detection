package com.bonefracture.detection;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Arrays;

// Resizes an image to size x size the way Ultralytics does before YOLO: the aspect ratio is kept,
// the rest is filled with grey (114) and the pixels go into a 1x3xSxS RGB tensor with values 0..1.
// Remembers the scale and padding so the predicted boxes can be mapped back to the original image.
final class Letterbox {

    private static final float PAD_VALUE = 114 / 255f;

    private final float[] tensor;
    private final float scale;
    private final int padLeft;
    private final int padTop;
    private final int imageWidth;
    private final int imageHeight;

    private Letterbox(float[] tensor, float scale, int padLeft, int padTop, int imageWidth, int imageHeight) {
        this.tensor = tensor;
        this.scale = scale;
        this.padLeft = padLeft;
        this.padTop = padTop;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    static Letterbox of(BufferedImage image, int size) {
        int width = image.getWidth();
        int height = image.getHeight();
        float scale = Math.min((float) size / width, (float) size / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        int padLeft = Math.round((size - newWidth) / 2f - 0.1f);
        int padTop = Math.round((size - newHeight) / 2f - 0.1f);

        float[][] channels = readRgb(image);
        float[] tensor = new float[3 * size * size];
        Arrays.fill(tensor, PAD_VALUE);

        // bilinear resize with pixel centres aligned (same as OpenCV INTER_LINEAR)
        float ratioX = (float) width / newWidth;
        float ratioY = (float) height / newHeight;
        for (int y = 0; y < newHeight; y++) {
            float sy = Math.max((y + 0.5f) * ratioY - 0.5f, 0);
            int y0 = Math.min((int) sy, height - 1);
            int y1 = Math.min(y0 + 1, height - 1);
            float wy = sy - y0;
            for (int x = 0; x < newWidth; x++) {
                float sx = Math.max((x + 0.5f) * ratioX - 0.5f, 0);
                int x0 = Math.min((int) sx, width - 1);
                int x1 = Math.min(x0 + 1, width - 1);
                float wx = sx - x0;
                int target = (y + padTop) * size + (x + padLeft);
                for (int c = 0; c < 3; c++) {
                    float[] ch = channels[c];
                    float top = ch[y0 * width + x0] * (1 - wx) + ch[y0 * width + x1] * wx;
                    float bottom = ch[y1 * width + x0] * (1 - wx) + ch[y1 * width + x1] * wx;
                    tensor[c * size * size + target] = Math.round(top * (1 - wy) + bottom * wy) / 255f;
                }
            }
        }
        return new Letterbox(tensor, scale, padLeft, padTop, width, height);
    }

    // RGB values 0..255. Grey images with more than 8 bits (X-ray PNGs are 16 bit) are reduced to 8 bits.
    private static float[][] readRgb(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        float[][] rgb = new float[3][width * height];
        Raster raster = image.getRaster();
        if (raster.getNumBands() == 1) {
            float divisor = 1 << Math.max(0, raster.getSampleModel().getSampleSize(0) - 8);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    float value = Math.min(255, Math.round(raster.getSample(x, y, 0) / divisor));
                    int i = y * width + x;
                    rgb[0][i] = value;
                    rgb[1][i] = value;
                    rgb[2][i] = value;
                }
            }
            return rgb;
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int i = y * width + x;
                rgb[0][i] = (pixel >> 16) & 0xFF;
                rgb[1][i] = (pixel >> 8) & 0xFF;
                rgb[2][i] = pixel & 0xFF;
            }
        }
        return rgb;
    }

    float[] tensor() {
        return tensor;
    }

    float scale() {
        return scale;
    }

    int padLeft() {
        return padLeft;
    }

    int padTop() {
        return padTop;
    }

    Rectangle toImageBox(YoloBox box) {
        int x1 = clamp(Math.round((box.x1() - padLeft) / scale), imageWidth);
        int y1 = clamp(Math.round((box.y1() - padTop) / scale), imageHeight);
        int x2 = clamp(Math.round((box.x2() - padLeft) / scale), imageWidth);
        int y2 = clamp(Math.round((box.y2() - padTop) / scale), imageHeight);
        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(value, max));
    }
}
