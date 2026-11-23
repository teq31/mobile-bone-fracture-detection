package com.bonefracture.xray.adapter;

import java.awt.image.BufferedImage;
import java.io.File;

// Adaptee - placeholder for PDFBox
public class PdfDocument {

    private File file;
    private int pageCount;

    public PdfDocument(File file) {
        this.file = file;
    }

    public File getFile() { return file; }

    public int getPageCount() { return pageCount; }

    public BufferedImage renderPage(int pageIndex, int dpi) {
        // TODO
        return null;
    }
}
