package com.bonefracture.detection;

import com.bonefracture.xray.XRayImage;

public class FractureDetector {

    private DetectionModel model;

    public FractureDetector(DetectionModel model) {
        this.model = model;
    }

    public FractureDetector(ModelRegistry registry) {
    }

    public void setModel(DetectionModel model) {
        this.model = model;
    }

    public DetectionModel getModel() {
        return null;
    }

    public AnalysisResult analyze(XRayImage img) {
        AnalysisResult result = model.detect(img);
        img.addResult(result);
        return result;
    }
}
