package com.bonefracture.detection;

import com.bonefracture.xray.XRayImage;

public class RfDetrModel extends DetectionModel {
    private String weightsPath;
    private String backbone;

    public RfDetrModel(String version, String weightsPath, String backbone) {
        super("RF-DETR", version);
        this.weightsPath = weightsPath;
        this.backbone = backbone;
    }

    @Override
    public AnalysisResult detect(XRayImage image) {
        return new AnalysisResult(this);
    }
}
