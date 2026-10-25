package com.bonefracture.detection;

import com.bonefracture.xray.XRayImage;

public class YoloModel extends DetectionModel {

    private String weightsPath;
    private int inputSize = 640;
    private float minConfidence = 0.25f;

    public YoloModel(String version, String weightsPath) {
        super("YOLO", version);
        this.weightsPath = weightsPath;
    }

    @Override
    public AnalysisResult detect(XRayImage image) {
        // TODO
        return new AnalysisResult(this);
    }
}
