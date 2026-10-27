package com.bonefracture.detection.composite;

import com.bonefracture.detection.AnalysisResult;
import com.bonefracture.detection.ModelRegistry;
import com.bonefracture.xray.XRayImage;

import java.util.List;

public class AgreementView {

    private ModelRegistry registry;
    private float minIou;

    public AgreementView(ModelRegistry registry, float minIou) {
        this.registry = registry;
        this.minIou = minIou;
    }

    public List<AnalysisResult> runAllModels(XRayImage image) {
        // TODO call detect() on registry.getAllModels()
        return null;
    }

    public DetectionGroup buildView(List<AnalysisResult> results) {
        // TODO
        return null;
    }

    public AgreementLevel getLevel(DetectionComponent component) {
        // TODO compare getAgreementCount() with the number of registered models
        return null;
    }
}