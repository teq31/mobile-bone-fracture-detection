package com.bonefracture.detection;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class YoloOnnxModelTest {

    // created by ml/export_onnx.py, not in git
    private static final Path MODEL = Path.of("models/yolov8-fracture.onnx");
    // GRAZPEDWRI-DX test image, 16-bit grey PNG
    private static final String WRIST = "src/test/resources/xray/0020_0163123912_01_WRI-L1_M015.png";

    @Test
    void classNamesComeFromTheModelMetadata() {
        List<String> names = YoloOnnxModel.parseNames(
                "{0: 'boneanomaly', 1: 'bonelesion', 2: 'foreignbody', 3: 'fracture'}");
        assertEquals(List.of("boneanomaly", "bonelesion", "foreignbody", "fracture"), names);
    }

    @Test
    void missingModelFileIsReported() {
        YoloOnnxModel model = new YoloOnnxModel("v8", "models/does-not-exist.onnx");
        assertFalse(model.isAvailable());
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> model.detect(new XRayImage(WRIST, FileFormat.PNG, BodyPart.WRIST, false)));
        assertTrue(e.getMessage().contains("does-not-exist.onnx"));
    }

    @Test
    void findsTheSameBoxesAsUltralytics() {
        assumeTrue(Files.isRegularFile(MODEL), "run ml/export_onnx.py first");
        YoloOnnxModel model = new YoloOnnxModel("v8", MODEL.toString());
        assertTrue(model.isAvailable());

        AnalysisResult result = model.detect(new XRayImage(WRIST, FileFormat.PNG, BodyPart.WRIST, false));

        // reference: Ultralytics 8.4 with the same ONNX file, conf 0.25, iou 0.45
        assertSame(model, result.getModel());
        List<Detection> found = result.getDetections();
        assertEquals(5, found.size());
        assertDetection(found.get(0), "text", 0.847f, new Rectangle(461, 173, 81, 120));
        assertDetection(found.get(1), "fracture", 0.732f, new Rectangle(707, 94, 82, 68));
        assertDetection(found.get(2), "fracture", 0.389f, new Rectangle(424, 520, 71, 71));
        assertDetection(found.get(3), "fracture", 0.277f, new Rectangle(410, 546, 98, 128));
        assertDetection(found.get(4), "fracture", 0.257f, new Rectangle(485, 731, 105, 132));
    }

    private static void assertDetection(Detection d, String label, float confidence, Rectangle box) {
        assertEquals(label, d.getLabel());
        assertEquals(confidence, d.getConfidence(), 0.01);
        Rectangle r = d.getBoundingBox();
        assertEquals(box.x, r.x, 3);
        assertEquals(box.y, r.y, 3);
        assertEquals(box.width, r.width, 3);
        assertEquals(box.height, r.height, 3);
    }
}
