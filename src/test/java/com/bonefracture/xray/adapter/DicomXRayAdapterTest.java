package com.bonefracture.xray.adapter;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TDD Iteration 1 - ADAPTER pattern: a DICOM file (16-bit pixels + metadata) -> XRayImage (8-bit PNG).
 * FakeDicomFile stands in for the external DICOM library (the Adaptee).
 */
class DicomXRayAdapterTest {

    private File folder;

    @BeforeEach
    void setUp() throws IOException {
        folder = Files.createTempDirectory("dicom-adapter").toFile();
    }

    @Test
    void toXRayImageConvertsPixelsToPng() throws IOException {
        FakeDicomFile dicom = new FakeDicomFile(new File(folder, "study.dcm"), "WRIST",
                2, 2, new short[]{0, 1000, 2000, 4000});
        XRayImageSource source = new DicomXRayAdapter(dicom);

        XRayImage image = source.toXRayImage(BodyPart.WRIST);

        assertNotNull(image);
        assertEquals(FileFormat.PNG, image.getFormat());
        assertEquals(new File(folder, "study.png").getPath(), image.getPath());
        BufferedImage png = ImageIO.read(new File(image.getPath()));
        assertEquals(2, png.getWidth());
        assertEquals(2, png.getHeight());
        assertEquals(0, png.getRaster().getSample(0, 0, 0));    // darkest pixel -> 0
        assertEquals(255, png.getRaster().getSample(1, 1, 0));  // brightest pixel -> 255
    }

    @Test
    void toXRayImageUsesBodyPartFromDicomWhenNotGiven() {
        FakeDicomFile dicom = new FakeDicomFile(new File(folder, "study.dcm"), "WRIST",
                1, 1, new short[]{500});
        XRayImageSource source = new DicomXRayAdapter(dicom);

        XRayImage image = source.toXRayImage(null);

        assertNotNull(image);
        assertEquals(BodyPart.WRIST, image.getBodyPart());
    }

    @Test
    void toXRayImageRejectsUnknownBodyPart() {
        FakeDicomFile dicom = new FakeDicomFile(new File(folder, "study.dcm"), "CHEST",
                1, 1, new short[]{500});
        XRayImageSource source = new DicomXRayAdapter(dicom);

        assertThrows(IllegalArgumentException.class, () -> source.toXRayImage(null));
    }

    /** Test double for the external DICOM library. */
    private static class FakeDicomFile extends DicomFile {

        private final String bodyPart;
        private final int rows;
        private final int columns;
        private final short[] pixels;

        FakeDicomFile(File file, String bodyPart, int rows, int columns, short[] pixels) {
            super(file);
            this.bodyPart = bodyPart;
            this.rows = rows;
            this.columns = columns;
            this.pixels = pixels;
        }

        @Override
        public short[] readPixelData() { return pixels; }

        @Override
        public int getRows() { return rows; }

        @Override
        public int getColumns() { return columns; }

        @Override
        public String getBodyPartExamined() { return bodyPart; }

        @Override
        public String getPatientId() { return "PATIENT-123"; }
    }
}
