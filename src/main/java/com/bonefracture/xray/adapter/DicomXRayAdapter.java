package com.bonefracture.xray.adapter;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;

// Adapter pattern - Adapter
// Converts a DICOM file (the format used in hospitals) into an XRayImage (8-bit PNG next to the file).
// Only the pixels and the body part are used: patient identifiers are never copied.
public class DicomXRayAdapter implements XRayImageSource {

    private DicomFile dicom;

    public DicomXRayAdapter(DicomFile dicom) {
        this.dicom = dicom;
    }

    @Override
    public XRayImage toXRayImage(BodyPart bodyPart) {
        BodyPart part = bodyPart != null ? bodyPart : mapBodyPart(dicom.getBodyPartExamined());

        int rows = dicom.getRows();
        int columns = dicom.getColumns();
        short[] pixels = dicom.readPixelData();
        if (pixels == null || pixels.length != rows * columns) {
            throw new IllegalArgumentException("The DICOM pixel data does not match the image size");
        }

        // 16-bit -> 8-bit: stretch the darkest pixel to 0 and the brightest to 255
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (short pixel : pixels) {
            int value = Short.toUnsignedInt(pixel);
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        int range = Math.max(1, max - min);
        BufferedImage gray = new BufferedImage(columns, rows, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = gray.getRaster();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                int value = Short.toUnsignedInt(pixels[y * columns + x]);
                raster.setSample(x, y, 0, Math.round((value - min) * 255f / range));
            }
        }

        File png = AdapterSupport.siblingFile(dicom.getFile(), "dcm", ".png");
        AdapterSupport.savePng(gray, png);
        return new XRayImage(png.getPath(), FileFormat.PNG, part, false);
    }

    private BodyPart mapBodyPart(String bodyPartExamined) {
        if (bodyPartExamined == null || bodyPartExamined.isBlank()) {
            throw new IllegalArgumentException("The DICOM file has no body part, choose it manually");
        }
        try {
            return BodyPart.valueOf(bodyPartExamined.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Body part not supported yet: " + bodyPartExamined);
        }
    }

    @Override
    public FileFormat getFormat() { return FileFormat.DICOM; }

    @Override
    public boolean isTakenWithCamera() { return false; }
}
