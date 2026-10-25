package com.bonefracture.xray.adapter;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;

// Adapter pattern - Target
// The rest of the app works only with XRayImage, no matter where the image comes from
// (JPG/PNG file, PDF, phone camera or DICOM).
public interface XRayImageSource {

    XRayImage toXRayImage(BodyPart bodyPart);

    FileFormat getFormat();

    boolean isTakenWithCamera();
}
