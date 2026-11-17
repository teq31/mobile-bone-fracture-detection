package com.bonefracture.aspect;

import com.bonefracture.detection.AnalysisResult;
import com.bonefracture.detection.Detection;
import com.bonefracture.detection.DetectionModel;
import com.bonefracture.detection.ModelRegistry;
import com.bonefracture.service.ModelComparisonService;
import com.bonefracture.xray.XRayImage;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelComparisonValidationAspectTest {

    private static class FakeModel extends DetectionModel {
        FakeModel(String name) {
            super(name, "1.0");
        }

        @Override
        public AnalysisResult detect(XRayImage image) {
            AnalysisResult result = new AnalysisResult(this);
            result.addDetection(new Detection("fracture", 0.8f, new Rectangle(0, 0, 10, 10), null));
            return result;
        }
    }

    private static class XRayImageStub extends XRayImage {
        XRayImageStub() {
            super("test.png", null, null, false);
        }
    }

    @Test
    void compareModels_rejectsNullImage() {
        ModelRegistry registry = new ModelRegistry();
        registry.register(new FakeModel("YOLO"));
        registry.register(new FakeModel("RF-DETR"));
        ModelComparisonService service = new ModelComparisonService(registry, 0.5f);

        assertThrows(ModelComparisonValidationException.class,
                () -> service.compareModels(null));
    }

    @Test
    void compareModels_rejectsFewerThanTwoModels() {
        ModelRegistry registry = new ModelRegistry();
        registry.register(new FakeModel("YOLO"));
        ModelComparisonService service = new ModelComparisonService(registry, 0.5f);

        assertThrows(ModelComparisonValidationException.class,
                () -> service.compareModels(new XRayImageStub()));
    }

    @Test
    void compareModels_passesValidationWithTwoModels() {
        ModelRegistry registry = new ModelRegistry();
        registry.register(new FakeModel("YOLO"));
        registry.register(new FakeModel("RF-DETR"));
        ModelComparisonService service = new ModelComparisonService(registry, 0.5f);

        assertNotNull(service.compareModels(new XRayImageStub()));
    }
}
