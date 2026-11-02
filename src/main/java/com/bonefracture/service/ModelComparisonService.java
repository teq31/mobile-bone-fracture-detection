package com.bonefracture.service;

import com.bonefracture.detection.ModelRegistry;
import com.bonefracture.detection.composite.AgreementLevel;
import com.bonefracture.detection.composite.AgreementView;
import com.bonefracture.detection.composite.DetectionComponent;
import com.bonefracture.detection.composite.DetectionGroup;
import com.bonefracture.xray.XRayImage;

public class ModelComparisonService {

    private final ModelRegistry registry;
    private final AgreementView agreementView;

    public ModelComparisonService(ModelRegistry registry, float minIou) {
        this.registry = registry;
        this.agreementView = new AgreementView(registry, minIou);
    }

    public DetectionGroup compareModels(XRayImage image) {
        return agreementView.buildView(agreementView.runAllModels(image));
    }

    public AgreementLevel getAgreementLevel(DetectionComponent finding) {
        return agreementView.getLevel(finding);
    }

    public int getRegisteredModelCount() {
        return registry.getAllModels().size();
    }
}
