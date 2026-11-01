package com.bonefracture.detection;

import com.bonefracture.xray.XRayImage;

public class FractureDetector {

    private DetectionModel model;

    public FractureDetector(DetectionModel model) {
        if (model == null) {
            throw new IllegalArgumentException("model is null");
        }
        this.model = model;
    }

    public FractureDetector(ModelRegistry registry) {
        this.model = registry.getMainModel();
    }

    public void setModel(DetectionModel model) {
        if (model == null) {
            throw new IllegalArgumentException("model is null");
        }
        this.model = model;
    }

    public DetectionModel getModel() {
        return model;
    }

    public AnalysisResult analyze(XRayImage img) {
        AnalysisResult result = model.detect(img);
        img.addResult(result);
        return result;
    }
}
