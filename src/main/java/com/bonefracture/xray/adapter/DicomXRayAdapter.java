package com.bonefracture.xray.adapter;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;

// Adapter pattern - Adapter
// Converts a DICOM file (the format used in hospitals) into an XRayImage.
public class DicomXRayAdapter implements XRayImageSource {

    private DicomFile dicom;

    public DicomXRayAdapter(DicomFile dicom) {
        this.dicom = dicom;
    }

    @Override
    public XRayImage toXRayImage(BodyPart bodyPart) {
        // TODO convert the 16-bit pixel data to an 8-bit PNG, drop patient identifiers,
        //  use dicom.getBodyPartExamined() when bodyPart is null
        return null;
    }

    private BodyPart mapBodyPart(String bodyPartExamined) {
        // TODO e.g. "WRIST" -> BodyPart.WRIST
        return null;
    }

    @Override
    public FileFormat getFormat() { return FileFormat.DICOM; }

    @Override
    public boolean isTakenWithCamera() { return false; }
}
