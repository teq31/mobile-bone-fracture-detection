package com.bonefracture.detection;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lab 7 - HeatmapGenerator: a heatmap the size of the X-ray, strongest in the centre of the detection
 * and fading outside it, so the app can draw it over the image.
 */
class HeatmapGeneratorTest {

    private XRayImage xray;
    private final HeatmapGenerator generator = new HeatmapGenerator();

    @BeforeEach
    void setUp() throws IOException {
        File folder = Files.createTempDirectory("heatmap").toFile();
        File file = new File(folder, "wrist.png");
        ImageIO.write(new BufferedImage(200, 100, BufferedImage.TYPE_BYTE_GRAY), "png", file);
        xray = new XRayImage(file.getPath(), FileFormat.PNG, BodyPart.WRIST, false);
    }

    private static int alpha(BufferedImage image, int x, int y) {
        return (image.getRGB(x, y) >>> 24) & 0xFF;
    }

    @Test
    void heatmapHasTheSizeOfTheXRay() {
        Detection detection = new Detection("fracture", 0.9f, new Rectangle(50, 20, 40, 40), null);

        Heatmap heatmap = generator.generate(xray, detection);

        assertNotNull(heatmap);
        BufferedImage image = (BufferedImage) heatmap.getImage();
        assertEquals(200, image.getWidth());
        assertEquals(100, image.getHeight());
    }

    @Test
    void heatmapIsStrongestInTheCentreOfTheDetection() {
        Detection detection = new Detection("fracture", 0.9f, new Rectangle(50, 20, 40, 40), null);

        BufferedImage image = (BufferedImage) generator.generate(xray, detection).getImage();

        int centre = alpha(image, 70, 40);
        assertTrue(centre > alpha(image, 55, 40), "centre is stronger than the edge of the box");
        assertTrue(alpha(image, 55, 40) > alpha(image, 180, 90), "the box is stronger than far away");
        assertEquals(0, alpha(image, 199, 0));
    }

    @Test
    void lowerConfidenceGivesAWeakerHeatmap() {
        Rectangle box = new Rectangle(50, 20, 40, 40);

        BufferedImage sure = (BufferedImage) generator.generate(xray, new Detection("fracture", 0.9f, box, null)).getImage();
        BufferedImage unsure = (BufferedImage) generator.generate(xray, new Detection("fracture", 0.3f, box, null)).getImage();

        assertTrue(alpha(sure, 70, 40) > alpha(unsure, 70, 40));
    }
}
