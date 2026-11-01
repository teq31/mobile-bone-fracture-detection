package com.bonefracture.xray.adapter;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

// Adapter pattern - Adapter
// Converts a page of a PDF document into an XRayImage (saved as PNG next to the PDF).
public class PdfXRayAdapter implements XRayImageSource {

    private static final int RENDER_DPI = 300;

    private PdfDocument pdf;
    private int pageIndex; // page that holds the X-ray, first page by default

    public PdfXRayAdapter(PdfDocument pdf) {
        this(pdf, 0);
    }

    public PdfXRayAdapter(PdfDocument pdf, int pageIndex) {
        if (pageIndex < 0 || pageIndex >= pdf.getPageCount()) {
            throw new IllegalArgumentException("Page " + (pageIndex + 1) + " does not exist, the PDF has "
                    + pdf.getPageCount() + " pages");
        }
        this.pdf = pdf;
        this.pageIndex = pageIndex;
    }

    @Override
    public XRayImage toXRayImage(BodyPart bodyPart) {
        if (bodyPart == null) {
            throw new IllegalArgumentException("The body part of the X-ray is required");
        }
        BufferedImage page = pdf.renderPage(pageIndex, RENDER_DPI);

        File source = pdf.getFile();
        String baseName = source.getName().replaceFirst("(?i)\\.pdf$", "");
        File png = new File(source.getParentFile(), baseName + "-page" + (pageIndex + 1) + ".png");
        try {
            ImageIO.write(page, "png", png);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the PDF page as PNG", e);
        }
        return new XRayImage(png.getPath(), FileFormat.PNG, bodyPart, false);
    }

    @Override
    public FileFormat getFormat() { return FileFormat.PDF; }

    @Override
    public boolean isTakenWithCamera() { return false; }
}
