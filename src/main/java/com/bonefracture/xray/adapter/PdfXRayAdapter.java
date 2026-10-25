package com.bonefracture.xray.adapter;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;

// Adapter pattern - Adapter
// Converts a page of a PDF document into an XRayImage.
public class PdfXRayAdapter implements XRayImageSource {

    private static final int RENDER_DPI = 300;

    private PdfDocument pdf;
    private int pageIndex; // page that holds the X-ray, first page by default

    public PdfXRayAdapter(PdfDocument pdf) {
        this(pdf, 0);
    }

    public PdfXRayAdapter(PdfDocument pdf, int pageIndex) {
        this.pdf = pdf;
        this.pageIndex = pageIndex;
    }

    @Override
    public XRayImage toXRayImage(BodyPart bodyPart) {
        // TODO render the page with pdf.renderPage(pageIndex, RENDER_DPI),
        //  save it as PNG and build the XRayImage
        return null;
    }

    @Override
    public FileFormat getFormat() { return FileFormat.PDF; }

    @Override
    public boolean isTakenWithCamera() { return false; }
}
