package com.bonefracture.xray;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lab 7 - ImagePreprocessor: the real preprocessing before detection
 * (resize so the longest side is 640 px, grey scale, contrast stretching).
 */
class ImagePreprocessorTest {

    private File folder;
    private final ImagePreprocessor preprocessor = new ImagePreprocessor();

    @BeforeEach
    void setUp() throws IOException {
        folder = Files.createTempDirectory("preprocessor").toFile();
    }

    private XRayImage xray(String name, BufferedImage pixels) throws IOException {
        File file = new File(folder, name);
        ImageIO.write(pixels, "png", file);
        return new XRayImage(file.getPath(), FileFormat.PNG, BodyPart.WRIST, false);
    }

    private static BufferedImage grey(int width, int height, int value) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.getRaster().setSample(x, y, 0, value);
            }
        }
        return image;
    }

    @Test
    void resizeScalesLongestSideToTargetSizeAndKeepsAspectRatio() throws IOException {
        XRayImage resized = preprocessor.resize(xray("wrist.png", grey(1280, 960, 100)));

        BufferedImage pixels = ImageIO.read(new File(resized.getPath()));
        assertEquals(640, pixels.getWidth());
        assertEquals(480, pixels.getHeight());
        assertEquals(FileFormat.PNG, resized.getFormat());
        assertEquals(BodyPart.WRIST, resized.getBodyPart());
        assertEquals(new File(folder, "wrist-resized.png").getPath(), resized.getPath());
    }

    @Test
    void resizeEnlargesSmallImages() throws IOException {
        XRayImage resized = preprocessor.resize(xray("small.png", grey(200, 400, 100)));

        BufferedImage pixels = ImageIO.read(new File(resized.getPath()));
        assertEquals(320, pixels.getWidth());
        assertEquals(640, pixels.getHeight());
    }

    @Test
    void resizeConvertsColourImagesToGrey() throws IOException {
        BufferedImage colour = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        XRayImage resized = preprocessor.resize(xray("colour.png", colour));

        BufferedImage pixels = ImageIO.read(new File(resized.getPath()));
        assertEquals(1, pixels.getRaster().getNumBands());
    }

    @Test
    void normalizeStretchesContrastToFullRange() throws IOException {
        BufferedImage dull = grey(100, 100, 100);
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 100; y++) {
                dull.getRaster().setSample(x, y, 0, 150);
            }
        }

        XRayImage normalized = preprocessor.normalize(xray("dull.png", dull));

        BufferedImage pixels = ImageIO.read(new File(normalized.getPath()));
        assertEquals(255, pixels.getRaster().getSample(10, 10, 0));
        assertEquals(0, pixels.getRaster().getSample(90, 10, 0));
        assertTrue(normalized.getPath().endsWith("dull-normalized.png"));
    }

    @Test
    void normalizeKeepsAFlatImageUnchanged() throws IOException {
        XRayImage normalized = preprocessor.normalize(xray("flat.png", grey(10, 10, 77)));

        BufferedImage pixels = ImageIO.read(new File(normalized.getPath()));
        assertEquals(77, pixels.getRaster().getSample(5, 5, 0));
    }

    @Test
    void unreadableImageIsRejected() throws IOException {
        File notAnImage = new File(folder, "broken.png");
        Files.write(notAnImage.toPath(), new byte[]{1, 2, 3});
        XRayImage broken = new XRayImage(notAnImage.getPath(), FileFormat.PNG, BodyPart.WRIST, false);

        assertThrows(IllegalArgumentException.class, () -> preprocessor.resize(broken));
        assertThrows(IllegalArgumentException.class, () -> preprocessor.normalize(broken));
    }
}
