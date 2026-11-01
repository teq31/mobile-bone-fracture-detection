package com.bonefracture.xray.adapter;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;

import java.io.File;

// Adapter pattern - Adapter for plain image files (JPG / PNG)
public class ImageFileAdapter implements XRayImageSource {

    private File file;
    private FileFormat format; // JPG or PNG

    public ImageFileAdapter(File file, FileFormat format) {
        if (format != FileFormat.JPG && format != FileFormat.PNG) {
            throw new IllegalArgumentException("ImageFileAdapter accepts only JPG or PNG files, not " + format);
        }
        this.file = file;
        this.format = format;
    }

    @Override
    public XRayImage toXRayImage(BodyPart bodyPart) {
        if (bodyPart == null) {
            throw new IllegalArgumentException("The body part of the X-ray is required");
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("File not found: " + file.getPath());
        }
        return new XRayImage(file.getPath(), format, bodyPart, false);
    }

    @Override
    public FileFormat getFormat() { return format; }

    @Override
    public boolean isTakenWithCamera() { return false; }
}
