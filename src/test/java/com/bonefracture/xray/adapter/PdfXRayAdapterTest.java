package com.bonefracture.xray.adapter;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD Iteration 1 - ADAPTER pattern: a page of a PDF document -> XRayImage (PNG).
 * FakePdfDocument stands in for the external PDF library (the Adaptee).
 */
class PdfXRayAdapterTest {

    private File folder;
    private FakePdfDocument pdf;

    @BeforeEach
    void setUp() throws IOException {
        folder = Files.createTempDirectory("pdf-adapter").toFile();
        pdf = new FakePdfDocument(new File(folder, "scan.pdf"), 2);
    }

    @Test
    void toXRayImageRendersFirstPageAsPng() {
        XRayImageSource source = new PdfXRayAdapter(pdf);

        XRayImage image = source.toXRayImage(BodyPart.WRIST);

        assertNotNull(image);
        assertEquals(FileFormat.PNG, image.getFormat());
        assertEquals(BodyPart.WRIST, image.getBodyPart());
        assertEquals(new File(folder, "scan-page1.png").getPath(), image.getPath());
        assertTrue(new File(image.getPath()).exists());
        assertEquals(0, pdf.renderedPage);
        assertEquals(300, pdf.renderedDpi);
    }

    @Test
    void toXRayImageRendersChosenPage() {
        XRayImageSource source = new PdfXRayAdapter(pdf, 1);

        XRayImage image = source.toXRayImage(BodyPart.ELBOW);

        assertNotNull(image);
        assertEquals(1, pdf.renderedPage);
        assertEquals(new File(folder, "scan-page2.png").getPath(), image.getPath());
    }

    @Test
    void constructorRejectsPageOutsideDocument() {
        assertThrows(IllegalArgumentException.class, () -> new PdfXRayAdapter(pdf, 5));
    }

    /** Test double for the external PDF library. */
    private static class FakePdfDocument extends PdfDocument {

        private final int pages;
        private int renderedPage = -1;
        private int renderedDpi = -1;

        FakePdfDocument(File file, int pages) {
            super(file);
            this.pages = pages;
        }

        @Override
        public int getPageCount() {
            return pages;
        }

        @Override
        public BufferedImage renderPage(int pageIndex, int dpi) {
            renderedPage = pageIndex;
            renderedDpi = dpi;
            return new BufferedImage(4, 4, BufferedImage.TYPE_BYTE_GRAY);
        }
    }
}
