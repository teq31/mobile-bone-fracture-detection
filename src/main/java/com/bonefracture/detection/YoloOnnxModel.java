package com.bonefracture.detection;

import com.bonefracture.xray.XRayImage;

import java.util.List;

public class YoloOnnxModel extends DetectionModel {

    public YoloOnnxModel(String version, String modelPath) {
        super("YOLO", version);
    }

    public boolean isAvailable() {
        return true;
    }

    @Override
    public AnalysisResult detect(XRayImage image) {
        return new AnalysisResult(this);
    }

    static List<String> parseNames(String metadata) {
        return List.of();
    }
}
