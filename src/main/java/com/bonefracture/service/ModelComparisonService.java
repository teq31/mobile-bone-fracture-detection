package com.bonefracture.service;

import com.bonefracture.detection.ModelRegistry;
import com.bonefracture.detection.composite.AgreementLevel;
import com.bonefracture.detection.composite.DetectionComponent;
import com.bonefracture.detection.composite.DetectionGroup;
import com.bonefracture.xray.XRayImage;

public class ModelComparisonService {

    private final ModelRegistry registry;
    private final float minIou;

    public ModelComparisonService(ModelRegistry registry, float minIou) {
        this.registry = registry;
        this.minIou = minIou;
    }

    public DetectionGroup compareModels(XRayImage image) {
        // TODO run all models through AgreementView and build the grouped view
        return null;
    }

    public AgreementLevel getAgreementLevel(DetectionComponent finding) {
        // TODO delegate to AgreementView / DetectionGroup using the registered model count
        return null;
    }

    public int getRegisteredModelCount() {
        // TODO number of models currently registered
        return 0;
    }
}
