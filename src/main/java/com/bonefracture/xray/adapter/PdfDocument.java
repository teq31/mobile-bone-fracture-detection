package com.bonefracture.xray.adapter;

import java.awt.image.BufferedImage;
import java.io.File;

// Adapter pattern - Adaptee
// Stands for an external PDF library (e.g. Apache PDFBox), which has its own interface:
// it works with pages, not with X-ray images.
public class PdfDocument {

    private File file;
    private int pageCount;

    public PdfDocument(File file) {
        this.file = file;
    }

    public int getPageCount() { return pageCount; }

    public BufferedImage renderPage(int pageIndex, int dpi) {
        // TODO
        return null;
    }
}
