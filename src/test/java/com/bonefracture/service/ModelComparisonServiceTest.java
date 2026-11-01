package com.bonefracture.service;

import com.bonefracture.detection.AnalysisResult;
import com.bonefracture.detection.Detection;
import com.bonefracture.detection.DetectionModel;
import com.bonefracture.detection.ModelRegistry;
import com.bonefracture.detection.composite.AgreementLevel;
import com.bonefracture.detection.composite.DetectionGroup;
import com.bonefracture.xray.XRayImage;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.*;

class ModelComparisonServiceTest {

    private static class FakeModel extends DetectionModel {
        private final Rectangle box;

        FakeModel(String name, Rectangle box) {
            super(name, "1.0");
            this.box = box;
        }

        @Override
        public AnalysisResult detect(XRayImage image) {
            AnalysisResult result = new AnalysisResult(this);
            result.addDetection(new Detection("fracture", 0.8f, box, null));
            return result;
        }
    }

    @Test
    void getRegisteredModelCount_reflectsTheRegistry() {
        ModelRegistry registry = new ModelRegistry();
        registry.register(new FakeModel("YOLO", new Rectangle(0, 0, 10, 10)));
        registry.register(new FakeModel("RF-DETR", new Rectangle(0, 0, 10, 10)));

        ModelComparisonService service = new ModelComparisonService(registry, 0.5f);

        assertEquals(2, service.getRegisteredModelCount());
    }

    @Test
    void compareModels_groupsOverlappingDetectionsFromAllModels() {
        ModelRegistry registry = new ModelRegistry();
        registry.register(new FakeModel("YOLO", new Rectangle(0, 0, 10, 10)));
        registry.register(new FakeModel("RF-DETR", new Rectangle(1, 1, 10, 10)));

        ModelComparisonService service = new ModelComparisonService(registry, 0.5f);
        DetectionGroup view = service.compareModels(new XRayImageStub());

        assertNotNull(view);
        assertEquals(2, view.getAgreementCount());
    }

    @Test
    void getAgreementLevel_isFullWhenEveryModelAgrees() {
        ModelRegistry registry = new ModelRegistry();
        registry.register(new FakeModel("YOLO", new Rectangle(0, 0, 10, 10)));
        registry.register(new FakeModel("RF-DETR", new Rectangle(1, 1, 10, 10)));

        ModelComparisonService service = new ModelComparisonService(registry, 0.5f);
        DetectionGroup view = service.compareModels(new XRayImageStub());

        assertEquals(AgreementLevel.FULL, service.getAgreementLevel(view));
    }

    private static class XRayImageStub extends XRayImage {
        XRayImageStub() {
            super("test.png", null, null, false);
        }
    }
}
