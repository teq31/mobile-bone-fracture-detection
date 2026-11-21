package com.bonefracture.xray.adapter;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;

// Adapter pattern - Adapter
// Converts a photo of an X-ray taken with the phone camera into an XRayImage (saved as JPG).
public class CameraXRayAdapter implements XRayImageSource {

    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private CameraCapture capture;
    private String saveDirectory;

    public CameraXRayAdapter(CameraCapture capture, String saveDirectory) {
        this.capture = capture;
        this.saveDirectory = saveDirectory;
    }

    @Override
    public XRayImage toXRayImage(BodyPart bodyPart) {
        AdapterSupport.requireBodyPart(bodyPart);
        byte[] photo = capture.getJpegBytes();
        if (photo == null || photo.length == 0) {
            throw new IllegalArgumentException("The camera photo is empty");
        }
        File jpg = new File(saveDirectory, "xray-" + capture.getCapturedAt().format(FILE_TIME) + ".jpg");
        try {
            Files.write(jpg.toPath(), photo);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the camera photo", e);
        }
        return new XRayImage(jpg.getPath(), FileFormat.JPG, bodyPart, true);
    }

    @Override
    public FileFormat getFormat() { return FileFormat.JPG; }

    @Override
    public boolean isTakenWithCamera() { return true; }
}
