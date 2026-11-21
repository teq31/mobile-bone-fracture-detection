package com.bonefracture.xray.adapter;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD Iteration 1 - ADAPTER pattern: a photo taken with the phone camera -> XRayImage (JPG).
 */
class CameraXRayAdapterTest {

    private File saveDirectory;

    @BeforeEach
    void setUp() throws IOException {
        saveDirectory = Files.createTempDirectory("camera-adapter").toFile();
    }

    @Test
    void toXRayImageSavesPhotoAsJpg() throws IOException {
        byte[] photo = {10, 20, 30, 40};
        XRayImageSource source = new CameraXRayAdapter(new CameraCapture(photo, 2, 2), saveDirectory.getPath());

        XRayImage image = source.toXRayImage(BodyPart.ANKLE);

        assertNotNull(image);
        assertEquals(FileFormat.JPG, image.getFormat());
        assertEquals(BodyPart.ANKLE, image.getBodyPart());
        File saved = new File(image.getPath());
        assertTrue(saved.exists());
        assertEquals(saveDirectory, saved.getParentFile());
        assertArrayEquals(photo, Files.readAllBytes(saved.toPath()));
    }

    @Test
    void toXRayImageRejectsEmptyPhoto() {
        XRayImageSource source = new CameraXRayAdapter(new CameraCapture(new byte[0], 0, 0), saveDirectory.getPath());

        assertThrows(IllegalArgumentException.class, () -> source.toXRayImage(BodyPart.WRIST));
    }
}
