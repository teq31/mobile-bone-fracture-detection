package com.bonefracture.xray.adapter;

import java.io.File;

// Adapter pattern - Adaptee
// Stands for an external DICOM library (e.g. dcm4che): 16-bit pixel data plus medical metadata.
public class DicomFile {

    private File file;
    private int rows;
    private int columns;
    private String bodyPartExamined; // DICOM tag (0018,0015)
    private String patientId;        // must not leave the device

    public DicomFile(File file) {
        this.file = file;
    }

    public short[] readPixelData() {
        // TODO
        return null;
    }

    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public String getBodyPartExamined() { return bodyPartExamined; }
    public String getPatientId() { return patientId; }
}
