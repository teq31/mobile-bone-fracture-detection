package com.bonefracture.xray.adapter;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TDD Iteration 1 - ADAPTER pattern: plain image files (JPG / PNG) -> XRayImage.
 */
class ImageFileAdapterTest {

    private File jpgFile;

    @BeforeEach
    void setUp() throws IOException {
        jpgFile = Files.createTempFile("wrist", ".jpg").toFile();
        Files.write(jpgFile.toPath(), new byte[]{1, 2, 3});
        jpgFile.deleteOnExit();
    }

    @Test
    void toXRayImageKeepsPathFormatAndBodyPart() {
        XRayImageSource source = new ImageFileAdapter(jpgFile, FileFormat.JPG);

        XRayImage image = source.toXRayImage(BodyPart.WRIST);

        assertNotNull(image);
        assertEquals(jpgFile.getPath(), image.getPath());
        assertEquals(FileFormat.JPG, image.getFormat());
        assertEquals(BodyPart.WRIST, image.getBodyPart());
    }

    @Test
    void constructorRejectsFormatThatIsNotAnImage() {
        assertThrows(IllegalArgumentException.class, () -> new ImageFileAdapter(jpgFile, FileFormat.PDF));
    }

    @Test
    void toXRayImageRejectsMissingFile() {
        XRayImageSource source = new ImageFileAdapter(new File("does-not-exist.png"), FileFormat.PNG);

        assertThrows(IllegalArgumentException.class, () -> source.toXRayImage(BodyPart.WRIST));
    }

    @Test
    void toXRayImageRequiresBodyPart() {
        XRayImageSource source = new ImageFileAdapter(jpgFile, FileFormat.JPG);

        assertThrows(IllegalArgumentException.class, () -> source.toXRayImage(null));
    }
}
