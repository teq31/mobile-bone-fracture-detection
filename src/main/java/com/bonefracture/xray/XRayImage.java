package com.bonefracture.xray;

import com.bonefracture.detection.AnalysisResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class XRayImage {

    private String path;
    private LocalDateTime uploadDate;
    private FileFormat format;
    private BodyPart bodyPart;
    private boolean takenWithCamera;
    private List<AnalysisResult> results = new ArrayList<>();

    public XRayImage(String path, FileFormat format, BodyPart bodyPart, boolean takenWithCamera) {
        this.path = path;
        this.format = format;
        this.bodyPart = bodyPart;
        this.takenWithCamera = takenWithCamera;
        this.uploadDate = LocalDateTime.now();
    }

    public boolean validate() {
        return false;
    }

    public void addResult(AnalysisResult r) {
        results.add(r);
    }

    public List<AnalysisResult> getResults() { return results; }

    public String getPath() { return path; }
    public FileFormat getFormat() { return format; }
    public BodyPart getBodyPart() { return bodyPart; }
    public LocalDateTime getUploadDate() {
        return uploadDate;
    }
}
