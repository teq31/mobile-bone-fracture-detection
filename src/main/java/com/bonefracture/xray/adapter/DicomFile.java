package com.bonefracture.xray.adapter;

import java.io.File;

// Adaptee - placeholder for dcm4che
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

    public File getFile() { return file; }
    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public String getBodyPartExamined() { return bodyPartExamined; }
    public String getPatientId() { return patientId; }
}
