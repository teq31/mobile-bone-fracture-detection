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
        this.file = file;
        this.format = format;
    }

    @Override
    public XRayImage toXRayImage(BodyPart bodyPart) {
        // TODO copy the file to app storage and build the XRayImage
        return null;
    }

    @Override
    public FileFormat getFormat() { return format; }

    @Override
    public boolean isTakenWithCamera() { return false; }
}
