package com.bonefracture.detection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnalysisResult {

    private DetectionModel model;
    private List<Detection> detections = new ArrayList<>();

    public AnalysisResult(DetectionModel model) {
        this.model = model;
    }

    public void addDetection(Detection d) {
        detections.add(d);
    }

    public List<Detection> getDetections() {
        return detections;
    }

    public DetectionModel getModel() { return model; }

    public File exportPdf() {
        return null; // later
    }
}
