package com.bonefracture.xray.adapter;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;

// Adapter pattern - Adapter
// Converts a photo of an X-ray taken with the phone camera into an XRayImage.
public class CameraXRayAdapter implements XRayImageSource {

    private CameraCapture capture;
    private String saveDirectory;

    public CameraXRayAdapter(CameraCapture capture, String saveDirectory) {
        this.capture = capture;
        this.saveDirectory = saveDirectory;
    }

    @Override
    public XRayImage toXRayImage(BodyPart bodyPart) {
        // TODO fix perspective and reflections, save the bytes as JPG
        //  in saveDirectory and build the XRayImage (takenWithCamera = true)
        return null;
    }

    @Override
    public FileFormat getFormat() { return FileFormat.JPG; }

    @Override
    public boolean isTakenWithCamera() { return true; }
}
