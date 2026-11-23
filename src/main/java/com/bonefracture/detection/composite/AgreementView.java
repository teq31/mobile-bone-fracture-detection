package com.bonefracture.detection.composite;

import com.bonefracture.detection.AnalysisResult;
import com.bonefracture.detection.Detection;
import com.bonefracture.detection.ModelRegistry;
import com.bonefracture.xray.XRayImage;

import java.util.ArrayList;
import java.util.List;

public class AgreementView {

    private ModelRegistry registry;
    private float minIou;

    public AgreementView(ModelRegistry registry, float minIou) {
        this.registry = registry;
        this.minIou = minIou;
    }

    public List<AnalysisResult> runAllModels(XRayImage image) {
        List<AnalysisResult> results = new ArrayList<>();
        for (var model : registry.getAllModels()) {
            results.add(model.detect(image));
        }
        return results;
    }

    public DetectionGroup buildView(List<AnalysisResult> results) {
        DetectionGroup view = new DetectionGroup(minIou);
        List<DetectionGroup> clusters = new ArrayList<>();

        for (AnalysisResult result : results) {
            for (Detection detection : result.getDetections()) {
                DetectionLeaf leaf = new DetectionLeaf(detection, result.getModel());
                DetectionGroup target = null;
                for (DetectionGroup cluster : clusters) {
                    if (cluster.accepts(leaf)) {
                        target = cluster;
                        break;
                    }
                }
                if (target == null) {
                    target = new DetectionGroup(minIou);
                    clusters.add(target);
                }
                target.add(leaf);
            }
        }

        for (DetectionGroup cluster : clusters) {
            view.add(cluster);
        }
        return view;
    }

    public AgreementLevel getLevel(DetectionComponent component) {
        int totalModels = registry.getAllModels().size();
        int count = component.getAgreementCount();
        if (count <= 1) {
            return AgreementLevel.NONE;
        }
        if (count >= totalModels) {
            return AgreementLevel.FULL;
        }
        return AgreementLevel.PARTIAL;
    }
}
