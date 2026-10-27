package com.bonefracture.xray;

// Facade pattern - subsystem class
// Prepares an image for the detection model.
public class ImagePreprocessor {

    private int targetSize = 640;

    public XRayImage resize(XRayImage image) {
        // TODO
        return null;
    }

    public XRayImage normalize(XRayImage image) {
        // TODO
        return null;
    }

    public int getTargetSize() { return targetSize; }
}
