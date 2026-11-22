package com.bonefracture.detection;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class LetterboxTest {

    private static final int SIZE = 640;

    private static BufferedImage filled(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }

    private static float at(float[] tensor, int channel, int x, int y) {
        return tensor[channel * SIZE * SIZE + y * SIZE + x];
    }

    @Test
    void wideImageIsScaledDownAndPaddedTopAndBottom() {
        Letterbox box = Letterbox.of(filled(1280, 640, Color.WHITE), SIZE);
        assertEquals(0.5f, box.scale(), 1e-6);
        assertEquals(0, box.padLeft());
        assertEquals(160, box.padTop());
        assertEquals(3 * SIZE * SIZE, box.tensor().length);
    }

    @Test
    void paddingIsGrey() {
        float[] tensor = Letterbox.of(filled(1280, 640, Color.WHITE), SIZE).tensor();
        for (int channel = 0; channel < 3; channel++) {
            assertEquals(114 / 255f, at(tensor, channel, 10, 10), 1e-6);
            assertEquals(114 / 255f, at(tensor, channel, 10, 630), 1e-6);
        }
    }

    @Test
    void pixelsAreRgbBetweenZeroAndOne() {
        float[] tensor = Letterbox.of(filled(640, 640, new Color(255, 0, 51)), SIZE).tensor();
        assertEquals(1f, at(tensor, 0, 320, 320), 1e-6);
        assertEquals(0f, at(tensor, 1, 320, 320), 1e-6);
        assertEquals(0.2f, at(tensor, 2, 320, 320), 1e-6);
    }

    @Test
    void sixteenBitGreyIsReducedToEightBits() {
        BufferedImage grey = new BufferedImage(640, 640, BufferedImage.TYPE_USHORT_GRAY);
        for (int y = 0; y < 640; y++) {
            for (int x = 0; x < 640; x++) {
                grey.getRaster().setSample(x, y, 0, x < 320 ? 65535 : 32768);
            }
        }
        float[] tensor = Letterbox.of(grey, SIZE).tensor();
        for (int channel = 0; channel < 3; channel++) {
            assertEquals(1f, at(tensor, channel, 100, 100), 1e-6);
            assertEquals(128 / 255f, at(tensor, channel, 500, 100), 1e-6);
        }
    }

    @Test
    void boxIsMappedBackToTheOriginalImage() {
        Letterbox box = Letterbox.of(filled(1280, 640, Color.WHITE), SIZE);
        Rectangle r = box.toImageBox(new YoloBox(3, 0.9f, 100, 170, 300, 330));
        assertEquals(new Rectangle(200, 20, 400, 320), r);
    }

    @Test
    void boxIsClippedToTheImage() {
        Letterbox box = Letterbox.of(filled(1280, 640, Color.WHITE), SIZE);
        Rectangle r = box.toImageBox(new YoloBox(3, 0.9f, -20, 150, 700, 500));
        assertEquals(new Rectangle(0, 0, 1280, 640), r);
    }
}
