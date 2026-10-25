package com.bonefracture.detection;

import com.bonefracture.xray.XRayImage;

public class FractureDetector {

    private DetectionModel model;

    public FractureDetector(DetectionModel model) {
        this.model = model;
    }

    public void setModel(DetectionModel model) {
        this.model = model;
    }

    public AnalysisResult analyze(XRayImage img) {
        AnalysisResult result = model.detect(img);
        img.addResult(result);
        return result;
    }
}
